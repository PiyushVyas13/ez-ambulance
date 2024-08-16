package com.swasthavyas.emergencyllp.component.trip.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddTripHistoryWorker extends ListenableWorkerAdapter {

    public AddTripHistoryWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        Map<String, Object> lockedTripMap = getInputData().getKeyValueMap();

        Double[] pickupLocationArray = (Double[]) lockedTripMap.get("pickupLocation");
        Double[] dropLocationArray = (Double[]) lockedTripMap.get("dropLocation");

        assert pickupLocationArray != null;
        assert dropLocationArray != null;

        Map<String, Object> tripMap = getTripMap(pickupLocationArray, dropLocationArray, lockedTripMap);

        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        dbInstance
                .collection("trip_history")
                .document((String) tripMap.get("id"))
                .set(tripMap)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    private @NonNull Map<String, Object> getTripMap(Double[] pickupLocationArray, Double[] dropLocationArray, Map<String, Object> lockedTripMap) {
        List<Double> pickupLocationList = new ArrayList<>();
        List<Double> dropLocationList = new ArrayList<>();

        pickupLocationList.add(pickupLocationArray[0]);
        pickupLocationList.add(pickupLocationArray[1]);

        dropLocationList.add(dropLocationArray[0]);
        dropLocationList.add(dropLocationArray[1]);

        Map<String, Object> tripMap = new HashMap<>(lockedTripMap);

        tripMap.put("pickupLocation", pickupLocationList);
        tripMap.put("dropLocation", dropLocationList);
        return tripMap;
    }
}
