package com.swasthavyas.emergencyllp.component.trip.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.database.FirebaseDatabase;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

public class RemoveTripWorker extends ListenableWorkerAdapter {

    public RemoveTripWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String ownerId = getInputData().getString("owner_id");
        String tripId = getInputData().getString("trip_id");

        if(ownerId == null || tripId == null) {
            callback.onFailure(new IllegalArgumentException("ownerId or tripId is null"));
            return;
        }

        FirebaseDatabase database = FirebaseService.getInstance().getDatabaseInstance();

        database
                .getReference()
                .getRoot()
                .child("trips")
                .child(ownerId)
                .child(tripId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }
}
