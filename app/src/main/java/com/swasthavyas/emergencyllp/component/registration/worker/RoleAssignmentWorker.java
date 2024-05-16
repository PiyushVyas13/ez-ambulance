package com.swasthavyas.emergencyllp.component.registration.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

import java.util.HashMap;
import java.util.Map;

public class RoleAssignmentWorker extends ListenableWorkerAdapter {


    public RoleAssignmentWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {

        try {

            FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();
            String role = getInputData().getString("role");
            String uid = getInputData().getString("userId");

            if(role == null || uid == null) {
                throw new IllegalArgumentException("role or userId not provided");
            }

            Map<String, Object> inputData = new HashMap<>();

            inputData.put("role", role);

            dbInstance.collection("user_roles")
                    .document(uid)
                    .set(inputData)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            callback.onSuccess(new Data.Builder()
                                    .putString("userId", uid)
                                    .putString("role", role)
                                    .build());
                        }
                        else {
                            callback.onFailure(task.getException());
                        }
                    });


        }catch (Exception e) {
            callback.onFailure(e);
        }


    }
}
