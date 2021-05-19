package it.unipi.dii.iodetectionlib.collectors;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GnssStatus;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import it.unipi.dii.iodetectionlib.collectors.ml.Feature;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureId;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureVector;
import it.unipi.dii.iodetectionlib.collectors.receivers.GpsListenerCallback;
import it.unipi.dii.iodetectionlib.collectors.receivers.LocationReceiver;
import it.unipi.dii.iodetectionlib.collectors.receivers.interfaces.OnGpsUpdateListener;

import static android.content.Context.LOCATION_SERVICE;

public class GpsCollector extends FeatureCollector implements OnGpsUpdateListener
{
	private final LocationManager manager;
	private final GnssStatus.Callback callback;
	private final LocationReceiver locationReceiver;

	public GpsCollector(Context context, long interval, FeatureVector featureVector)
	{
		super(context, interval, featureVector);
		manager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
		callback = new GpsListenerCallback(this);
		locationReceiver = new LocationReceiver();
	}

	@SuppressLint("MissingPermission")
	@Override
	public void start()
	{
		assertPermissions(getRequiredPermissions());
		manager.registerGnssStatusCallback(callback, new Handler(Looper.getMainLooper()));
		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, locationReceiver);
	}

	@Override
	public void stop()
	{
		manager.removeUpdates(locationReceiver);
		manager.unregisterGnssStatusCallback(callback);
	}

	@Override
	public void onGpsUpdate(int satellites, long lastFixMillis, int fixSatellites)
	{
		setLastGpsFix(lastFixMillis);
		collect(new Feature(FeatureId.GPS_SATELLITES, (float)satellites));
		collect(new Feature(FeatureId.GPS_FIX_SATELLITES, (float)fixSatellites));
	}

	public static String[] getRequiredPermissions()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
		return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
	}
}
