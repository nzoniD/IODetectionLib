package it.unipi.dii.iodetectionlib.collectors;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

import it.unipi.dii.iodetectionlib.collectors.ml.Feature;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureId;
import it.unipi.dii.iodetectionlib.collectors.ml.FeatureVector;
import it.unipi.dii.iodetectionlib.collectors.receivers.ActivityReceiver;
import it.unipi.dii.iodetectionlib.collectors.receivers.interfaces.OnActivityUpdateListener;

public class ActivityCollector extends FeatureCollector implements OnActivityUpdateListener
{
	private static final String TAG = ActivityCollector.class.getName();
	private static final FeatureId[] ACTIVITY_FEATURE_IDS = {
		FeatureId.IN_VEHICLE, FeatureId.ON_BICYCLE, FeatureId.ON_FOOT, FeatureId.STILL,
		FeatureId.TILTING, FeatureId.WALKING, FeatureId.RUNNING
	};

	private final ActivityRecognitionClient recognitionClient;
	private final PendingIntent pendingIntent;

	public ActivityCollector(Context context, long collectInterval, FeatureVector featureVector)
	{
		super(context, collectInterval, featureVector);
		recognitionClient = new ActivityRecognitionClient(this.context);
		ActivityReceiver.setCallbackListener(this);
		Intent intent = new Intent(context, ActivityReceiver.class);
		pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void start()
	{
		assertPermissions(getRequiredPermissions());
		Task<Void> task = recognitionClient.requestActivityUpdates(this.interval, pendingIntent); // FIXME: not working !! ActivityReceiver not called
		task.addOnSuccessListener(result -> {});
		task.addOnFailureListener(e -> Log.w(TAG, "Activity Recognition Client failure: " + e.toString()));
	}

	@Override
	public void stop()
	{
		recognitionClient.removeActivityUpdates(pendingIntent);
	}

	@Override
	public void onActivityUpdate(int activity)
	{
		FeatureId curId = FeatureId.fromDetectedActivity(activity);
		for (FeatureId id: ACTIVITY_FEATURE_IDS)
			collect(new Feature(id, id == curId ? 1.0f : 0.0f));

	}

	public static String[] getRequiredPermissions()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
			return new String[]{Manifest.permission.ACTIVITY_RECOGNITION};
		return new String[0];
	}
}
