package com.swasthavyas.emergencyllp.component.dashboard.owner.employee.worker;

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
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.model.EmployeeDriver.ModelColumns;

import java.util.HashMap;
import java.util.Map;

public class AddDriverWorker extends ListenableWorkerAdapter {


    public AddDriverWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String driverId = getInputData().getString(ModelColumns.DRIVER_ID);
        String userId = getInputData().getString(ModelColumns.USER_ID);
        String phoneNumber = getInputData().getString(ModelColumns.PHONE_NUMBER);
        String name = getInputData().getString(ModelColumns.NAME);
        String email = getInputData().getString(ModelColumns.EMAIL);
        String aadhaarNumber = getInputData().getString(ModelColumns.AADHAAR_NUMBER);
        String assignedAmbulanceNumber = getInputData().getString(ModelColumns.ASSIGNED_AMBULANCE_NUMBER);

        String ownerId = getInputData().getString("owner_id");
        String ownerUid = getInputData().getString("owner_uid");
        String password = getInputData().getString("password");
        String aadhaarUriString = getInputData().getString("aadhaarUriString");
        String licenseUriString = getInputData().getString("licenceUriString");
        int age = getInputData().getInt(ModelColumns.AGE, -1);

        if(driverId == null || userId == null || age == -1 || ownerId == null || ownerUid == null || phoneNumber == null || name == null || password == null || email == null || aadhaarNumber == null || aadhaarUriString == null || licenseUriString == null) {
            Log.d(AppConstants.TAG, "doAsyncBackgroundTask: " + getInputData());
            callback.onFailure(new IllegalArgumentException("one of the input data is null/invalid."));
            return;
        }

        FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference rootRef = storage.getReference();

        Map<String, Object> inputData = new HashMap<>();

        inputData.put(ModelColumns.USER_ID, userId);
        inputData.put(ModelColumns.AGE, age);
        inputData.put(ModelColumns.PHONE_NUMBER, phoneNumber);
        inputData.put(ModelColumns.NAME, name);
        inputData.put(ModelColumns.DRIVER_ID, driverId);
        inputData.put(ModelColumns.AADHAAR_NUMBER, aadhaarNumber);

        String assignedAmbulance = assignedAmbulanceNumber == null ? "None" : assignedAmbulanceNumber;
        inputData.put(ModelColumns.ASSIGNED_AMBULANCE_NUMBER, assignedAmbulance);


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

                        StorageReference aadhaarRef = rootRef.child(String.format("/users/owner/%s/employees/%s/docs/aadhaar_card.jpg", ownerUid, driverId));
                        StorageReference licenseRef = rootRef.child(String.format("/users/owner/%s/employees/%s/docs/license.jpg", ownerUid, driverId));

                        Data.Builder opData = new Data.Builder()
                                .putString(ModelColumns.NAME, name)
                                .putString(ModelColumns.PHONE_NUMBER, phoneNumber)
                                .putInt(ModelColumns.AGE, age)
                                .putString(ModelColumns.DRIVER_ID, driverId)
                                .putString(ModelColumns.USER_ID, userId)
                                .putString(ModelColumns.EMAIL, email)
                                .putString("password", password);


                        aadhaarRef.putFile(Uri.parse(aadhaarUriString))
                                .addOnCompleteListener(aadhaarUploadTask -> {
                                   if(aadhaarUploadTask.isSuccessful()) {
                                       opData.putString(ModelColumns.AADHAAR_IMAGE_REF, aadhaarUploadTask.getResult().getStorage().toString());
                                       licenseRef.putFile(Uri.parse(licenseUriString))
                                               .addOnCompleteListener(licenseUploadTask -> {
                                                   if(licenseUploadTask.isSuccessful()) {
                                                        opData.putString(ModelColumns.LICENSE_IMAGE_REF, licenseUploadTask.getResult().getStorage().toString());

                                                        dbInstance.collection("owners")
                                                                .document(ownerId)
                                                                .collection("employees")
                                                                .document(email)
                                                                .update(ModelColumns.AADHAAR_IMAGE_REF, aadhaarUploadTask.getResult().getStorage().toString(), ModelColumns.LICENSE_IMAGE_REF, licenseUploadTask.getResult().getStorage().toString());

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
