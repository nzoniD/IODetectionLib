package it.unipi.dii.iodetectionlib.collectors.scanners;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

/* Periodically scan for BT devices */
public class BluetoothScanner extends PeriodicScanner
{
	private static final String TAG = BluetoothScanner.class.getName();
	private final BluetoothAdapter adapter;

	public BluetoothScanner(long interval)
	{
		super(interval);
		adapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	void scan()
	{
		if (!adapter.isEnabled())
			Log.w(TAG, "Bluetooth is not enabled.");
		else if (!adapter.isDiscovering())
			if (!adapter.startDiscovery())
				Log.e(TAG, "Failed to start bluetooth discovery.");
	}
}
