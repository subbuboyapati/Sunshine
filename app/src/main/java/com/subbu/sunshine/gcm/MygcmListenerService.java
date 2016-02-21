package com.subbu.sunshine.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;
import com.subbu.sunshine.MainActivity;
import com.subbu.sunshine.R;

/**
 * Created by subrahmanyam on 20-02-2016, 07:45 AM.
 */
public class MygcmListenerService extends GcmListenerService {
    public static final int NOTIFICATION_ID = 1;
    private static final String EXTRA_DATA = "data";
    private static final String EXTRA_WEATHER = "weather";
    private static final String EXTRA_LOCATION = "location";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (!data.isEmpty()) {
            String gcmSenderId = getString(R.string.gcm_defaultSenderId);
            if (gcmSenderId.length() == 0) {
                Toast.makeText(this, "Sender id need to set", Toast.LENGTH_SHORT).show();
            }
            if (gcmSenderId.equals(from)) {
                String weather = data.getString(EXTRA_WEATHER);
                String location = data.getString(EXTRA_LOCATION);
                String alert = String.format(getString(R.string.gcm_weather_alert), weather, location);
                sendNotification(alert);
            }
        }
    }

    private void sendNotification(String message) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.art_storm);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.art_clear)
                        .setLargeIcon(largeIcon)
                        .setContentTitle("Weather Alert!")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
