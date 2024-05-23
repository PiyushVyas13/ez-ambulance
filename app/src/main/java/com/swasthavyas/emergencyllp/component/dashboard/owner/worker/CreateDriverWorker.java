package com.swasthavyas.emergencyllp.component.dashboard.owner.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.UserProfileChangeRequest;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

import java.security.SecureRandom;
import java.util.Random;


public class CreateDriverWorker extends ListenableWorkerAdapter {
    Context context;
    public CreateDriverWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        this.context = appContext;
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        try {
            String name = getInputData().getString("name");
            String phoneNumber = getInputData().getString("phone_number");
            String email = getInputData().getString("email");

            if(name == null || phoneNumber == null || email == null) {
                callback.onFailure(new IllegalArgumentException("one of the arguments is null/invalid."));
                return;
            }

            String driverId = name.split(" ")[0] + phoneNumber.substring(3, 7);
            String password = generateDummyPassword();


            FirebaseAuth auth = FirebaseAuth.getInstance();

            FirebaseUser currentUser = auth.getCurrentUser();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();

                            assert user != null;

                            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build())
                                    .addOnCompleteListener(updateNameTask -> {
                                        if(updateNameTask.isSuccessful()) {
                                            Log.d(AppConstants.TAG, "createDriverWorker: name updated");
                                            // auth.signOut();
                                            auth.updateCurrentUser(currentUser);

                                        }
                                        else {
                                            Log.d(AppConstants.TAG, "createDriverWorker: cannot update name " + updateNameTask.getException());
                                        }
                                    });

                            Data opData = new Data.Builder()
                                    .putString("driver_id", driverId)
                                    .putString("password", password)
                                    .putString("user_id", user.getUid())
                                    .putString("name", name)
                                    .putString("phone_number", phoneNumber)
                                    .putString("email", email)
                                    .build();

                            callback.onSuccess(opData);

                        }
                        else {
                            Log.e(AppConstants.TAG, "doAsyncBackgroundTask: ", task.getException());
                            callback.onFailure(task.getException());
                        }
                    });



        } catch (StringIndexOutOfBoundsException e) {
            callback.onFailure(new IllegalArgumentException("name/phone number's format is invalid"));
        }

    }

    private String generateDummyPassword() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder password = new StringBuilder();
        Random random = new SecureRandom();

        for(int i=0; i<6; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }
}
