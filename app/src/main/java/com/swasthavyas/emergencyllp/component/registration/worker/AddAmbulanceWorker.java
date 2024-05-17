package com.swasthavyas.emergencyllp.component.registration.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

public class AddAmbulanceWorker extends ListenableWorkerAdapter {


    public AddAmbulanceWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String userId = getInputData().getString("userId");
        String vehicleType = getInputData().getString("vehicleType");
        String vehicleNumber = getInputData().getString("vehicleNumber");
        String ambulanceType = getInputData().getString("ambulanceType");

        if(userId == null || vehicleNumber == null || vehicleType == null || ambulanceType == null) {
            callback.onFailure(new IllegalArgumentException("insufficient arguments provided"));
            return;
        }


    }
}
