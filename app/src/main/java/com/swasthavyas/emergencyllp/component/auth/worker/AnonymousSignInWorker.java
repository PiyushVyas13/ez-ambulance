package com.swasthavyas.emergencyllp.component.auth.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

public class AnonymousSignInWorker extends ListenableWorkerAdapter {

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public AnonymousSignInWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(task ->  {
            if(task.isSuccessful()) {
                callback.onSuccess(null);
            }
            else {
                callback.onFailure(task.getException());
            }
        });
    }
}
