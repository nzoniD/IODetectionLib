package it.unipi.dii.iodetectionlib.collectors.ml;

class FeatureMetadata
{
	//TODO
	public static final int MAX_WIFI_ACCESS_POINTS = 100;
	public static final int MAX_BLUETOOTH_DEVICES = 100;
	public static final int MAX_GPS_SATELLITES = 100;
	public static final int MAX_GPS_FIX_SATELLITES = 100;
	public static final int MAX_GPS_TIME_FROM_FIX = 1000000;
	public static final int MAX_TIME_FROM_LAST_FAR = 1000000;
	public static final int MAX_LUMINOSITY = 32767;
	public static final int MAX_LUMINOSITY30S = 32767;
	public static final int MAX_LAST_LUMINOSITY30S_WHEN_FAR = 32767;
	public static final int MAX_LAST_LUMINOSITY_WHEN_FAR = 32767;

	public static int getMaxValue(FeatureId featureId)
	{
		switch (featureId) {
			case LUMINOSITY:
				return MAX_LUMINOSITY;
			case LUMINOSITY30S:
				return MAX_LUMINOSITY30S;
			case LAST_LUMINOSITY_WHEN_FAR:
				return MAX_LAST_LUMINOSITY_WHEN_FAR;
			case LAST_LUMINOSITY30S_WHEN_FAR:
				return MAX_LAST_LUMINOSITY30S_WHEN_FAR;
			case TIME_FROM_LAST_FAR:
				return MAX_TIME_FROM_LAST_FAR;
			case WIFI_ACCESS_POINTS:
				return MAX_WIFI_ACCESS_POINTS;
			case BLUETOOTH_DEVICES:
				return MAX_BLUETOOTH_DEVICES;
			case GPS_SATELLITES:
				return MAX_GPS_SATELLITES;
			case GPS_FIX_SATELLITES:
				return MAX_GPS_FIX_SATELLITES;
			case GPS_TIME_FROM_FIX:
				return MAX_GPS_TIME_FROM_FIX;
			default:
				throw new IllegalArgumentException("Requested max value for feature " + featureId + " which does not requires normalization.");
		}
	}
}
