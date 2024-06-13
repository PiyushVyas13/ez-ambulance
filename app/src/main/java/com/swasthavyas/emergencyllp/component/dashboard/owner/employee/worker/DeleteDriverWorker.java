package com.swasthavyas.emergencyllp.component.dashboard.owner.employee.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

import java.util.Map;
import java.util.NoSuchElementException;

public class DeleteDriverWorker extends ListenableWorkerAdapter {

    private FirebaseFirestore dbInstance;
    private FirebaseStorage storage;
    private NetworkResultCallback callback;

    public DeleteDriverWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        this.callback = callback;
        String driverId = getInputData().getString("driver_id");
        String ownerId = getInputData().getString("owner_id");
        String ownerUid = getInputData().getString("owner_uid");

        if(driverId == null || ownerId == null || ownerUid == null) {
            Log.d(AppConstants.TAG, "doAsyncBackgroundTask: " + getInputData());
            callback.onFailure(new IllegalArgumentException("One of the arguments is null/invalid"));
            return;
        }

        dbInstance = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        dbInstance
                .collection("owners")
                .document(ownerId)
                .collection("employees")
                .whereEqualTo(EmployeeDriver.ModelColumns.DRIVER_ID, driverId)
                .limit(1)
                .get()
                .addOnCompleteListener(fetchDriverTask -> {
                   if(fetchDriverTask.isSuccessful()) {
                       if(fetchDriverTask.getResult().isEmpty()) {
                           Log.d(AppConstants.TAG, "DeleteDriverWorker.doAsyncBackgroundTask: " + driverId + ", " + ownerId);
                           callback.onFailure(new NoSuchElementException("Driver with given ID does not exist. May be it was already deleted."));
                       }
                       else {
                           DocumentSnapshot snapshot = fetchDriverTask.getResult().getDocuments().get(0);

                           String aadhaarRef = snapshot.getString(EmployeeDriver.ModelColumns.AADHAAR_IMAGE_REF);
                           String licenceRef = snapshot.getString(EmployeeDriver.ModelColumns.LICENSE_IMAGE_REF);

                           snapshot.getReference()
                                   .delete()
                                   .addOnCompleteListener(deleteDriverTask -> {
                                      if(deleteDriverTask.isSuccessful()) {
                                          deleteDocuments(aadhaarRef, licenceRef);
                                      }
                                      else  {
                                          callback.onFailure(deleteDriverTask.getException());
                                      }
                                   });
                       }
                   }
                   else {
                       callback.onFailure(fetchDriverTask.getException());
                   }
                });
    }

    private void deleteDocuments(String aadhaarRefString, String licenceRefString) {
        StorageReference aadhaarRef = storage.getReferenceFromUrl(aadhaarRefString);
        StorageReference licenceRef = storage.getReferenceFromUrl(licenceRefString);

        aadhaarRef.delete()
                .addOnCompleteListener(deleteAadhaarTask -> {
                   if(deleteAadhaarTask.isSuccessful()) {
                       licenceRef.delete()
                               .addOnCompleteListener(licenceDeleteTask -> {
                                  if(licenceDeleteTask.isSuccessful()) {
                                      callback.onSuccess(null);
                                  }
                                  else {
                                      Log.d(AppConstants.TAG, "deleteDocuments: " + licenceRef);
                                      callback.onFailure(licenceDeleteTask.getException());
                                  }
                               });
                   }
                   else {
                       Log.d(AppConstants.TAG, "deleteDocuments: " + aadhaarRef);
                       callback.onFailure(deleteAadhaarTask.getException());
                   }
                });
    }
}
