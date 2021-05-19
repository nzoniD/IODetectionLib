package it.unipi.dii.iodetectionlib;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import it.unipi.dii.iodetectionlib.collectors.ActivityCollector;
import it.unipi.dii.iodetectionlib.collectors.BluetoothCollector;
import it.unipi.dii.iodetectionlib.collectors.FeatureCollector;
import it.unipi.dii.iodetectionlib.collectors.GpsCollector;
import it.unipi.dii.iodetectionlib.collectors.SensorCollector;
import it.unipi.dii.iodetectionlib.collectors.WifiCollector;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureId;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureVector;
import it.unipi.dii.iodetectionlib.ml.Model;

public class IODetector implements Closeable
{
	private static final String TAG = IODetector.class.getName();
	private final List<FeatureCollector> collectors;
	private final FeatureVector featureVector;
	private IODetectionResult oldResult;
	private final Model model;
	private Handler periodicHandler;
	private Runnable periodicRunnable;
	private boolean started = false;

	public IODetector(Context context, int samplingPeriod, long scanInterval, long activityInterval, long gpsInterval) throws IOException
	{
		featureVector = new FeatureVector();
		try {
			model = Model.newInstance(context);
		} catch (IOException ex) {
			Log.e(TAG, "Error loading TensorFlow module: " + ex.getMessage());
			throw ex;
		}
		collectors = Arrays.asList(
			new SensorCollector(context, samplingPeriod, featureVector),
			new WifiCollector(context, scanInterval, featureVector),
			new BluetoothCollector(context, scanInterval, featureVector),
			new ActivityCollector(context, activityInterval, featureVector),
			new GpsCollector(context, gpsInterval, featureVector)
		);
	}

	public IODetector(Context context, int samplingPeriod, long scanInterval, long activityInterval) throws IOException
	{
		this(context, samplingPeriod, scanInterval, activityInterval, 60*1000);
	}

	public IODetector(Context context, int samplingPeriod, long scanInterval) throws IOException
	{
		this(context, samplingPeriod, scanInterval, 15*1000);
	}

	public IODetector(Context context, int samplingPeriod) throws IOException
	{
		this(context, samplingPeriod, 30*1000);
	}

	public IODetector(Context context) throws IOException
	{
		this(context, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public static String[] getRequiredPermissions()
	{
		String[][] permissionMatrix = new String[5][];
		permissionMatrix[0] = SensorCollector.getRequiredPermissions();
		permissionMatrix[1] = WifiCollector.getRequiredPermissions();
		permissionMatrix[2] = BluetoothCollector.getRequiredPermissions();
		permissionMatrix[3] = ActivityCollector.getRequiredPermissions();
		permissionMatrix[4] = GpsCollector.getRequiredPermissions();
		int count = 0;
		for (String[] curArray: permissionMatrix)
			count += curArray.length;
		int index = 0;
		String[] permissions = new String[count];
		for (String[] curArray: permissionMatrix)
			for (String permission: curArray) {
				permissions[index] = permission;
				index++;
			}
		return permissions;
	}

	public void start()
	{
		for (FeatureCollector collector: collectors)
			collector.start();
		started = true;
	}

	public IODetectionResult detect()
	{
		ByteBuffer buffer;
		try {
			buffer = featureVector.toByteBuffer();
		} catch (IllegalStateException ex) {
			return new IODetectionResult(Float.NaN);
		}
		TensorBuffer inputFeatures = TensorBuffer.createFixedSize(new int[]{1, FeatureId.values().length}, DataType.FLOAT32);
		inputFeatures.loadBuffer(buffer);

		Model.Outputs outputs = model.process(inputFeatures);
		TensorBuffer outputFeature = outputs.getOutputFeature0AsTensorBuffer();
		float output = outputFeature.getFloatArray()[0];
		IODetectionResult tmp = oldResult;
		if (tmp == null)
			oldResult = new IODetectionResult(output);
		else
			oldResult = new IODetectionResult(output, tmp);
		Log.d(TAG, "DETECTION RESULT: " + oldResult);
		return oldResult;
	}

	public static void setConfidenceThreshold(float threshold)
	{
		IODetectionResult.setConfidenceThreshold(threshold);
	}

	public void registerIODetectionListener(IODetectionListener listener, long detectInterval)
	{
		if (periodicHandler != null)
			return;
		if (!started)
			start();
		periodicHandler = new Handler(Looper.getMainLooper());
		periodicRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				periodicHandler.postDelayed(this, detectInterval);
				listener.onDetectionChange(detect());
			}
		};
		periodicHandler.post(periodicRunnable);
	}

	@Override
	public void close()
	{
		if (!started)
			return;
		started = false;
		if (periodicHandler != null)
			periodicHandler.removeCallbacks(periodicRunnable);
		for (FeatureCollector collector: collectors)
			collector.stop();
		if (model != null)
			model.close();
	}
}
