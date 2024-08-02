package com.swasthavyas.emergencyllp.util.service;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.location.DefaultLocationClient;
import com.swasthavyas.emergencyllp.util.location.LocationClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LocationService extends Service {
    public LocationService() {
    }

    private static final String ACTION_START = "action_start";
    private static final String ACTION_STOP = "action_stop";

    private LocationClient locationClient;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String driverId, driverMail;
    private final FirebaseDatabase database = FirebaseService.getInstance().getDatabaseInstance();

    private static boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = new DefaultLocationClient(getApplicationContext(), LocationServices.getFusedLocationProviderClient(getApplicationContext()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            if(intent.hasExtra("driver_id")) {
                this.driverId = intent.getStringExtra("driver_id");
            }
            if(intent.hasExtra("driver_mail")) {
                this.driverMail = intent.getStringExtra("driver_mail");
            }

            switch (Objects.requireNonNull(action)) {
                case ACTION_START:
                    start();
                    break;
                case ACTION_STOP:
                    stop();
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void start() {
        isRunning = true;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "location")
                .setContentTitle("Tracking Location")
                .setContentText("Location: null")
                .setSmallIcon(R.drawable.ambulance)
                .setOngoing(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Disposable disposable = locationClient.getLocationUpdates(3000L)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        location -> {
                            String lat = Double.toString(location.getLatitude());
                            String lng = Double.toString(location.getLongitude());
                            Notification updatedNotification = notificationBuilder.setContentText("Location: (" + lat + ", " + lng + ")").build();

                            if(notificationManager != null) {
                                notificationManager.notify(1, updatedNotification);
                            }

                            sendLocationToDatabase(location);
                        },
                        Throwable::printStackTrace
                );

        compositeDisposable.add(disposable);

        startForeground(1, notificationBuilder.build());
    }

    private Location currentLocation;

    private void sendLocationToDatabase(Location location) {
        List<Double> coords = new ArrayList<>();
        coords.add(location.getLatitude());
        coords.add(location.getLongitude());

        if(database != null && driverId != null) {
            database
                    .getReference()
                    .getRoot()
                    .child("active_drivers")
                    .child(driverId)
                    .setValue(coords)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "location updated: " + location.toString());
                            currentLocation = location;
                        }
                        else {
                            Log.d(TAG, "sendLocationToDatabase: " + task.getException());
                        }
                    });
        }
    }

    private void stop() {
        stopForeground(true);
        stopSelf();
        compositeDisposable.clear();
        if(this.driverMail != null && currentLocation != null) {
            saveLastLocation(driverMail);
        }
        isRunning = false;
    }

    public static boolean isServiceRunning() {
        return isRunning;
    }

    private void saveLastLocation(String driverMail) {
        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();


        dbInstance
                .collection("employees")
                .document(driverMail)
                .update("last_location", Arrays.asList(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       Log.d(TAG, "saveLastLocation: location saved in firestore");
                   }
                   else {
                       Log.d(TAG, "saveLastLocation: failed to save last location" + task.getException());
                   }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    public static void startService(Context context, String driverId) {
        Intent startIntent = new Intent(context, LocationService.class);
        startIntent.setAction(ACTION_START);
        startIntent.putExtra("driver_id", driverId);
        context.startService(startIntent);
    }

    public static void stopService(Context context, String driverMail) {
        Intent stopIntent = new Intent(context, LocationService.class);
        stopIntent.setAction(ACTION_STOP);
        stopIntent.putExtra("driver_mail", driverMail);
        context.startService(stopIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}