package com.swasthavyas.emergencyllp.util.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

public class DefaultLocationClient implements LocationClient {

    private final FusedLocationProviderClient locationProviderClient;
    private final Context appContext;

    public DefaultLocationClient(Context context, FusedLocationProviderClient locationProviderClient) {
        this.locationProviderClient = locationProviderClient;
        this.appContext = context;
    }


    @SuppressLint("MissingPermission")
    @Override
    public Observable<Location> getLocationUpdates(long interval) {
        return Observable.create(emitter -> {
            if(!hasLocationPermission()) {
                emitter.onError(new Exception("Missing Location permission"));
                return;
            }

            LocationManager locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);

            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGpsEnabled && !isNetworkEnabled) {
                emitter.onError(new Exception("GPS not enabled."));
                return;
            }

            LocationRequest.Builder builder = new LocationRequest.Builder(interval)
                    .setMinUpdateDistanceMeters(1f)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateIntervalMillis(1500L)
                    .setWaitForAccurateLocation(true);


            LocationRequest locationRequest = builder.build();

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    Location location = locationResult.getLastLocation();

                    if(location != null && !emitter.isDisposed()) {
                        emitter.onNext(location);
                    }
                }
            };

            locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

            emitter.setDisposable(new Disposable() {
                @Override
                public void dispose() {
                    locationProviderClient.removeLocationUpdates(locationCallback);
                }

                @Override
                public boolean isDisposed() {
                    return false;
                }
            });
        });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
