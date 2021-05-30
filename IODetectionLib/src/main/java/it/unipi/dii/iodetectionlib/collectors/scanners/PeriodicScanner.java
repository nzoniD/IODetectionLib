package it.unipi.dii.iodetectionlib.collectors.scanners;

import android.os.Handler;
import android.os.Looper;

/* Base class for a periodic scanner */
public abstract class PeriodicScanner
{
	private final Handler handler;
	private final Runnable runnable;
	private final long interval;

	public PeriodicScanner(long interval)
	{
		this.interval = interval;
		handler = new Handler(Looper.getMainLooper());
		runnable = new Runnable()
		{
			@Override
			public void run()
			{
				handler.postDelayed(this, interval);
				scan();
			}
		};
	}

	public void start(boolean now)
	{
		if (now)
			handler.post(runnable);
		else
			handler.postDelayed(runnable, interval);
	}

	public void start()
	{
		start(false);
	}

	public void startNow()
	{
		start(true);
	}

	public void stop()
	{
		handler.removeCallbacks(runnable);
	}

	/* Called periodically */
	abstract void scan();
}
