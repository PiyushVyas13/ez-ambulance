package com.swasthavyas.emergencyllp.component.dashboard.driver.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
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
                .whereEqualTo(EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER, ambulanceNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        WriteBatch batch = dbInstance.batch();
                        for(DocumentSnapshot snapshot : task.getResult().getDocuments()) {
                            String email = snapshot.getId();

                            DocumentReference ref = dbInstance.collection("employees").document(email);
                            batch.update(ref, EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER, "None");
                        }
                        batch.commit().addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()) {
                                dbInstance
                                        .collection("employees")
                                        .document(driverMail)
                                        .update(EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER, ambulanceNumber)
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                callback.onSuccess(null);
                                            }
                                            else{
                                                callback.onFailure(task2.getException());
                                            }
                                        });
                            } else {
                                callback.onFailure(task1.getException());
                            }
                        });
                    } else {
                        callback.onFailure(task.getException());
                    }
                });

    }
}
