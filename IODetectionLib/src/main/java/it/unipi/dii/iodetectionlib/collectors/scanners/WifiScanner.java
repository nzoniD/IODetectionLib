package it.unipi.dii.iodetectionlib.collectors.scanners;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiScanner extends PeriodicScanner
{
	private static final String TAG = WifiScanner.class.getName();
	private final WifiManager manager;

	public WifiScanner(Context context, long interval)
	{
		super(interval);
		manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	}

	@Override
	void scan()
	{
		if (!manager.isWifiEnabled())
			Log.w(TAG, "WiFi is not enabled.");
		else if (!manager.startScan())
			Log.e(TAG, "Failed to start WiFi scan.");
	}
}
