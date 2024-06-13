package com.swasthavyas.emergencyllp.component.dashboard.owner.employee.worker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddDriverWorker extends ListenableWorkerAdapter {
    NetworkResultCallback callback;
    FirebaseFirestore dbInstance;
    FirebaseStorage storage;
    StorageReference rootRef;
    Bundle receivedData;

    public AddDriverWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        this.callback = callback;

        // Extract received params from inputData
        receivedData = extractInputData();
        assert  receivedData != null;


        // Initialize Firestore and Storage
        dbInstance = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        rootRef = storage.getReference();

        DocumentReference newDriverReference = dbInstance.collection("owners").document(Objects.requireNonNull(receivedData.getString("owner_id"))).collection("employees").document(Objects.requireNonNull(receivedData.getString("email")));

        // Perform pre-insertion check (email)
        newDriverReference.get().addOnCompleteListener(checkEmailTask -> {
           if(checkEmailTask.isSuccessful()) {
               DocumentSnapshot snapshot = checkEmailTask.getResult();
               if(snapshot.exists()) {
                   callback.onFailure(new IllegalStateException("Email already exists."));
               }
               else {
                   checkPhoneNumber();
               }
           }
        });

    }

    private Bundle extractInputData() {
        String name = getInputData().getString(EmployeeDriver.ModelColumns.NAME);
        String email = getInputData().getString(EmployeeDriver.ModelColumns.EMAIL);
        String phoneNumber = getInputData().getString(EmployeeDriver.ModelColumns.PHONE_NUMBER);
        String aadhaarNumber = getInputData().getString(EmployeeDriver.ModelColumns.AADHAAR_NUMBER);
        String assignedAmbulanceInput = getInputData().getString(EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER);
        String assignedAmbulance = assignedAmbulanceInput != null ? assignedAmbulanceInput : "None";
        String aadhaarUriString = getInputData().getString("aadhaarUriString");
        String licenceUriString = getInputData().getString("licenceUriString");
        int age = getInputData().getInt(EmployeeDriver.ModelColumns.AGE, -1);
        String ownerId = getInputData().getString("owner_id");
        String ownerUid = getInputData().getString("owner_uid");

        if(name == null || email == null || phoneNumber == null || aadhaarNumber == null || aadhaarUriString == null || licenceUriString == null || age == -1 || ownerId == null || ownerUid == null) {
            Log.d(AppConstants.TAG, "extractInputData: " + getInputData());
            callback.onFailure(new IllegalArgumentException("one of the input data is null/invalid"));
            return null;
        }

        Bundle receivedData = new Bundle();

        receivedData.putString("name", name);
        receivedData.putString("email", email);
        receivedData.putString("phone_number", phoneNumber);
        receivedData.putString("aadhaar_number", aadhaarNumber);
        receivedData.putString("aadhaar_uri_string", aadhaarUriString);
        receivedData.putString("licence_uri_string", licenceUriString);
        receivedData.putString("assigned_ambulance", assignedAmbulance);
        receivedData.putInt("age", age);
        receivedData.putString("owner_id", ownerId);
        receivedData.putString("owner_uid", ownerUid);

        return receivedData;

    }

    /**
     * Perform pre-insertion check (phone number).
     */
    private void checkPhoneNumber() {
        dbInstance.collection("owners")
                .document(Objects.requireNonNull(receivedData.getString("owner_id")))
                .collection("employees")
                .whereEqualTo("phone_number", Objects.requireNonNull(receivedData.getString("phone_number")))
                .get()
                .addOnCompleteListener(checkPhoneNumberTask -> {
                    if(checkPhoneNumberTask.isSuccessful()) {
                        if(!checkPhoneNumberTask.getResult().isEmpty()) {
                            callback.onFailure(new IllegalStateException("Phone number already exists."));
                        }
                        else {
                            insertData();
                        }
                    }
                });
    }


    private void insertData() {
        String driverId = Objects.requireNonNull(receivedData.getString("name")).split(" ")[0] + receivedData.getString("phone_number").substring(3, 7);


        Map<String, Object> inputData = new HashMap<>();

        inputData.put(EmployeeDriver.ModelColumns.NAME, Objects.requireNonNull(receivedData.getString("name")));
        inputData.put(EmployeeDriver.ModelColumns.AGE, receivedData.getInt("age"));
        inputData.put(EmployeeDriver.ModelColumns.AADHAAR_NUMBER, receivedData.getString("aadhaar_number"));
        inputData.put(EmployeeDriver.ModelColumns.PHONE_NUMBER, Objects.requireNonNull(receivedData.getString("phone_number")));
        inputData.put(EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER, Objects.requireNonNull(receivedData.getString("assigned_ambulance")));
        inputData.put(EmployeeDriver.ModelColumns.DRIVER_ID, driverId);

        dbInstance
                .collection("owners")
                .document(Objects.requireNonNull(receivedData.getString("owner_id")))
                .collection("employees")
                .document(Objects.requireNonNull(receivedData.getString("email")))
                .set(inputData)
                .addOnCompleteListener(insertDataTask -> {
                   if(insertDataTask.isSuccessful()) {
                       uploadDocuments(inputData);
                   }
                   else {
                       callback.onFailure(insertDataTask.getException());
                   }
                });

    }

    private void uploadDocuments(Map<String, Object> inputData) {
        StorageReference aadhaarRef = rootRef.child(String.format("/users/owner/%s/employees/%s/docs/aadhaar_card.jpg", receivedData.getString("owner_uid"), receivedData.getString("email")));
        StorageReference licenceRef = rootRef.child(String.format("/users/owner/%s/employees/%s/docs/licence.jpg", receivedData.getString("owner_uid"), receivedData.getString("email")));

        aadhaarRef.putFile(Uri.parse(receivedData.getString("aadhaar_uri_string")))
                .addOnCompleteListener(aadhaarUploadTask -> {
                    if(aadhaarUploadTask.isSuccessful()) {
                        inputData.put(EmployeeDriver.ModelColumns.AADHAAR_IMAGE_REF, aadhaarUploadTask.getResult().getStorage().toString());
                        licenceRef.putFile(Uri.parse(receivedData.getString("licence_uri_string")))
                                .addOnCompleteListener(licenceUploadTask -> {
                                   if(licenceUploadTask.isSuccessful()) {
                                       inputData.put(EmployeeDriver.ModelColumns.LICENSE_IMAGE_REF, licenceUploadTask.getResult().getStorage().toString());
                                       updateDocument(inputData);
                                   }
                                   else {
                                       callback.onFailure(licenceUploadTask.getException());
                                   }
                                });
                    }
                    else {
                        callback.onFailure(aadhaarUploadTask.getException());
                    }
                });
    }

    private void updateDocument(Map<String, Object> inputData) {
        dbInstance
                .collection("owners")
                .document(Objects.requireNonNull(receivedData.getString("owner_id")))
                .collection("employees")
                .document(Objects.requireNonNull(receivedData.getString("email")))
                .update(
                        EmployeeDriver.ModelColumns.AADHAAR_IMAGE_REF, inputData.get(EmployeeDriver.ModelColumns.AADHAAR_IMAGE_REF),
                        EmployeeDriver.ModelColumns.LICENSE_IMAGE_REF, inputData.get(EmployeeDriver.ModelColumns.LICENSE_IMAGE_REF)
                )
                .addOnCompleteListener(updateDocumentTask -> {
                   if(updateDocumentTask.isSuccessful()) {
                       fetchAndInsertUserId(inputData);
                   }
                   else {
                       callback.onFailure(updateDocumentTask.getException());
                   }
                });
    }

    private void fetchAndInsertUserId(Map<String, Object> inputData) {
        dbInstance
                .collection("owners")
                .document(Objects.requireNonNull(receivedData.getString("owner_id")))
                .collection("employees")
                .document(Objects.requireNonNull(receivedData.getString("email")))
                .addSnapshotListener((value, error) -> {
                   if(value != null && value.exists()) {
                       Map<String, Object> data = value.getData();

                       if(data != null && data.containsKey("user_id")) {
                           inputData.put(EmployeeDriver.ModelColumns.USER_ID, data.get("user_id"));

                           Data.Builder opDataBuilder = new Data.Builder()
                                   .putAll(inputData);

                           callback.onSuccess(opDataBuilder.build());
                       }
                   }
                });
    }
}