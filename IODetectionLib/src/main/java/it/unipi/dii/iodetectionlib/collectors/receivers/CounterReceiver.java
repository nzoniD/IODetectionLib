package it.unipi.dii.iodetectionlib.collectors.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.unipi.dii.iodetectionlib.collectors.receivers.interfaces.OnCounterUpdateListener;

public abstract class CounterReceiver extends BroadcastReceiver
{
	private final OnCounterUpdateListener callbackListener;
	protected int lastValue = -1;

	public CounterReceiver(OnCounterUpdateListener callbackListener)
	{
		this.callbackListener = callbackListener;
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		int counter = getCounter(context, intent);
		if (counter != lastValue) {
			lastValue = counter;
			callbackListener.onCounterUpdate(counter);
		}
	}

	public abstract int getCounter(Context context, Intent intent);
}
