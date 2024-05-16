package com.swasthavyas.emergencyllp.component.auth.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

public class PhoneAuthLinkWorker extends ListenableWorkerAdapter {

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public PhoneAuthLinkWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String otp = getInputData().getString("otp");
        String verificationId = getInputData().getString("verificationId");

        if(otp == null || verificationId == null) {
            callback.onFailure(new IllegalArgumentException("otp or verificationId was not provided."));
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) {
            callback.onFailure(new NullPointerException("user is somehow null"));
            return;
        }

        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, otp);

        user.linkWithCredential(phoneAuthCredential).addOnCompleteListener(task ->  {
            if(task.isSuccessful()) {
                callback.onSuccess(null);
            }
            else {
                callback.onFailure(task.getException());
            }
        });
    }

}
