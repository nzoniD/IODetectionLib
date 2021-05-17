package it.unipi.dii.iodetectiontest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0xBEEF;
    private static final int ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE = 105;
    private static final int ACCESS_BACKGROUND_LOCATION_PERMISSION_CODE = 106;
    private static final int ACTIVITY_RECOGNITION_PERMISSION_CODE = 107;
    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            checkPermission(Manifest.permission.ACTIVITY_RECOGNITION, ACTIVITY_RECOGNITION_PERMISSION_CODE);
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    ACCESS_BACKGROUND_LOCATION_PERMISSION_CODE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            checkPermission(Manifest.permission.ACTIVITY_RECOGNITION,ACTIVITY_RECOGNITION_PERMISSION_CODE);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "startMonitoring: Device doesn't support bluetooth.");
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Intent i = new Intent(getApplicationContext(),IODetectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(i);
        }else{
            getApplicationContext().startService(i);
        }
        IntentFilter intentFilter = new IntentFilter(IODetectionService.IODetectionAction);
        this.registerReceiver(ioReceiver, intentFilter);
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "ON RESUME");
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(IODetectionService.IODetectionAction);
        this.registerReceiver(ioReceiver, intentFilter);
    }


    private void checkPermission(String permission, int requestCode)
    {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission " + permission + " already granted.");
            return;
        }
        requestPermissions(new String[] { permission }, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String permissionName = String.valueOf(requestCode);
        if (requestCode == ACCESS_FINE_LOCATION_STATE_PERMISSION_CODE)
            permissionName = "ACCESS_FINE_LOCATION_STATE";
        else if (requestCode == ACCESS_BACKGROUND_LOCATION_PERMISSION_CODE)
            permissionName = "ACCESS_BACKGROUND_LOCATION";
        else if (requestCode == ACTIVITY_RECOGNITION_PERMISSION_CODE)
            permissionName = "ACTIVITY_RECOGNITION";
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, permissionName + " " + (granted ? "granted." : "denied."));
    }

    private final BroadcastReceiver ioReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, "ON RECEIVE");
            TextView tv = findViewById(R.id.io);
            boolean indoor = intent.getBooleanExtra("indoor",true);
            if(indoor){
                tv.setText("INDOOR");
            }else{
                tv.setText("OUTDOOR");
            }
        }
    };

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(ioReceiver);
        super.onDestroy();
    }
}