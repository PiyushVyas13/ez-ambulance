package com.swasthavyas.emergencyllp.component.trip.worker;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.content.Context;
import android.util.Log;

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
        String terminalState = getInputData().getString("terminal_state");

        if(ownerId == null || tripId == null) {
            Log.d(TAG, "doAsyncBackgroundTask: " + ownerId + tripId + terminalState);
            callback.onFailure(new IllegalArgumentException("ownerId or tripId or terminal state is null"));
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
