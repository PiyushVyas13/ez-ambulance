package com.swasthavyas.emergencyllp.component.dashboard.driver.worker;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.AggregateField;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

import java.util.Date;

public class FetchDriverEarningWorker extends ListenableWorkerAdapter {

    public FetchDriverEarningWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String driverId = getInputData().getString("driver_id");

        if(driverId == null) {
            callback.onFailure(new IllegalArgumentException("driver id not provided"));
            Log.d(TAG, "doAsyncBackgroundTask: " + driverId);
            return;
        }

        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        dbInstance
                .collection("trip_history")
                .whereEqualTo("trip.assignedDriverId", driverId)
                .whereEqualTo("terminalState", TripStatus.COMPLETED)
                .aggregate(AggregateField.sum("trip.price"))
                .get(AggregateSource.SERVER)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Object result = task.getResult().get(AggregateField.sum("trip.price"));

                        if(result instanceof Long) result = (long)result + 0.0;
                        Log.d(TAG, "doAsyncBackgroundTask: " + result);
                        callback.onSuccess(new Data.Builder().putDouble("total_earning", (double)result).build());
                    }
                    else {
                        Log.d(TAG, "doAsyncBackgroundTask: " + task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }
}
