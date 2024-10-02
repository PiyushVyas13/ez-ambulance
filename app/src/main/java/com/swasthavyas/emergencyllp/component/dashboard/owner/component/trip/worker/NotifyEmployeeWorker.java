package com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.worker;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class NotifyEmployeeWorker extends ListenableWorkerAdapter {

    public NotifyEmployeeWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String driverUserId = getInputData().getString("driver_uid");
        String tripId = getInputData().getString("trip_id");

        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        if(driverUserId == null) {
            callback.onFailure(new IllegalArgumentException("userId of driver not provided."));
            return;
        }

        if(tripId == null) {
            callback.onFailure(new IllegalArgumentException("Trip ID was not provided."));
            return;
        }



        dbInstance
                .collection("fcm_tokens")
                .document(driverUserId)
                .get()
                .addOnCompleteListener(tokenFetchTask -> {
                    if(tokenFetchTask.isSuccessful()) {

                        DocumentSnapshot snapshot = tokenFetchTask.getResult();

                        if(snapshot.exists()) {
                            String token = snapshot.getString("token");
                            notifyEmployee(token, tripId)
                                    .addOnCompleteListener(task -> {
                                        if(task.isSuccessful()) {
                                            callback.onSuccess(null);
                                        }
                                        else {
                                            Log.d(TAG, "doAsyncBackgroundTask: " + task.getException());
                                            callback.onFailure(task.getException());
                                        }
                                    });

                        }
                        else {
                            Log.d(TAG, "doAsyncBackgroundTask: HELLO WORLD");
                            callback.onFailure(new NoSuchElementException("token for given userId does not exist yet. (tell driver to login first)"));
                        }
                    }
                    else {
                        Log.d(TAG, "doAsyncBackgroundTask: " + tokenFetchTask.getException());
                        callback.onFailure(tokenFetchTask.getException());
                    }
                });
    }

    private Task<String> notifyEmployee(String token, String tripId) {
        FirebaseFunctions functions = FirebaseFunctions.getInstance();
//        functions.useEmulator("10.0.2.2", 5001);

        Map<String, Object> inputMap = new HashMap<>();


        inputMap.putIfAbsent("fcm_token", token);
        inputMap.putIfAbsent("trip_id", tripId);

        return functions
                .getHttpsCallable("notifyDriver")
                .call(inputMap)
                .continueWith(task -> (String) task.getResult().getData());
    }
}
