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
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

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
        Log.d("MYAPP", "fetchOwnerWorker: Inside the worker, userId: " + userId);

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
                           Log.d("MYAPP", "fetchOwnerWorker: " + "[ "+ document.getId() + " => " + document.getData() + " ]");
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
                                                Log.d("MYAPP", String.format("fetchOwnerWorker: [%s => %s]", ambulance.getId(), ambulance.getData()));
                                                Map<String, Object> ambulanceMap = new HashMap<>();
                                                ambulanceMap.put("ambulance_id", ambulance.getId());
                                                ambulanceMap.put("ambulance_type", ambulance.getData().get("ambulanceType"));
                                                ambulanceMap.put("vehicle_number", ambulance.getData().get("vehicle_number"));
                                                ambulanceMap.put("vehicle_type", ambulance.getData().get("vehicle_type"));
                                                ambulanceMap.put("owner_id", ambulance.getData().get("owner_id"));
                                                ambulances.add(ambulanceMap);
                                            }

                                            modifiedResult.put("ambulances", getSerializedAmbulances(ambulances));

                                            Data opData = new Data.Builder()
                                                    .putAll(modifiedResult)
                                                            .build();



                                            callback.onSuccess(opData);
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

    private static String getSerializedAmbulances(List<Map<String, Object>> mapArrayList) {
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
