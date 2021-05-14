package it.unipi.dii.iodetectionlib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import it.unipi.dii.iodetectionlib.ml.Model;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class IODetection implements SensorEventListener, LocationListener {

    private static final long GPS_UPDATE_INTERVAL = 15000;
    private static final long ACTIVITY_COLLECTION_INTERVAL = 15000;
    private final SensorManager sensorManager;
    private final WifiManager wifiManager;
    private final BluetoothAdapter bluetoothAdapter;
    private final ActivityRecognitionClient mActivityRecognitionClient;
    private final Handler periodicHandler;
    private final Runnable periodicRunnable;
    private static final long PERIODIC_DELAY = 30000;
    private static final String TAG = IODetection.class.getName();
    private final WiFiAPCounter wiFiAPCounter;
    private final BLTCounter bltCounter;
    private Context context = null;
    private LocationManager locationManager = null;
    private GnssStatus.Callback gnssStatusCallback = null;
    private FeatureVector featureVector;
    private Model ioDetectionNN;
    private final BroadcastReceiver activityDataReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (featureVector != null)
                featureVector.setActivity(intent.getIntExtra("activity_type",-1));
        }
    };

    public IODetection(Context context){
        this.context = context;
        try {
            ioDetectionNN = Model.newInstance(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        featureVector = new FeatureVector();
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mActivityRecognitionClient = new ActivityRecognitionClient(context);

        periodicHandler = new Handler(Looper.getMainLooper());
        periodicRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                periodicHandler.postDelayed(this, PERIODIC_DELAY);
                scan();
            }
        };

        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null)
            Log.e(TAG, "Device does not have a light sensor!");
        else if (!sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST))
            Log.e(TAG, "Unable to register listener for sensor " + lightSensor.getName() + ".");
        Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor == null)
            Log.e(TAG, "Device does not have a proximity sensor!");
        else if (!sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_FASTEST))
            Log.e(TAG, "Unable to register listener for sensor " + proximitySensor.getName() + ".");
        periodicHandler.post(periodicRunnable);

        wiFiAPCounter = new WiFiAPCounter(featureVector);
        IntentFilter intentFilter_WIFI = new IntentFilter();
        intentFilter_WIFI.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wiFiAPCounter, intentFilter_WIFI);

        bltCounter = new BLTCounter(featureVector);
        IntentFilter intentFilter_BLT = new IntentFilter();
        intentFilter_BLT.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter_BLT.addAction(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(bltCounter, intentFilter_BLT);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "ACCESS_FINE_LOCATION not granted. Can not get GPS status.");
        } else {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            gnssStatusCallback = new GnssStatus.Callback()
            {
                @Override
                public void onStarted()
                {
                }

                @Override
                public void onStopped()
                {
                }

                @SuppressLint("MissingPermission")
                @Override
                public void onFirstFix(int ttffMillis)
                {
                    featureVector.setTimestampGPSLastFix(System.currentTimeMillis());
                }

                @Override
                public void onSatelliteStatusChanged(GnssStatus status)
                {
                    int satCount = status.getSatelliteCount();
                    featureVector.setGPSSatellites(satCount);

                    int fixCount = 0;
                    for (int i = 0; i < satCount; i++) {
                        if (status.usedInFix(i))
                            fixCount++;
                    }
                    featureVector.setGPSFixSatellites(fixCount);
                }
            };
            locationManager.registerGnssStatusCallback(gnssStatusCallback, new Handler(Looper.getMainLooper()));
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,GPS_UPDATE_INTERVAL, 0, this);
        }

        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                ACTIVITY_COLLECTION_INTERVAL,
                getActivityDetectionPendingIntent());
        task.addOnSuccessListener(result -> Toast.makeText(context,
                "Activity Detection Success",
                Toast.LENGTH_SHORT)
                .show());

        task.addOnFailureListener(e -> {
            Log.i(TAG, "onFailure: " + e.toString()  );
            e.printStackTrace();
            Toast.makeText(context,
                    "Activity Detection Failure",
                    Toast.LENGTH_SHORT)
                    .show();
        });

        // This receiver will receive the updates about the activity performed by the user
        context.registerReceiver(activityDataReceiver,new IntentFilter(DetectedActivitiesIntentService.ACTIVITY_DETECTED_ACTION));
    }

    public void stop() {
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        if (wiFiAPCounter != null)
            context.unregisterReceiver(wiFiAPCounter);
        if (bltCounter != null)
            context.unregisterReceiver(bltCounter);
        if (locationManager != null) {
            locationManager.removeUpdates(this);
            locationManager.unregisterGnssStatusCallback(gnssStatusCallback);
        }
        this.mActivityRecognitionClient.removeActivityTransitionUpdates(getActivityDetectionPendingIntent());
        context.unregisterReceiver(activityDataReceiver);
        // Releases model resources if no longer used.
        ioDetectionNN.close();
    }

    public boolean get() {

        float[] fv = featureVector.getFeatureVector();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(fv.length * 4);
        for(float f : fv){
            if(Float.isNaN(f)){
                Log.i(TAG, "get: NON ANCORA PRONTO");
                return true;
            }
            byteBuffer.putFloat(f);
        }
        // Creates inputs for reference.
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 17}, DataType.FLOAT32);
        inputFeature0.loadBuffer(byteBuffer);

        // Runs model inference and gets result.
        Model.Outputs outputs = ioDetectionNN.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
        Log.i(TAG, "get: TENSOR BUFFER" + outputFeature0);
        float output = outputFeature0.getFloatArray()[0];
        if(output > 0.5){
            return true;
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.values.length == 0)
            return;
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            featureVector.setLightIntensity(event.values[0]);
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            featureVector.setProximity(event.values[0]);
        } else {
            return;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "onAccuracyChanged: Accuracy changed for sensor " + sensor.getName() + ", new Accuracy: " + accuracy);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    private PendingIntent getActivityDetectionPendingIntent()
    {
        Intent intent = new Intent(context, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void scan()
    {
        if (!wifiManager.isWifiEnabled())
            Log.w(TAG, "WiFi is not enabled.");
        else if (!wifiManager.startScan()) // FIXME: deprecated
            Log.e(TAG, "Failed to start WiFi scan.");
        if (!bluetoothAdapter.isEnabled())
            Log.w(TAG, "Bluetooth is not enabled.");
        else if (!bluetoothAdapter.isDiscovering())
            if (!bluetoothAdapter.startDiscovery())
                Log.e(TAG, "Failed to start BT discovery.");
    }
}
