package it.unipi.dii.iodetectionlib.collectors.receivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import it.unipi.dii.iodetectionlib.collectors.receivers.interfaces.OnCounterUpdateListener;

public class BluetoothCounterReceiver extends CounterReceiver
{
	private static final String TAG = BluetoothCounterReceiver.class.getName();
	private int tmpCounter;

	public BluetoothCounterReceiver(OnCounterUpdateListener callbackListener)
	{
		super(callbackListener);
		tmpCounter = 0;
	}

	@Override
	public int getCounter(Context context, Intent intent)
	{
		String action = intent.getAction();
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			Log.d(TAG, "Found a new bluetooth device.");
			tmpCounter++;
		} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
			int current = tmpCounter;
			tmpCounter = 0;
			return current;
		}
		return lastValue;
	}
}
