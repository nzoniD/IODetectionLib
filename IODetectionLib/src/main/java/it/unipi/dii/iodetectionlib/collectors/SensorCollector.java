package it.unipi.dii.iodetectionlib.collectors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import it.unipi.dii.iodetectionlib.collectors.ml.FeatureVector;

public class SensorCollector extends FeatureCollector implements SensorEventListener
{
	private static final String TAG = SensorCollector.class.getName();
	private final SensorManager manager;

	public SensorCollector(Context context, int samplingPeriod, FeatureVector featureVector)
	{
		super(context, samplingPeriod, featureVector);
		manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	}

	@Override
	public void start()
	{
		Sensor lightSensor = manager.getDefaultSensor(Sensor.TYPE_LIGHT);
		if (lightSensor == null)
			Log.e(TAG, "Device does not have a light sensor!");
		else if (!manager.registerListener(this, lightSensor, (int)this.interval))
			Log.e(TAG, "Unable to register listener for sensor " + lightSensor.getName() + ".");
		Sensor proximitySensor = manager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximitySensor == null)
			Log.e(TAG, "Device does not have a proximity sensor!");
		else if (!manager.registerListener(this, proximitySensor, (int)this.interval))
			Log.e(TAG, "Unable to register listener for sensor " + proximitySensor.getName() + ".");
	}

	@Override
	public void stop()
	{
		manager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (event == null || event.values.length == 0)
			return;
		switch (event.sensor.getType()) {
			case Sensor.TYPE_LIGHT:
				collectLuminosity(event.timestamp, event.values[0]);
				break;
			case Sensor.TYPE_PROXIMITY:
				collectProximity(event.values[0]);
				break;
			default:
				Log.w(TAG, "Received sensor update from invalid sensor: " + event.sensor.getName() + ".");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	public static String[] getRequiredPermissions()
	{
		return new String[0];
	}
}
