package it.unipi.dii.iodetectionlib.collectors.receivers;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import it.unipi.dii.iodetectionlib.collectors.receivers.interfaces.OnCounterUpdateListener;

public class WifiCounterReceiver extends CounterReceiver
{
	private static final String TAG = WifiCounterReceiver.class.getName();

	public WifiCounterReceiver(OnCounterUpdateListener callbackListener)
	{
		super(callbackListener);
	}

	@Override
	public int getCounter(Context context, Intent intent)
	{
		boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
		if (!success) {
			Log.w(TAG, "Can not get WiFi access points number.");
			return lastValue;
		}

		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.getScanResults().size();
	}
}
