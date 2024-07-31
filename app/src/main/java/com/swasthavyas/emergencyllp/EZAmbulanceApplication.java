package com.swasthavyas.emergencyllp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;


public class EZAmbulanceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("location-track", "location-track", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationChannel navigationChannel = new NotificationChannel("NAVIGATION_NOTIFICATION_CHANNEL", "NAVIGATION_NOTIFICATION_CHANNEL", NotificationManager.IMPORTANCE_HIGH);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            manager.createNotificationChannel(navigationChannel);
        }
    }
}
