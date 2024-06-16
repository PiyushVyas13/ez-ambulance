package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.worker;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance.ModelColumns;

import java.util.HashMap;
import java.util.Map;

public class AddAmbulanceWorker extends ListenableWorkerAdapter {
    public AddAmbulanceWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String ownerId = getInputData().getString(ModelColumns.OWNER_ID);
        String vehicleType = getInputData().getString(ModelColumns.VEHICLE_TYPE);
        String vehicleNumber = getInputData().getString(ModelColumns.VEHICLE_NUMBER);
        String ambulanceType = getInputData().getString(ModelColumns.AMBULANCE_TYPE);
        String photoUri = getInputData().getString("photoUri");
        String userId = getInputData().getString("userId");

        if(ownerId == null || vehicleNumber == null || vehicleType == null || ambulanceType == null || photoUri == null) {
            callback.onFailure(new IllegalArgumentException("insufficient arguments provided"));
            return;
        }

        FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();

        Map<String, Object> inputData = new HashMap<>();
//        String uid = UUID.randomUUID().toString();
        inputData.put(ModelColumns.OWNER_ID, ownerId);
        inputData.put(ModelColumns.VEHICLE_NUMBER, vehicleNumber);
        inputData.put(ModelColumns.VEHICLE_TYPE, vehicleType);
        inputData.put(ModelColumns.AMBULANCE_TYPE, ambulanceType);

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference rootRef = storage.getReference();

        StorageReference ambulanceImageRef = rootRef.child(String.format("users/owner/%s/ambulances/ambulance_%s.jpg", userId, vehicleNumber));


        dbInstance.collection("owners")
                .document(ownerId)
                .collection("ambulances")
                .add(inputData)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        DocumentReference reference = task.getResult();

                        Data.Builder opDataBuilder = new Data.Builder()
                                .putString(ModelColumns.ID, reference.getId())
                                .putString(ModelColumns.OWNER_ID, ownerId)
                                .putString(ModelColumns.AMBULANCE_TYPE, ambulanceType)
                                .putString(ModelColumns.VEHICLE_NUMBER, vehicleNumber)
                                .putString(ModelColumns.VEHICLE_TYPE, vehicleType);

                        ambulanceImageRef.putFile(Uri.parse(photoUri))
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()) {
                                        opDataBuilder.putString(ModelColumns.IMAGE_REF, task1.getResult().getStorage().toString());

                                        Log.d(AppConstants.TAG, "addAmbulanceWorker: " + task.getResult().getPath());

                                        dbInstance
                                                .collection("owners")
                                                .document(ownerId)
                                                .collection("ambulances")
                                                .document(reference.getId())
                                                .update(ModelColumns.IMAGE_REF, task1.getResult().getStorage().toString());

                                        callback.onSuccess(opDataBuilder.build());
                                    }
                                    else {
                                        callback.onFailure(task1.getException());
                                    }
                                });


                    }
                    else {
                        callback.onFailure(task.getException());
                    }
                });

    }
}
