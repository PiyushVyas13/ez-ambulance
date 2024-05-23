package com.swasthavyas.emergencyllp.component.registration.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.util.AppConstants;
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
                    Log.d(AppConstants.TAG, "userRegistrationWorker: " + key);
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

                    Map<String, Object> driverAmbulance = new HashMap<>();

                    String ambulanceUid = UUID.randomUUID().toString();
                    String ambulanceType = getInputData().getString("ambulance_type");
                    String vehicleNumber = getInputData().getString("vehicle_number");
                    String vehicleType = getInputData().getString("vehicle_type");
                    String aadhaar = getInputData().getString("aadhaar_number");

                    if(ambulanceType == null || vehicleType == null || vehicleNumber == null || aadhaar == null) {
                        callback.onFailure(new IllegalArgumentException("ambulanceDriver parameters are null"));
                        return;
                    }

                    driverAmbulance.put("id", ambulanceUid);
                    driverAmbulance.put("ambulance_type", ambulanceType);
                    driverAmbulance.put("vehicle_number", vehicleNumber);
                    driverAmbulance.put("vehicle_type", vehicleType);


                    inputData.put("user_id", userId);
                    inputData.put("ambulance", driverAmbulance);
                    inputData.put("aadhaar_number", aadhaar);

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
