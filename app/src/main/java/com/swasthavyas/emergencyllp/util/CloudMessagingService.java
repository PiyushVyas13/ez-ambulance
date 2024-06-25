package com.swasthavyas.emergencyllp.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CloudMessagingService extends FirebaseMessagingService {
    public CloudMessagingService() {
    }

    @Override
    public void onNewToken(@NonNull String token) {
//        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();
//
//        String deviceId = AppUtils.getDeviceId(getApplicationContext());
//
//        Map<String, Object> inputMap = new HashMap<>();
//        inputMap.put("token", token);
//        inputMap.put("timestamp", FieldValue.serverTimestamp());
//
//        dbInstance
//                .collection("fcm_tokens")
//                .document(deviceId)
//                .set(inputMap)
//                .addOnCompleteListener(task -> {
//                    if(task.isSuccessful()) {
//                        Log.d(AppConstants.TAG, "FCM token created/updated: " + token);
//                    }
//                    else {
//                        Log.e(AppConstants.TAG, "FCM token updation/creation failed.", task.getException());
//                    }
//                });

        SharedPreferences preferences = getSharedPreferences("fcm_token", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences
                .edit()
                .putString("token", token)
                .putString("timestamp", FieldValue.serverTimestamp().toString());

        editor.apply();
        editor.commit();

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Intent intent = new Intent(AppConstants.SHOW_TRIP_REQUEST);

        String tripId = message.getData().get("tripId");
        Log.d(AppConstants.TAG, "Received trip ID: " + tripId);
        intent.putExtra("trip_id", tripId);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        broadcastManager.sendBroadcast(intent);
    }
}