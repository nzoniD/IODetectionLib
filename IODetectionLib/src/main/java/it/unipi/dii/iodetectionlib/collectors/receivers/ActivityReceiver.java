package it.unipi.dii.iodetectionlib.collectors.receivers;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import it.unipi.dii.iodetectionlib.collectors.receivers.interfaces.OnActivityUpdateListener;

public class ActivityReceiver extends JobIntentService
{
	private static OnActivityUpdateListener callbackListener;
	private int lastActivity = DetectedActivity.UNKNOWN;

	public static void setCallbackListener(OnActivityUpdateListener callbackListener)
	{
		ActivityReceiver.callbackListener = callbackListener;
	}

	@Override
	protected void onHandleWork(@NonNull Intent intent)
	{
		if (callbackListener == null)
			return;
		ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
		if (result == null)
			return;
		List<DetectedActivity> activities = result.getProbableActivities();
		if (!activities.isEmpty()) {
			int activity = activities.get(0).getType();
			if (activity != lastActivity) {
				lastActivity = activity;
				callbackListener.onActivityUpdate(activity);
			}
		}
	}
}
