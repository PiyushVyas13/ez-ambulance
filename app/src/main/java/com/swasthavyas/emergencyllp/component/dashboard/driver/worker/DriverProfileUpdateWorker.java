package com.swasthavyas.emergencyllp.component.dashboard.driver.worker;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

public class DriverProfileUpdateWorker extends ListenableWorkerAdapter {

    public DriverProfileUpdateWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String profileUriString = getInputData().getString("profile_uri_string");

        if(profileUriString == null) {
            callback.onFailure(new IllegalArgumentException("Profile Uri not provided"));
            return;
        }

        FirebaseUser currentUser = FirebaseService.getInstance().getAuthInstance().getCurrentUser();

        if(currentUser == null) {
            callback.onFailure(new IllegalArgumentException("Unauthorized!"));
            return;
        }

        StorageReference rootRef = FirebaseService.getInstance().getStorageInstance().getReference();

        StorageReference profileRef = rootRef.child(String.format("/users/employees/%s/profile_image.jpg", currentUser.getEmail()));

        Uri profileUri = Uri.parse(profileUriString);

        profileRef.putFile(profileUri)
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

                       String profileRefString = profileRef.toString().replace("%40", "@");

                       dbInstance
                               .collection("employees")
                               .document(currentUser.getEmail())
                               .update("profileImageRef", profileRefString)
                               .addOnCompleteListener(updateTask -> {
                                  if(updateTask.isSuccessful()) {
                                      callback.onSuccess(new Data.Builder()
                                              .putString("profile_image_ref", profileRefString)
                                              .build());
                                  } else {
                                      callback.onFailure(updateTask.getException());
                                  }
                               });
                   } else {
                       callback.onFailure(task.getException());
                   }
                });
    }
}
