package it.unipi.dii.iodetectionlib.collectors.ml;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureVector
{
	private static class TimeRange
	{
		private final FeatureId featureId;
		private final int start;
		private final int end;

		TimeRange(FeatureId featureId, int start, int end)
		{
			this.featureId = featureId;
			this.start = start;
			this.end = end;
		}

		TimeRange(FeatureId featureId, int startH, int startM, int endH, int endM)
		{
			this(featureId, startH*60 + startM, endH*60 + endM);
		}

		FeatureId getFeatureId()
		{
			return featureId;
		}

		boolean isInRange(int value)
		{
			return value >= start && value <= end;
		}

		boolean isInRange(int h, int m)
		{
			return isInRange(h*60 + m);
		}

		boolean isNowInRange()
		{
			Calendar now = Calendar.getInstance();
			int h = now.get(Calendar.HOUR_OF_DAY);
			int m = now.get(Calendar.MINUTE);
			return isInRange(h, m);
		}
	}

	private static final String TAG = FeatureVector.class.getName();
	private static final FeatureId[] DAYFRAME_FEATURE_IDS = {FeatureId.DAYLIGHT, FeatureId.TWILIGHT, FeatureId.NIGHT};
	private static final List<TimeRange> TIME_RANGES = Arrays.asList(
		new TimeRange(FeatureId.NIGHT, 0, 0, 5, 23),
		new TimeRange(FeatureId.TWILIGHT, 5, 24, 5, 56),
		new TimeRange(FeatureId.DAYLIGHT, 5, 57, 20, 33),
		new TimeRange(FeatureId.TWILIGHT, 20, 34, 21, 6),
		new TimeRange(FeatureId.NIGHT, 21, 7, 24, 0)
	);

	private final HashMap<FeatureId, Feature> features;

	public FeatureVector()
	{
		features = new HashMap<>(FeatureId.values().length, 1);
	}

	public void add(Feature feature)
	{
		features.put(feature.getFeatureId(), feature);
		Log.d(TAG, "ADDED FEATURE: " + feature);
	}

	private static FeatureId getCurrentDayframeFeatureId()
	{
		for (TimeRange range: TIME_RANGES)
			if (range.isNowInRange())
				return range.getFeatureId();
		return null;
	}

	public Map<FeatureId, Feature> getFeatureMap()
	{
		addDayframeFeatures();
		return features;
	}

	public void addDayframeFeatures()
	{
		FeatureId curId = getCurrentDayframeFeatureId();
		for (FeatureId id: DAYFRAME_FEATURE_IDS)
			add(new Feature(id, id == curId ? 1.0f : 0.0f));
	}

	public boolean hasRequiredFeatures()
	{
		addDayframeFeatures();
		return features.containsKey(FeatureId.LUMINOSITY) && features.containsKey(FeatureId.PROXIMITY);
	}

	public float[] getFloatVector()
	{
		if (!hasRequiredFeatures())
			throw new IllegalStateException("Required features not still available.");
		float[] vector = new float[FeatureId.values().length];
		int index = 0;
		for (FeatureId id: FeatureId.values()) {
			Feature feature = features.get(id);
			float value = 0.0f;
			if (feature != null)
				value = feature.getValue();
			else
				Log.i(TAG, "Feature " + id.name() + " not available. Forcing to 0.");
			if (!Float.isFinite(value)) {
				Log.w(TAG, "Non finite value (" + value + ") in normalized vector for feature " + id.name() + ". Forcing to 0.5.");
				value = 0.5f;
			}
			vector[index] = value;
			index++;
		}
		return vector;
	}

	public ByteBuffer toByteBuffer()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(FeatureId.values().length * (Float.SIZE/8));
		float[] vector = getFloatVector();
		for (float value: vector)
			byteBuffer.putFloat(value);
		return byteBuffer;
	}
}
