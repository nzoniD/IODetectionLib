package it.unipi.dii.iodetectionlib.collectors;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import it.unipi.dii.iodetectionlib.collectors.ml.Feature;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureId;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureVector;
import it.unipi.dii.iodetectionlib.collectors.receivers.WifiCounterReceiver;
import it.unipi.dii.iodetectionlib.collectors.receivers.interfaces.OnCounterUpdateListener;
import it.unipi.dii.iodetectionlib.collectors.scanners.WifiScanner;

public class WifiCollector extends FeatureCollector implements OnCounterUpdateListener
{
	private final WifiScanner scanner;
	private final BroadcastReceiver wifiReceiver;
	public static final String[] REQUIRED_PERMISSIONS = {
		Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE
	};

	public WifiCollector(Context context, long scanInterval, FeatureVector featureVector)
	{
		super(context, scanInterval, featureVector);
		scanner = new WifiScanner(context, this.interval);
		wifiReceiver = new WifiCounterReceiver(this);
	}

	@Override
	public void start()
	{
		assertPermissions(getRequiredPermissions());
		IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		context.registerReceiver(wifiReceiver, intentFilter);
		scanner.startNow();
	}

	@Override
	public void stop()
	{
		scanner.stop();
		context.unregisterReceiver(wifiReceiver);
	}

	@Override
	public void onCounterUpdate(int value)
	{
		collect(new Feature(FeatureId.WIFI_ACCESS_POINTS, (float)value));
	}

	public static String[] getRequiredPermissions()
	{
		return REQUIRED_PERMISSIONS;
	}
}
