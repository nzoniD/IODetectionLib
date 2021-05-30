package it.unipi.dii.iodetectionlib.collectors;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import it.unipi.dii.iodetectionlib.collectors.ml.Feature;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureId;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureVector;
import it.unipi.dii.iodetectionlib.collectors.receivers.BluetoothCounterReceiver;
import it.unipi.dii.iodetectionlib.collectors.receivers.interfaces.OnCounterUpdateListener;
import it.unipi.dii.iodetectionlib.collectors.scanners.BluetoothScanner;

/* Collects the number of BT devices found */
public class BluetoothCollector extends FeatureCollector implements OnCounterUpdateListener
{
	private final BluetoothScanner scanner;
	private final BroadcastReceiver bluetoothReceiver;
	public static final String[] REQUIRED_PERMISSIONS = {
		Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN
	};

	public BluetoothCollector(Context context, long scanInterval, FeatureVector featureVector)
	{
		super(context, scanInterval, featureVector);
		scanner = new BluetoothScanner(this.interval);
		bluetoothReceiver = new BluetoothCounterReceiver(this);
	}

	/* Called by BluetoothCounterReiceiver */
	@Override
	public void onCounterUpdate(int value)
	{
		collect(new Feature(FeatureId.BLUETOOTH_DEVICES, (float)value));
	}

	@Override
	public void start()
	{
		assertPermissions(getRequiredPermissions());
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(bluetoothReceiver, intentFilter);
		scanner.startNow();
	}

	@Override
	public void stop()
	{
		scanner.stop();
		context.unregisterReceiver(bluetoothReceiver);
	}

	public static String[] getRequiredPermissions()
	{
		return REQUIRED_PERMISSIONS;
	}
}
