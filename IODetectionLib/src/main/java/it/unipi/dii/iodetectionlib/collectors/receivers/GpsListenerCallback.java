package it.unipi.dii.iodetectionlib.collectors.receivers;

import android.location.GnssStatus;

import it.unipi.dii.iodetectionlib.collectors.receivers.interfaces.OnGpsUpdateListener;

/* Receives GnssStatus updates from Android */
public class GpsListenerCallback extends GnssStatus.Callback
{
	private final OnGpsUpdateListener callbackListener;
	private int lastSatellites;
	private int lastFixSatellites;
	private long lastFix;

	public GpsListenerCallback(OnGpsUpdateListener callbackListener)
	{
		super();
		this.callbackListener = callbackListener;
		lastFix = 0;
		lastSatellites = 0;
		lastFixSatellites = 0;
	}

	@Override
	public void onStarted()
	{

	}

	@Override
	public void onStopped()
	{

	}

	/* Called when a fix occurs */
	@Override
	public void onFirstFix(int ttffMillis)
	{
		lastFix = System.currentTimeMillis();
		callListener();
	}

	/* Called when the number of satellites in view changes */
	@Override
	public void onSatelliteStatusChanged(GnssStatus status)
	{
		int satCount = status.getSatelliteCount();
		int fixCount = 0;
		for (int i = 0; i < satCount; i++)
			if (status.usedInFix(i))
				fixCount++;
		if (satCount == lastSatellites && fixCount == lastFixSatellites)
			return;
		lastSatellites = satCount;
		lastFixSatellites = fixCount;
		callListener(); /* Callback to the collector */
	}

	private void callListener()
	{
		callbackListener.onGpsUpdate(lastSatellites, lastFix, lastFixSatellites);
	}
}
