package com.swasthavyas.emergencyllp.util.asyncwork;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

public abstract class ListenableWorkerAdapter extends ListenableWorker {
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public ListenableWorkerAdapter(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    public abstract void doAsyncBackgroundTask(NetworkResultCallback callback);

    @Override
    @NonNull
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            NetworkResultCallback callback = new NetworkResultCallback() {
                @Override
                public void onSuccess(Data data) {
                    if(data == null) {
                        completer.set(Result.success());
                    }
                    else {
                        completer.set(Result.success(data));
                    }
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

            doAsyncBackgroundTask(callback);

            return callback;
        });
    }
}
