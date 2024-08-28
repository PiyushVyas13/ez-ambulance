package com.swasthavyas.emergencyllp.component.dashboard.driver.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

public class FetchRideCountWorker extends ListenableWorkerAdapter {

    public FetchRideCountWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String driverId = getInputData().getString("driver_id");

        if(driverId == null) {
            callback.onFailure(new IllegalArgumentException("Driver Id not provided"));
            return;
        }

        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        dbInstance
                .collection("trip_history")
                .whereEqualTo("trip.assignedDriverId", driverId)
                .count()
                .get(AggregateSource.SERVER)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        long count = task.getResult().getCount();
                        callback.onSuccess(new Data.Builder().putLong("ride_count", count).putString("driver_id", driverId).build());
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }
}
