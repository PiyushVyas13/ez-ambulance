package com.swasthavyas.emergencyllp.component.dashboard.driver.worker;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

import java.sql.Time;
import java.util.Calendar;

public class FetchRideCountWorker extends ListenableWorkerAdapter {

    public FetchRideCountWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    FirebaseFirestore dbInstance;

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {

        String driverId = getInputData().getString("driver_id");

        if(driverId == null) {
            callback.onFailure(new IllegalArgumentException("Driver Id not provided"));
            return;
        }

        dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        dbInstance
                .collection("trip_history")
                .whereEqualTo("trip.assignedDriverId", driverId)
                .count()
                .get(AggregateSource.SERVER)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        long totalCount = task.getResult().getCount();

                        getLastWeekRideCount(callback, driverId, totalCount);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    private void getLastWeekRideCount(NetworkResultCallback callback, String driverId, long totalCount) {
        Timestamp now = Timestamp.now();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);

        Timestamp sevenDaysAgo = new Timestamp(calendar.getTime());


        dbInstance
                .collection("trip_history")
                .whereEqualTo("trip.assignedDriverId", driverId)
                .whereGreaterThanOrEqualTo("completionTimestamp", sevenDaysAgo)
                .whereLessThanOrEqualTo("completionTimestamp", now)
                .orderBy("completionTimestamp", Query.Direction.DESCENDING)
                .count()
                .get(AggregateSource.SERVER)
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       long lastWeekCount = task.getResult().getCount();

                       callback.onSuccess(new Data.Builder()
                               .putString("driver_id", driverId)
                               .putLong("total_ride_count", totalCount)
                               .putLong("last_week_ride_count", lastWeekCount)
                               .build());
                   } else {
                       Log.d(TAG, "getLastWeekRideCount: " + task.getException());
                       callback.onFailure(task.getException());
                   }
                });

    }
}
