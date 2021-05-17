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

import it.unipi.dii.iodetectionlib.IODetection;

public class IODetectionService extends Service {

    private static final String TAG = IODetectionService.class.getName();
    private static final long WAKELOCK_TIMEOUT = 2*60*1000;
    public static String IODetectionAction = "it.unipi.dii.iodetectionlib.detect_io";
    private static final long PERIODIC_DELAY = 5000;
    Handler periodicHandler = null;
    Runnable periodicRunnable = null;
    IODetection ioDetection = null;

    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NotificationCreator.getNotificationId(),NotificationCreator.getNotification(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ioDetection = new IODetection(getApplicationContext());
        /*-------------------------------------------*/
        periodicHandler = new Handler(Looper.getMainLooper());
        periodicRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                periodicHandler.postDelayed(this, PERIODIC_DELAY);
                boolean indoor = ioDetection.get();
                Intent i = new Intent(IODetectionAction);
                i.putExtra("indoor",indoor);
                Log.d(TAG, "SEND BROADCAST");
                getApplicationContext().sendBroadcast(i);
                if (!mWakeLock.isHeld())
                    mWakeLock.acquire(WAKELOCK_TIMEOUT);
            }
        };
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire(WAKELOCK_TIMEOUT);
        periodicHandler.postDelayed(periodicRunnable, 1);
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();
        ioDetection.stop();
        if (periodicHandler != null && periodicRunnable != null)
            periodicHandler.removeCallbacks(periodicRunnable);
        super.onDestroy();
    }
}
