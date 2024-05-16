package com.swasthavyas.emergencyllp.component.registration.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserRegistrationWorker extends ListenableWorkerAdapter {


    public UserRegistrationWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();

        try {
            String uid = UUID.randomUUID().toString();
            String role = getInputData().getString("role");
            String userId = getInputData().getString("userId");

            if(role == null || userId == null) {
                for (String key :
                        getInputData().getKeyValueMap().keySet()) {
                    Log.d("MYAPP", "userRegistrationWorker: " + key);
                }
                throw new IllegalArgumentException("userId or role not provided.");
            }
            String collection = "";
            Map<String, Object> inputData = new HashMap<>();
            switch (role) {
                case "owner":
                    collection = "owners";
                    String aadhaarNumber = getInputData().getString("aadhaar_number");
                    if(aadhaarNumber == null) {
                        throw new IllegalArgumentException("aadhaar number not provided");
                    }
                    inputData.put("user_id", userId);
                    inputData.put("aadhaar_number", aadhaarNumber);
                    break;
                case "driver":
                    collection = "drivers";
                    inputData.put("user_id", userId);
                    break;
                default:
                    throw new IllegalArgumentException("provided role is invalid");
            }



            dbInstance.collection(collection)
                    .document(uid)
                    .set(inputData)
                    .addOnCompleteListener(task -> {
                       if(task.isSuccessful())  {
                           callback.onSuccess(new Data.Builder().putString("uid", uid).build());
                       }
                       else{
                           callback.onFailure(task.getException());
                       }
                    });
        }catch (Exception e) {
            callback.onFailure(e);
        }




    }
}
