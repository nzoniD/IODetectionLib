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
import it.unipi.dii.iodetectionlib.collectors.ml.Metadata;
import it.unipi.dii.iodetectionlib.ml.IODetectorModel;

public class IODetector implements Closeable
{
	private static final String TAG = IODetector.class.getName();
	private final List<FeatureCollector> collectors;
	private final FeatureVector featureVector;
	private IODetectionResult oldResult;
	private final IODetectorModel model;
	private Handler periodicHandler;
	private Runnable periodicRunnable;
	private boolean started = false;

	public IODetector(Context context, int samplingPeriod, long scanInterval, long activityInterval, long gpsInterval) throws IOException
	{
		Metadata metadata;
		try {
			model = IODetectorModel.newInstance(context);
			metadata = Metadata.createModelMetadata(context);
		} catch (IOException ex) {
			Log.e(TAG, "Error loading TensorFlow module: " + ex.getMessage());
			throw ex;
		}
		featureVector = new FeatureVector(metadata);
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

	/* Get the list of required permissions for all the collectors. */
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

	/* Starts all the collectors */
	public void start()
	{
		for (FeatureCollector collector: collectors)
			collector.start();
		started = true;
	}

	/* Performs an I/O detection with the last feature vector */
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

		IODetectorModel.Outputs outputs = model.process(inputFeatures);
		TensorBuffer output = outputs.getIODetectionAsTensorBuffer();
		float value = output.getFloatValue(0);
		IODetectionResult tmp = oldResult;
		if (tmp == null)
			oldResult = new IODetectionResult(value);
		else
			oldResult = new IODetectionResult(value, tmp);
		Log.d(TAG, "DETECTION RESULT: " + oldResult);
		return oldResult;
	}

	/* Sets the confidence threshold needed to consider the result of the detection as "validated" */
	public static void setConfidenceThreshold(float threshold)
	{
		IODetectionResult.setConfidenceThreshold(threshold);
	}

	/* Registers a listeners for IODetection. The listener will be called periodically with
	 * the specified interval with the last result of the detection.
	 */
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

	/* Stops all the collectors and deregister the listener */
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
