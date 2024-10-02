package com.swasthavyas.emergencyllp.util.location;

import android.location.Location;

import io.reactivex.rxjava3.core.Observable;

public interface LocationClient {
    Observable<Location> getLocationUpdates(long interval);
}
