package it.unipi.dii.iodetectionlib;

import java.util.Calendar;

import static com.google.android.gms.location.DetectedActivity.IN_VEHICLE;
import static com.google.android.gms.location.DetectedActivity.ON_BICYCLE;
import static com.google.android.gms.location.DetectedActivity.ON_FOOT;
import static com.google.android.gms.location.DetectedActivity.RUNNING;
import static com.google.android.gms.location.DetectedActivity.STILL;
import static com.google.android.gms.location.DetectedActivity.TILTING;
import static com.google.android.gms.location.DetectedActivity.WALKING;

public class FeatureVector
{
	private float lightIntensity = Float.NaN;
	private float proximity = Float.NaN;
	private float devicesBLT = Float.NaN;
	private float devicesWiFi = Float.NaN;
	private float GPSFixSatellites = Float.NaN;
	private float GPSSatellites = Float.NaN;
	private float In_Vehicle = Float.NaN;
	private float On_Bicycle = Float.NaN;
	private float On_Foot = Float.NaN;
	private float Running = Float.NaN;
	private float Still = Float.NaN;
	private float Tilting = Float.NaN;
	private float Walking = Float.NaN;
	private Long timestampGPSLastFix = null;

	public void resetActivity()
	{
		In_Vehicle = (float) 0.0;
		On_Bicycle = (float) 0.0;
		On_Foot = (float) 0.0;
		Running = (float) 0.0;
		Still = (float) 0.0;
		Tilting = (float) 0.0;
		Walking = (float) 0.0;
	}

	public void setActivity(int type)
	{
		resetActivity();
		switch (type) {
			case IN_VEHICLE:
				In_Vehicle = (float) 1.0;
				break;
			case ON_BICYCLE:
				On_Bicycle = (float) 1.0;
				break;
			case ON_FOOT:
				On_Foot = (float) 1.0;
				break;
			case RUNNING:
				Running = (float) 1.0;
				break;
			case STILL:
				Still = (float) 1.0;
				break;
			case TILTING:
				Tilting = (float) 1.0;
				break;
			case WALKING:
				Walking = (float) 1.0;
				break;
			default:
		}
	}

	public float getGPSFixSatellites()
	{
		return GPSFixSatellites;
	}

	public void setGPSFixSatellites(float GPSFixSatellites)
	{
		this.GPSFixSatellites = GPSFixSatellites;
	}

	public float getLightIntensity()
	{
		return lightIntensity;
	}

	public void setLightIntensity(float lightIntensity)
	{
		this.lightIntensity = lightIntensity;
	}

	public float getProximity()
	{
		return proximity;
	}

	public void setProximity(float proximity)
	{
		if (proximity > 0) {
			this.proximity = (float) 1.0;
		} else {
			this.proximity = (float) 0.0;
		}
	}

	public float getDevicesBLT()
	{
		return devicesBLT;
	}

	public void setDevicesBLT(float devicesBLT)
	{
		this.devicesBLT = devicesBLT;
	}

	public float getDevicesWiFi()
	{
		return devicesWiFi;
	}

	public void setDevicesWiFi(float devicesWiFi)
	{
		this.devicesWiFi = devicesWiFi;
	}

	public float getGPSSatellites()
	{
		return GPSSatellites;
	}

	public void setGPSSatellites(float GPSSatellites)
	{
		this.GPSSatellites = GPSSatellites;
	}

	public Long getTimestampGPSLastFix()
	{
		if (timestampGPSLastFix == null)
			return null;
		return System.currentTimeMillis() - timestampGPSLastFix;
	}

	public void setTimestampGPSLastFix(Long timestampGPSLastFix)
	{
		this.timestampGPSLastFix = timestampGPSLastFix;
	}

	public float isNight()
	{
		Calendar rightNow = Calendar.getInstance();
		int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);
		int currentMinutes = rightNow.get(Calendar.MINUTE);
		int t = currentHourIn24Format * 60 + currentMinutes;
		if ((t > 0 && t < 5 * 60 + 23) || (t > 21 * 60 + 7 && t < 24 * 60)) {
			return (float) 1.0;
		} else {
			return (float) 0.0;
		}
	}

	public float isTwilight()
	{
		Calendar rightNow = Calendar.getInstance();
		int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);
		int currentMinutes = rightNow.get(Calendar.MINUTE);
		int t = currentHourIn24Format * 60 + currentMinutes;
		if ((t > 5 * 60 + 24 && t < 5 * 60 + 56) || (t > 20 * 60 + 34 && t < 21 * 60 + 6)) {
			return (float) 1.0;
		} else {
			return (float) 0.0;
		}
	}

	public float isDaylight()
	{
		Calendar rightNow = Calendar.getInstance();
		int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);
		int currentMinutes = rightNow.get(Calendar.MINUTE);
		int t = currentHourIn24Format * 60 + currentMinutes;
		if (t > 5 * 60 + 57 && t < 20 * 60 + 33) {
			return (float) 1.0;
		} else {
			return (float) 0.0;
		}
	}

	public float[] getFeatureVector()
	{
		float[] result = new float[17];
		if (Float.isNaN(lightIntensity))
			lightIntensity = 0.0f;
		if (Float.isNaN(proximity))
			proximity = 1.0f;
		if (Float.isNaN(devicesBLT))
			devicesBLT = 0.0f;
		if (Float.isNaN(devicesWiFi))
			devicesWiFi = 0.0f;
		if (Float.isNaN(GPSFixSatellites))
			GPSFixSatellites = 0.0f;
		if (Float.isNaN(GPSSatellites))
			GPSSatellites = 0.0f;
		if (Float.isNaN(In_Vehicle))
			resetActivity();
		float lastFix = Float.POSITIVE_INFINITY;
		if (timestampGPSLastFix != null)
			lastFix = (float) getTimestampGPSLastFix();
		result[0] = getDevicesBLT();
		result[1] = lastFix;
		result[2] = getGPSFixSatellites();
		result[3] = getGPSSatellites();
		result[4] = getLightIntensity();
		result[5] = getDevicesWiFi();
		result[6] = isNight();
		result[7] = isTwilight();
		result[8] = isDaylight();
		result[9] = getIn_Vehicle();
		result[10] = getOn_Bicycle();
		result[11] = getOn_Foot();
		result[12] = getStill();
		result[13] = getTilting();
		result[14] = getWalking();
		result[15] = getRunning();
		result[16] = getProximity();
		return result;
	}

	public float getIn_Vehicle()
	{
		return In_Vehicle;
	}

	public float getOn_Bicycle()
	{
		return On_Bicycle;
	}

	public float getOn_Foot()
	{
		return On_Foot;
	}

	public float getRunning()
	{
		return Running;
	}

	public float getStill()
	{
		return Still;
	}

	public float getTilting()
	{
		return Tilting;
	}

	public float getWalking()
	{
		return Walking;
	}
}
