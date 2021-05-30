package it.unipi.dii.iodetectionlib.collectors.ml;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* A feature input vector for the TFLite model. */
public class FeatureVector
{
	/* Represents a time range with an associated FeatureId: DAYLIGHT, TWILIGHT, NIGHT */
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
	private final Metadata metadata;

	public FeatureVector(Metadata metadata)
	{
		this.metadata = metadata;
		features = new HashMap<>(FeatureId.values().length, 1);
	}

	/* Adds or updates a feature in the vector */
	public void add(Feature feature)
	{
		features.put(feature.getFeatureId(), feature);
		Log.d(TAG, "ADDED FEATURE: " + feature);
	}

	/* Returns the current FeatureId for the current time of day */
	private static FeatureId getCurrentDayframeFeatureId()
	{
		for (TimeRange range: TIME_RANGES)
			if (range.isNowInRange())
				return range.getFeatureId();
		return null;
	}

	/* Returns the feature vector */
	public Map<FeatureId, Feature> getFeatureMap()
	{
		addDayframeFeatures();
		return features;
	}

	/* Adds the DAYLIGHT,TWILIGHT,NIGHT features, based on current time */
	public void addDayframeFeatures()
	{
		FeatureId curId = getCurrentDayframeFeatureId();
		for (FeatureId id: DAYFRAME_FEATURE_IDS)
			add(new Feature(id, id == curId ? 1.0f : 0.0f));
	}

	/* Checks if there are at least the most important features to run the TFLite model */
	public boolean hasRequiredFeatures()
	{
		addDayframeFeatures();
		return features.containsKey(FeatureId.LUMINOSITY) && features.containsKey(FeatureId.PROXIMITY);
	}

	/* Returns the feature vector as a float array */
	public float[] getFloatVector()
	{
		if (!hasRequiredFeatures())
			throw new IllegalStateException("Required features not still available.");
		float[] vector = new float[FeatureId.values().length];
		for (FeatureId id: FeatureId.values()) {
			Feature feature = features.get(id);
			float value = metadata.getFeatureVectorMean()[id.ordinal()];
			if (feature != null)
				value = feature.getValue();
			else
				Log.i(TAG, "Feature " + id.name() + " not available. Forcing to mean value (" + value + ").");
			if (!Float.isFinite(value)) {
				Log.w(TAG, "Non finite value (" + value + ") in normalized vector for feature " + id.name() + ". Forcing to 0.5.");
				value = 0.5f;
			}
			vector[id.ordinal()] = value;
		}
		return vector;
	}

	/* Returns the feature vector as a byte buffer */
	public ByteBuffer toByteBuffer()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(FeatureId.values().length * (Float.SIZE/8));
		float[] vector = getFloatVector();
		for (float value: vector)
			byteBuffer.putFloat(value);
		return byteBuffer;
	}
}
