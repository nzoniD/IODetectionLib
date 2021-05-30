package it.unipi.dii.iodetectionlib.collectors.receivers;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import androidx.annotation.NonNull;

/* Dummy class used to periodically receive Location updates.
 * This is needed in order to force Android to periodically update the number of satellites in view.
 */
public class LocationReceiver implements LocationListener
{
	@Override
	public void onLocationChanged(@NonNull Location location)
	{
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}

	@Override
	public void onProviderEnabled(@NonNull String provider)
	{
	}

	@Override
	public void onProviderDisabled(@NonNull String provider)
	{
	}
}
