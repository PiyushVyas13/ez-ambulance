package com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

public class DeleteAmbulanceWorker extends ListenableWorkerAdapter {

    public DeleteAmbulanceWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {

        String ambulanceId = getInputData().getString(Ambulance.ModelColumns.ID);
        String ownerId = getInputData().getString(Ambulance.ModelColumns.OWNER_ID);
        String imageRef = getInputData().getString(Ambulance.ModelColumns.IMAGE_REF);
        String vehicleNumber = getInputData().getString(Ambulance.ModelColumns.VEHICLE_NUMBER);

        if(ambulanceId == null || ownerId == null || imageRef == null || vehicleNumber == null) {
            Log.d(AppConstants.TAG, "doAsyncBackgroundTask: " + getInputData());
            callback.onFailure(new NullPointerException("ambulanceId or ownerId or imageRef is null"));
            return;
        }

        FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();

        dbInstance
                .collection("owners")
                .document(ownerId)
                .collection("employees")
                .whereEqualTo(EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER, vehicleNumber)
                .get()
                .addOnCompleteListener(employeeFetchTask -> {
                   if(employeeFetchTask.isSuccessful()) {
                       WriteBatch updateBatch = dbInstance.batch();
                       for(DocumentSnapshot snapshot : employeeFetchTask.getResult()) {
                           if(snapshot.exists()) {
                               updateBatch.update(snapshot.getReference(), EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER, "None");
                           }
                       }

                       updateBatch.commit();

                       dbInstance
                               .collection("owners")
                               .document(ownerId)
                               .collection("ambulances")
                               .document(ambulanceId)
                               .delete()
                               .addOnCompleteListener(deleteTask -> {
                                   if(deleteTask.isSuccessful()) {

                                       FirebaseStorage storage = FirebaseStorage.getInstance();

                                       StorageReference ambulanceRef = storage.getReferenceFromUrl(imageRef);

                                       ambulanceRef.delete()
                                               .addOnCompleteListener(deleteImageTask -> {
                                                   if(deleteImageTask.isSuccessful()) {
                                                       callback.onSuccess(null);
                                                   }
                                                   else {
                                                       callback.onFailure(deleteImageTask.getException());
                                                   }
                                               });
                                   }
                                   else {
                                       callback.onFailure(deleteTask.getException());
                                   }
                               });
                   }
                });

    }
}
