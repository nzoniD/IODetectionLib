package it.unipi.dii.iodetectiontest;

import android.app.Notification;
import android.content.Context;

import androidx.core.app.NotificationCompat;

public class NotificationCreator {

    private static final int NOTIFICATION_ID = 1094;
    private static final String CHANNEL_ID = "Foreground Service Channel";
    private static Notification notification;

    public static Notification getNotification(Context context) {

        if(notification == null) {

            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Try IO Detection")
                    .setContentText("IO Detection Running")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
        }

        return notification;
    }

    public static int getNotificationId() {
        return NOTIFICATION_ID;
    }
}