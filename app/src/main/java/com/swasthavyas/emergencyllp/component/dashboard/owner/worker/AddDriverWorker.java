package com.swasthavyas.emergencyllp.component.dashboard.owner.worker;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.swasthavyas.emergencyllp.util.AppConstants;
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
        String assignedAmbulanceNumber = getInputData().getString("assigned_ambulance_number");
        String aadhaarUriString = getInputData().getString("aadhaarUriString");
        String licenseUriString = getInputData().getString("licenceUriString");
        int age = getInputData().getInt("age", -1);

        if(driverId == null || userId == null || age == -1 || ownerId == null || phoneNumber == null || name == null || password == null || email == null || assignedAmbulanceNumber == null || aadhaarUriString == null || licenseUriString == null) {
            Log.d(AppConstants.TAG, "doAsyncBackgroundTask: " + getInputData());
            callback.onFailure(new IllegalArgumentException("one of the input data is null/invalid."));
            return;
        }

        FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference rootRef = storage.getReference();

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

                        StorageReference aadhaarRef = rootRef.child(String.format("/users/owner/%s/employees/%s/docs/aadhaar_card.jpg", ownerId, driverId));
                        StorageReference licenseRef = rootRef.child(String.format("/users/owner/%s/employees/%s/docs/license.jpg", ownerId, driverId));

                        Data.Builder opData = new Data.Builder()
                                .putString("name", name)
                                .putString("phone_number", phoneNumber)
                                .putInt("age", age)
                                .putString("driverId", driverId)
                                .putString("userId", userId)
                                .putString("password", password)
                                .putString("email", email);


                        aadhaarRef.putFile(Uri.parse(aadhaarUriString))
                                .addOnCompleteListener(aadhaarUploadTask -> {
                                   if(aadhaarUploadTask.isSuccessful()) {
                                       opData.putString("aadhaar_storage_ref", aadhaarUploadTask.getResult().getStorage().toString());
                                       licenseRef.putFile(Uri.parse(licenseUriString))
                                               .addOnCompleteListener(licenseUploadTask -> {
                                                   if(licenseUploadTask.isSuccessful()) {
                                                        opData.putString("license_storage_ref", licenseUploadTask.getResult().getStorage().toString());

                                                        callback.onSuccess(opData.build());
                                                   }
                                                   else {
                                                       callback.onFailure(licenseUploadTask.getException());
                                                   }

                                               });

                                   }
                                   else {
                                       callback.onFailure(aadhaarUploadTask.getException());
                                   }

                                });
                    }
                    else {
                        callback.onFailure(task.getException());
                    }
                });
    }
}
