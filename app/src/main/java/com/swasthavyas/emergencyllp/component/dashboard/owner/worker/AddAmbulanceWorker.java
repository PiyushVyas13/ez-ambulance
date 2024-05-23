package com.swasthavyas.emergencyllp.component.dashboard.owner.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

import java.util.HashMap;
import java.util.Map;

public class AddAmbulanceWorker extends ListenableWorkerAdapter {
    public AddAmbulanceWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String ownerId = getInputData().getString("ownerId");
        String vehicleType = getInputData().getString("vehicleType");
        String vehicleNumber = getInputData().getString("vehicleNumber");
        String ambulanceType = getInputData().getString("ambulanceType");

        if(ownerId == null || vehicleNumber == null || vehicleType == null || ambulanceType == null) {
            callback.onFailure(new IllegalArgumentException("insufficient arguments provided"));
            return;
        }

        FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();

        Map<String, Object> inputData = new HashMap<>();
//        String uid = UUID.randomUUID().toString();
        inputData.put("owner_id", ownerId);
        inputData.put("vehicle_number", vehicleNumber);
        inputData.put("vehicle_type", vehicleType);
        inputData.put("ambulanceType", ambulanceType);


        dbInstance.collection("owners")
                .document(ownerId)
                .collection("ambulances")
                .add(inputData)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        DocumentReference reference = task.getResult();

                        reference.getId();



                        Data opData = new Data.Builder()
                                .putString("id", reference.getId())
                                .putString("owner_id", ownerId)
                                .putString("ambulance_type", ambulanceType)
                                .putString("vehicle_number", vehicleNumber)
                                .putString("vehicle_type", vehicleType)
                                .build();


                        Log.d(AppConstants.TAG, "addAmbulanceWorker: " + task.getResult().getPath());
                        callback.onSuccess(opData);
                    }
                    else {
                        callback.onFailure(task.getException());
                    }
                });



    }
}
