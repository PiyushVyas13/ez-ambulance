package com.swasthavyas.emergencyllp.component.auth.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;


public class SignInWorker extends ListenableWorker {

    public SignInWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            NetworkResultCallback callback = new NetworkResultCallback() {
                @Override
                public void onSuccess(Data data) {
                    completer.set(Result.success());
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

            String mode = getInputData().getString("mode");

            if(mode == null) {
                callback.onFailure(new IllegalArgumentException("mode not supplied!"));
                return callback;
            }

            if(mode.equals("mobile")) {
                performMobileSignIn(callback);
            }
            else if(mode.equals("email")) {
                performEmailSignIn(callback);
            }

            return callback;
        });
    }

    private void performMobileSignIn(NetworkResultCallback callback) {

        String verificationId = getInputData().getString("verificationId");
        String otp = getInputData().getString("otp");

        if(verificationId == null || otp == null)  {
            callback.onFailure(new IllegalArgumentException("verificationId or otp is null"));
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task ->  {
                    if(task.isSuccessful()) {
                        callback.onSuccess(null);
                    }
                    else {
                        callback.onFailure(task.getException());
                    }
                });

    }

    private void performEmailSignIn(NetworkResultCallback callback) {
        String email = getInputData().getString("email");
        String password = getInputData().getString("password");

        if(email == null || password == null) {
            callback.onFailure(new IllegalArgumentException("email or password is null"));
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
           if(task.isSuccessful()) {
               callback.onSuccess(null);
           }
           else {
               callback.onFailure(task.getException());
           }
        });

    }


}
