package com.swasthavyas.emergencyllp.component.dashboard.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

import java.util.NoSuchElementException;

public class FetchRoleWorker extends ListenableWorker {


    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public FetchRoleWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            NetworkResultCallback callback = new NetworkResultCallback() {
                @Override
                public void onSuccess(Data data) {
                    completer.set(Result.success(data));
                }

                @Override
                public void onFailure(Exception exception) {

                    Data opData = new Data.Builder()
                            .putString("exception", exception.getClass().getSimpleName())
                            .putString("message", exception.getMessage())
                            .build();

                    completer.set(Result.failure(opData));
                }
            };

            getRoleFromDb(callback);

            return callback;
        });
    }

    private void getRoleFromDb(NetworkResultCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = getInputData().getString("userId");

        if(userId == null) {
            callback.onFailure(new IllegalArgumentException("userId not provided"));
            return;
        }

        DocumentReference ref = db.collection("user_roles").document(userId);

        ref.get().addOnCompleteListener(task ->  {
           if(task.isSuccessful()) {
               DocumentSnapshot document = task.getResult();
               Data opData = new Data.Builder().putString("role", document.getString("role")).build();
               if(document.exists()) {
                   callback.onSuccess(opData);
               }
               else {
                   Log.d("MYAPP", "getRoleFromDb: document not found");
                   callback.onFailure(new NoSuchElementException("document not found"));
               }
           }
        });
    }
}
