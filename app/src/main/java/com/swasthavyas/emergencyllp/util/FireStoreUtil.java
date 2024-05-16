package com.swasthavyas.emergencyllp.util;

import android.os.Bundle;

import androidx.work.Data;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FireStoreUtil {
    private final FirebaseFirestore dbInstance;


    public FireStoreUtil(FirebaseFirestore dbInstance) {
        this.dbInstance = dbInstance;
    }

    public boolean insertData(String collection, String document, Map<String, Object> inputData) {
        final AtomicBoolean result = new AtomicBoolean(false);

        dbInstance.collection(collection).document(document)
                .set(inputData)
                .addOnCompleteListener(task ->  {
                   if(task.isSuccessful()) {
                       result.set(true);
                   }
                });

        return result.get();
    }
}
