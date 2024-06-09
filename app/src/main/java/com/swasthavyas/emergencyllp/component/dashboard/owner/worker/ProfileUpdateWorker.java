package com.swasthavyas.emergencyllp.component.dashboard.owner.worker;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

public class ProfileUpdateWorker extends ListenableWorkerAdapter {

    public ProfileUpdateWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {

        String newProfileUriString = getInputData().getString("newProfileUriString");

        if(newProfileUriString == null) {
            callback.onFailure(new NullPointerException("profileUriString is missing"));
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null) {
            callback.onFailure(new IllegalStateException("Unauthorized!"));
            return;
        }

        Uri newProfileUri = Uri.parse(newProfileUriString);

        StorageReference rootRef = FirebaseStorage.getInstance().getReference();
        StorageReference profilePicRef = rootRef.child(String.format("/users/owner/%s/profile_image.jpg", currentUser.getUid()));

        profilePicRef.putFile(newProfileUri)
                .addOnCompleteListener(uploadImageTask -> {
                    if(uploadImageTask.isSuccessful()) {

                        profilePicRef.getDownloadUrl()
                                .addOnCompleteListener(downloadUrlTask -> {

                                    if(downloadUrlTask.isSuccessful()) {
                                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                                .setPhotoUri(downloadUrlTask.getResult())
                                                .build();

                                        currentUser.updateProfile(profileChangeRequest)
                                                .addOnCompleteListener(imageUpdateTask -> {
                                                    if(imageUpdateTask.isSuccessful()) {
                                                        callback.onSuccess(null);
                                                    }
                                                    else {
                                                        Log.d(AppConstants.TAG, "uploadProfileImage: " + imageUpdateTask.getException());
                                                        callback.onFailure(imageUpdateTask.getException());
                                                    }

                                                });
                                    }
                                    else {
                                        Log.d(AppConstants.TAG, "uploadProfileImage: " + downloadUrlTask.getException());
                                        callback.onFailure(downloadUrlTask.getException());
                                    }
                                });

                    }
                    else {
                        Log.d(AppConstants.TAG, "uploadProfileImage: " + uploadImageTask.getException());
                        callback.onFailure(uploadImageTask.getException());
                    }

                });
    }
}
