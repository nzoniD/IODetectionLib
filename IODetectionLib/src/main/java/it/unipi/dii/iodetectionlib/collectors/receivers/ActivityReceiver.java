package it.unipi.dii.iodetectionlib.collectors.receivers;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import it.unipi.dii.iodetectionlib.collectors.receivers.interfaces.OnActivityUpdateListener;

/* Receives user activity updates from Android */
public class ActivityReceiver extends IntentService
{
	private static OnActivityUpdateListener callbackListener;
	private int lastActivity = DetectedActivity.UNKNOWN;
	private final static String TAG = ActivityReceiver.class.getName();

	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 */
	public ActivityReceiver()
	{
		super(TAG);
	}

	public static void setCallbackListener(OnActivityUpdateListener callbackListener)
	{
		ActivityReceiver.callbackListener = callbackListener;
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent)
	{
		if(intent == null || callbackListener == null)
			return;
		ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
		if (result == null)
			return;
		List<DetectedActivity> activities = result.getProbableActivities();
		if (!activities.isEmpty()) {
			int activity = activities.get(0).getType();
			if (activity != lastActivity) {
				lastActivity = activity;
				/* Callback to the collector if activity has changed */
				callbackListener.onActivityUpdate(activity);
			}
		}
	}
}
