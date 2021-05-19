package it.unipi.dii.iodetectiontest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

import it.unipi.dii.iodetectionlib.IODetectionListener;
import it.unipi.dii.iodetectionlib.IODetectionResult;
import it.unipi.dii.iodetectionlib.IODetector;

public class IODetectionService extends Service implements IODetectionListener
{

	private static final String TAG = IODetectionService.class.getName();
	private static final long WAKELOCK_TIMEOUT = 2 * 60 * 1000;
	private static final long DETECT_INTERVAL = 5 * 1000;
	public static String IODetectionAction = "it.unipi.dii.iodetectiontest.detection_result";
	Handler periodicHandler = null;
	Runnable periodicRunnable = null;
	IODetector detector = null;

	private PowerManager.WakeLock mWakeLock;

	@Override
	public void onCreate()
	{
		super.onCreate();
		try {
			detector = new IODetector(getApplicationContext());
		} catch (IOException ex) {
			Log.e(TAG, "Can not initialize IODetector: " + ex.getMessage());
			stopSelf();
			return;
		}
		startForeground(NotificationCreator.getNotificationId(), NotificationCreator.getNotification(this));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		periodicHandler = new Handler(Looper.getMainLooper());
		periodicRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				periodicHandler.postDelayed(this, WAKELOCK_TIMEOUT - 5*1000);
				if (mWakeLock.isHeld())
					mWakeLock.release();
				mWakeLock.acquire(WAKELOCK_TIMEOUT);
			}
		};
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire(WAKELOCK_TIMEOUT);
		periodicHandler.postDelayed(periodicRunnable, WAKELOCK_TIMEOUT - 5*1000);
		detector.registerIODetectionListener(this, DETECT_INTERVAL);
		return START_STICKY;
	}


	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onDestroy()
	{
		if (periodicHandler != null && periodicRunnable != null)
			periodicHandler.removeCallbacks(periodicRunnable);
		if (detector != null)
			detector.close();
		if (mWakeLock != null && mWakeLock.isHeld())
			mWakeLock.release();
		super.onDestroy();
	}

	@Override
	public void onDetectionChange(IODetectionResult result)
	{

		Intent i = new Intent(IODetectionAction);
		i.putExtra("iostatus", result.getIOStatus());
		i.putExtra("confidence", result.getConfidence());
		i.putExtra("validated_iostatus", result.getValidatedIOStatus());
		i.putExtra("validated_confidence", result.getValidatedConfidence());
		getApplicationContext().sendBroadcast(i);
	}
}
