package com.swasthavyas.emergencyllp.component.trip.worker;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.TripHistory;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
        String terminalState = getInputData().getString("terminal_state");
        String pickupPolyline = getInputData().getString("pickup_polyline");
        String dropPolyline = getInputData().getString("drop_polyline");

        if(terminalState == null || pickupPolyline == null || dropPolyline == null) {
            Log.d(TAG, "doAsyncBackgroundTask: " + getInputData());
            callback.onFailure(new IllegalArgumentException("terminal state/polylines not provided."));
            return;
        }

        Double[] pickupLocationArray = (Double[]) lockedTripMap.get("pickupLocation");
        Double[] dropLocationArray = (Double[]) lockedTripMap.get("dropLocation");

        assert pickupLocationArray != null;
        assert dropLocationArray != null;

        String assignedAmbulanceId = (String) lockedTripMap.get("assignedAmbulanceId");
        String assignedDriverId = (String) lockedTripMap.get("assignedDriverId");
        String ownerId = (String) lockedTripMap.get("ownerId");

        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        dbInstance
                .collection("owners")
                .document(ownerId)
                .collection("ambulances")
                .document(assignedAmbulanceId)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        String imageRef = task.getResult().getString("image_ref");

                        dbInstance
                                .collection("employees")
                                .whereEqualTo("driver_id", assignedDriverId)
                                .limit(1)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()) {
                                        String profileRef = task1.getResult().getDocuments().get(0).getString("profileImageRef");

                                        Map<String, Object> tripMap = getTripMap(
                                                pickupLocationArray,
                                                dropLocationArray,
                                                lockedTripMap,
                                                terminalState,
                                                imageRef,
                                                profileRef,
                                                Arrays.asList(pickupPolyline, dropPolyline)
                                        );

                                        if(tripMap == null) {
                                            callback.onFailure(new IllegalArgumentException("trip status is invalid."));
                                            return;
                                        }

                                        TripHistory tripHistory = TripHistory.createFromMap(tripMap);


                                        dbInstance
                                                .collection("trip_history")
                                                .document(tripHistory.getTrip().getId())
                                                .set(tripHistory)
                                                .addOnCompleteListener(task2 -> {
                                                    if(task2.isSuccessful()) {
                                                        callback.onSuccess(null);
                                                    } else {
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

    private Map<String, Object> getTripMap(Double[] pickupLocationArray, Double[] dropLocationArray, Map<String, Object> lockedTripMap, String terminalState, String imageRef, String profileRef, List<String> polylines) {
        List<Double> pickupLocationList = new ArrayList<>();
        List<Double> dropLocationList = new ArrayList<>();

        pickupLocationList.add(pickupLocationArray[0]);
        pickupLocationList.add(pickupLocationArray[1]);

        dropLocationList.add(dropLocationArray[0]);
        dropLocationList.add(dropLocationArray[1]);

        Map<String, Object> tripMap = new HashMap<>(lockedTripMap);

        tripMap.put("pickupLocation", pickupLocationList);
        tripMap.put("dropLocation", dropLocationList);
        tripMap.put("status", TripStatus.valueOf((String) tripMap.get("status")));
        Log.d(TAG, "getTripMap: "+tripMap);
        Trip trip = Trip.createFromMap(tripMap);


        Map <String,Object> tripHistoryMap = new HashMap<>();

        try {
            tripHistoryMap.put("trip",trip);
            tripHistoryMap.put("terminalState", TripStatus.valueOf(terminalState));
            tripHistoryMap.put("completionTimestamp" , new Timestamp(new Date()));
            tripHistoryMap.put("routePolyLines", polylines);
            tripHistoryMap.put("ambulanceImageRef", imageRef);
            tripHistoryMap.put("driverProfileImageRef", profileRef);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "getTripMap: "+e);
            return null;
        }

        return tripHistoryMap;
    }
}
