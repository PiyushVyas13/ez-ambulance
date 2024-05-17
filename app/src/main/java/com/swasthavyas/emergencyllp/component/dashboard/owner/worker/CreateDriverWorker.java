package com.swasthavyas.emergencyllp.component.dashboard.owner.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.GetPasswordOption;
import androidx.credentials.PasswordCredential;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

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

            if(name == null || phoneNumber == null) {
                callback.onFailure(new IllegalArgumentException("one of the arguments is null/invalid."));
                return;
            }

            String driverId = name.split(" ")[0] + phoneNumber.substring(3, 7) + "@swasthavyasllp.com";
            String password = generateDummyPassword();


            FirebaseAuth auth = FirebaseAuth.getInstance();

            FirebaseUser currentUser = auth.getCurrentUser();

            auth.createUserWithEmailAndPassword(driverId, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();

                            assert user != null;

                            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build())
                                    .addOnCompleteListener(updateNameTask -> {
                                        if(updateNameTask.isSuccessful()) {
                                            Log.d("MYAPP", "createDriverWorker: name updated");
                                            // auth.signOut();
                                            auth.updateCurrentUser(currentUser);

                                        }
                                        else {
                                            Log.d("MYAPP", "createDriverWorker: cannot update name " + updateNameTask.getException());
                                        }
                                    });

                            Data opData = new Data.Builder()
                                    .putString("driver_id", driverId)
                                    .putString("password", password)
                                    .putString("user_id", user.getUid())
                                    .putString("name", name)
                                    .putString("phone_number", phoneNumber)
                                    .build();

                            callback.onSuccess(opData);

                        }
                        else {
                            Log.e("MYAPP", "doAsyncBackgroundTask: ", task.getException());
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
