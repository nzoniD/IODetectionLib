package it.unipi.dii.iodetectionlib.collectors;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemClock;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unipi.dii.iodetectionlib.collectors.ml.Feature;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureId;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureVector;

public abstract class FeatureCollector
{
	protected final Context context;
	private final FeatureVector featureVector;
	protected final long interval;
	private final HashMap<Long, Float> lastLuminosity30s;
	private final HashMap<Long, Float> lastLuminosity30sFar;
	private float lastLuminosityFar = Float.NaN;
	private boolean isFar;
	private long lastFarTime = 0;
	private long lastFixTime = 0;

	protected FeatureCollector(Context context, long interval, FeatureVector featureVector)
	{
		this.context = context;
		this.interval = interval;
		this.featureVector = featureVector;
		this.lastLuminosity30s = new HashMap<>();
		this.lastLuminosity30sFar = new HashMap<>();
	}

	protected void collect(Feature feature)
	{
		featureVector.add(new Feature(FeatureId.TIME_FROM_LAST_FAR, (float)(System.currentTimeMillis() - lastFarTime)/1000));
		featureVector.add(new Feature(FeatureId.GPS_TIME_FROM_FIX, (float)(System.currentTimeMillis() - lastFixTime)/1000));
		featureVector.add(feature);
	}

	private void historyCleanup()
	{
		Map<Long, Float> map = isFar ? lastLuminosity30sFar : lastLuminosity30s;
		List<Long> toRemove = new ArrayList<>();
		for (Map.Entry<Long, Float> entry: map.entrySet())
			if (entry.getKey() < System.currentTimeMillis() - 30*1000)
				toRemove.add(entry.getKey());
		for (Long key: toRemove)
			map.remove(key);
	}

	private static float getMean(Map<Long, Float> map)
	{
		if (map.size() == 0)
			return Float.NaN;
		float sum = 0;
		for (Float value: map.values())
			sum += value;
		return sum / (float)map.size();
	}

	protected void collectLuminosity(long timestamp, float luminosity)
	{
		timestamp = System.currentTimeMillis() - (SystemClock.elapsedRealtimeNanos() - timestamp)/(1000*1000);
		lastLuminosity30s.put(timestamp, luminosity);
		if (isFar) {
			lastLuminosity30sFar.put(timestamp, luminosity);
			lastLuminosityFar = luminosity;
			lastFarTime = timestamp;
		}
		historyCleanup();
		collect(new Feature(FeatureId.LUMINOSITY, luminosity));
		if (!Float.isNaN(lastLuminosityFar))
			collect(new Feature(FeatureId.LAST_LUMINOSITY_WHEN_FAR, lastLuminosityFar));
		float mean = getMean(lastLuminosity30s);
		if (!Float.isNaN(mean))
			collect(new Feature(FeatureId.LUMINOSITY30S, mean));
		mean = getMean(lastLuminosity30sFar);
		if (Float.isNaN(mean)) {
			if (!Float.isNaN(lastLuminosityFar))
				collect(new Feature(FeatureId.LAST_LUMINOSITY30S_WHEN_FAR, lastLuminosityFar));
		} else {
			collect(new Feature(FeatureId.LAST_LUMINOSITY30S_WHEN_FAR, mean));
		}
	}

	protected void collectProximity(float proximity)
	{
		if (proximity > 0.1)
			proximity = 1.0f;
		isFar = proximity > 0.5f;
		collect(new Feature(FeatureId.PROXIMITY, proximity));
	}

	protected void setLastGpsFix(long lastFixTime)
	{
		this.lastFixTime = lastFixTime;
	}

	abstract public void start();
	abstract public void stop();

	protected boolean checkPermission(String permission)
	{
		return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
	}

	protected void assertPermissions(String[] permissions)
	{
		for (String permission: permissions)
			if (!checkPermission(permission))
				throw new RuntimeException("Collector '" + this.getClass().getName() + "' started but permission '" + permission + "' not granted.");
	}
}
