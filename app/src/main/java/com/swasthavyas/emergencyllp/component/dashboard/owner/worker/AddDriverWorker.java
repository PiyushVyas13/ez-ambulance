package com.swasthavyas.emergencyllp.component.dashboard.owner.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;


import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.types.UserRole;

import java.util.HashMap;
import java.util.Map;

public class AddDriverWorker extends ListenableWorkerAdapter {


    public AddDriverWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String driverId = getInputData().getString("driver_id");
        String userId = getInputData().getString("user_id");
        String ownerId = getInputData().getString("owner_id");
        String phoneNumber = getInputData().getString("phone_number");
        String name = getInputData().getString("name");
        String password = getInputData().getString("password");
        String email = getInputData().getString("email");
        int age = getInputData().getInt("age", -1);

        if(driverId == null || userId == null || age == -1 || ownerId == null || phoneNumber == null || name == null || password == null || email == null) {
            Log.d("MYAPP", "doAsyncBackgroundTask: " + getInputData());
            callback.onFailure(new IllegalArgumentException("one of the input data is null/invalid."));
            return;
        }

        FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();

        Map<String, Object> inputData = new HashMap<>();

        inputData.put("user_id", userId);
        inputData.put("age", age);
        inputData.put("phone_number", phoneNumber);
        inputData.put("name", name);
        inputData.put("driver_id", driverId);

        Map<String, Object> roleMap = new HashMap<>();

        roleMap.put("role", UserRole.EMPLOYEE_DRIVER.name().toLowerCase());

        dbInstance.collection("user_roles")
                .document(userId)
                .set(roleMap);

        dbInstance.collection("owners")
                .document(ownerId)
                .collection("employees")
                .document(email)
                .set(inputData)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {

                        Data opData = new Data.Builder()
                                .putString("name", name)
                                .putString("phone_number", phoneNumber)
                                .putInt("age", age)
                                .putString("driverId", driverId)
                                .putString("userId", userId)
                                .putString("password", password)
                                .putString("email", email)
                                .build();

                       callback.onSuccess(opData);
                    }
                    else {
                        callback.onFailure(task.getException());
                    }
                });
    }
}
