package it.unipi.dii.iodetectiontest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

public class NotificationCreator
{

	private static final int NOTIFICATION_ID = 1094;
	private static Notification notification;

	public static Notification getNotification(Context context)
	{

		if (notification != null)
			return notification;

		String notificationChannelId = "IO DATA ACQUISITION NOTIFICATION CHANNEL";

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, "IO Detection", NotificationManager.IMPORTANCE_HIGH);
			notificationChannel.setLightColor(Color.RED);
			notificationChannel.setDescription("IO Detection");
			notificationChannel.enableLights(true);
			notificationChannel.enableVibration(true);
			notificationManager.createNotificationChannel(notificationChannel);
		}

		Intent notificationIntent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		Notification.Builder builder;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			builder = new Notification.Builder(context, notificationChannelId);
		else
			builder = new Notification.Builder(context);

		builder.setContentTitle("IODetection");
		builder.setContentText("Detecting IO status...");
		builder.setContentIntent(pendingIntent);
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setPriority(Notification.PRIORITY_HIGH);

		notification = builder.build();
		return notification;
	}

	public static int getNotificationId()
	{
		return NOTIFICATION_ID;
	}
}