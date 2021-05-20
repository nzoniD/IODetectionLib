package it.unipi.dii.iodetectionlib.collectors.ml;

import com.google.android.gms.location.DetectedActivity;

public enum FeatureId
{
	LUMINOSITY,
	LUMINOSITY30S,
	LAST_LUMINOSITY_WHEN_FAR,
	LAST_LUMINOSITY30S_WHEN_FAR,
	TIME_FROM_LAST_FAR,
	WIFI_ACCESS_POINTS,
	BLUETOOTH_DEVICES,
	GPS_SATELLITES,
	GPS_FIX_SATELLITES,
	GPS_TIME_FROM_FIX,
	PROXIMITY,
	DAYLIGHT,
	TWILIGHT,
	NIGHT,
	IN_VEHICLE,
	ON_BICYCLE,
	ON_FOOT,
	STILL,
	TILTING,
	WALKING,
	RUNNING;

	public static FeatureId fromDetectedActivity(int activity)
	{
		switch (activity) {
			case DetectedActivity.IN_VEHICLE:
				return FeatureId.IN_VEHICLE;
			case DetectedActivity.ON_BICYCLE:
				return FeatureId.ON_BICYCLE;
			case DetectedActivity.ON_FOOT:
				return FeatureId.ON_FOOT;
			case DetectedActivity.STILL:
				return FeatureId.STILL;
			case DetectedActivity.TILTING:
				return FeatureId.TILTING;
			case DetectedActivity.WALKING:
				return FeatureId.WALKING;
			case DetectedActivity.RUNNING:
				return FeatureId.RUNNING;
			case DetectedActivity.UNKNOWN:
			default:
				return null;
		}
	}
}
