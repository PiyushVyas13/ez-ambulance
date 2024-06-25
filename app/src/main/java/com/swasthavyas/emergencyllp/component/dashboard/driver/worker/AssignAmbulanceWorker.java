package com.swasthavyas.emergencyllp.component.dashboard.driver.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

public class AssignAmbulanceWorker extends ListenableWorkerAdapter {

    public AssignAmbulanceWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String ambulanceNumber = getInputData().getString("ambulance_number");
        String driverMail = getInputData().getString("driver_mail");

        if(ambulanceNumber == null || driverMail == null) {
            callback.onFailure(new IllegalArgumentException("Invalid/null arguments to worker."));
            return;
        }

        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        dbInstance
                .collection("employees")
                .document(driverMail)
                .update(EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER, ambulanceNumber)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    }
                    else{
                        callback.onFailure(task.getException());
                    }
                });

    }
}
