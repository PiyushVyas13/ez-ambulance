package com.swasthavyas.emergencyllp.component.dashboard.owner.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class FetchOwnerWorker extends ListenableWorkerAdapter {

    public FetchOwnerWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();

        String userId = getInputData().getString("userId");

        if(userId == null) {
            callback.onFailure(new IllegalArgumentException("userId not provided"));
            return;
        }
        Log.d(AppConstants.TAG, "fetchOwnerWorker: Inside the worker, userId: " + userId);

        dbInstance.collection("owners")
                .whereEqualTo("user_id", userId).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {

                       if(task.getResult().isEmpty()) {
                           callback.onFailure(new NoSuchElementException("Owner not found. User may be unauthorized."));
                           return ;

                       }

                       for(QueryDocumentSnapshot document : task.getResult()) {
                           Log.d(AppConstants.TAG, "fetchOwnerWorker: " + "[ "+ document.getId() + " => " + document.getData() + " ]");
                           Map<String, Object> modifiedResult = new HashMap<>();

                           modifiedResult.put("owner_id", document.getId());
                           modifiedResult.put("user_id", document.getData().get("user_id"));
                           modifiedResult.put("aadhaar_number", document.getData().get("aadhaar_number"));


                           dbInstance
                                   .collection("owners")
                                   .document(document.getId())
                                   .collection("ambulances")
                                   .get()
                                   .addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()) {

                                            List<Map<String, Object>> ambulances = new ArrayList<>();

                                            for(DocumentSnapshot ambulance : task1.getResult()) {
                                                Log.d(AppConstants.TAG, String.format("fetchOwnerWorker: [%s => %s]", ambulance.getId(), ambulance.getData()));
                                                Map<String, Object> ambulanceMap = new HashMap<>();
                                                ambulanceMap.put(Ambulance.ModelColumns.ID, ambulance.getId());
                                                ambulanceMap.put(Ambulance.ModelColumns.AMBULANCE_TYPE, ambulance.getData().get(Ambulance.ModelColumns.AMBULANCE_TYPE));
                                                ambulanceMap.put(Ambulance.ModelColumns.VEHICLE_NUMBER, ambulance.getData().get(Ambulance.ModelColumns.VEHICLE_NUMBER));
                                                ambulanceMap.put(Ambulance.ModelColumns.VEHICLE_TYPE, ambulance.getData().get(Ambulance.ModelColumns.VEHICLE_TYPE));
                                                ambulanceMap.put(Ambulance.ModelColumns.OWNER_ID, ambulance.getData().get(Ambulance.ModelColumns.OWNER_ID));
                                                ambulanceMap.put(Ambulance.ModelColumns.IMAGE_REF, ambulance.getData().get(Ambulance.ModelColumns.IMAGE_REF));
                                                ambulances.add(ambulanceMap);
                                            }

                                            modifiedResult.put("ambulances", getSerializedString(ambulances));

                                            dbInstance
                                                    .collection("employees")
                                                    .whereEqualTo("owner_id", document.getId())
                                                    .get()
                                                    .addOnCompleteListener(employeeTask -> {
                                                       List<Map<String, Object>> employees = new ArrayList<>();

                                                       for(DocumentSnapshot employee: employeeTask.getResult()) {
                                                           Log.d(AppConstants.TAG, String.format("fetchOwnerWorker: [%s => %s]", employee.getId(), employee.getData()));
                                                            Map<String, Object> employeeMap = new HashMap<>();

                                                            employeeMap.put(EmployeeDriver.ModelColumns.EMAIL, employee.getId());
                                                            employeeMap.put(EmployeeDriver.ModelColumns.DRIVER_ID, employee.getString(EmployeeDriver.ModelColumns.DRIVER_ID));
                                                            employeeMap.put(EmployeeDriver.ModelColumns.USER_ID, employee.getString(EmployeeDriver.ModelColumns.USER_ID));
                                                            employeeMap.put(EmployeeDriver.ModelColumns.PHONE_NUMBER, employee.getString(EmployeeDriver.ModelColumns.PHONE_NUMBER));
                                                            employeeMap.put(EmployeeDriver.ModelColumns.AGE, ((Long) employee.get(EmployeeDriver.ModelColumns.AGE)).intValue());
                                                            employeeMap.put(EmployeeDriver.ModelColumns.NAME, employee.get(EmployeeDriver.ModelColumns.NAME));
                                                            employeeMap.put(EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER, employee.getString(EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER));
                                                            employeeMap.put(EmployeeDriver.ModelColumns.OWNER_ID, employee.getString(EmployeeDriver.ModelColumns.OWNER_ID));
                                                            employeeMap.put(EmployeeDriver.ModelColumns.LAST_LOCATION, employee.get(EmployeeDriver.ModelColumns.LAST_LOCATION));
                                                            employeeMap.put(EmployeeDriver.ModelColumns.PROFILE_IMAGE_REF, employee.get(EmployeeDriver.ModelColumns.PROFILE_IMAGE_REF));
                                                            employees.add(employeeMap);
                                                       }

                                                       modifiedResult.put("employees", getSerializedString(employees));

                                                        Data opData = new Data.Builder()
                                                                .putAll(modifiedResult)
                                                                .build();

                                                        callback.onSuccess(opData);

                                                    });


                                        }
                                        else {
                                            callback.onFailure(task1.getException());
                                        }
                                   });
                       }
                   }
                   else {
                       callback.onFailure(task.getException());
                   }
                });
    }

    private static String getSerializedString(List<Map<String, Object>> mapArrayList) {
        if (mapArrayList == null || mapArrayList.isEmpty()) {
            return "[]";
        }

        Gson gson = new Gson();
        JSONArray jsonArray = new JSONArray();
        for (Map<String, Object> map : mapArrayList) {
            jsonArray.put(gson.toJsonTree(map));
        }
        return jsonArray.toString();
    }
}
