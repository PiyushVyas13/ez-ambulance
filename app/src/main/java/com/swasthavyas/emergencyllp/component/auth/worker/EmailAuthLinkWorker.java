package com.swasthavyas.emergencyllp.component.auth.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

public class EmailAuthLinkWorker extends ListenableWorkerAdapter {


    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public EmailAuthLinkWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String email = getInputData().getString("email");
        String password = getInputData().getString("password");

        if(email == null || password == null) {
            callback.onFailure(new IllegalArgumentException("email or password not provided"));
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) {
            callback.onFailure(new NullPointerException("user is somehow null"));
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        user.linkWithCredential(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                callback.onSuccess(null);
            }
            else {
                callback.onFailure(task.getException());
            }
        });
    }
}
