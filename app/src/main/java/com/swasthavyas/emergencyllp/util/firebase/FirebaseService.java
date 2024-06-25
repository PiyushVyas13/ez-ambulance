package com.swasthavyas.emergencyllp.util.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Class to implement singleton pattern on Firebase services.
 * <p>Supports instances of {@link FirebaseAuth}, {@link FirebaseFirestore}, {@link FirebaseStorage}
 * and {@link FirebaseDatabase}.</p>
 */
public class FirebaseService {
    private static final String FIREBASE_DATABASE_URL = "https://swasthavyas-emergency-llp-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private static FirebaseService instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseDatabase database;

    private FirebaseService() {
        auth = FirebaseAuth.getInstance();
        firestore =FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL);
    }

    public static synchronized FirebaseService getInstance() {
        if(instance == null) {
            instance = new FirebaseService();
        }

        return instance;
    }

    public FirebaseAuth getAuthInstance() {
        return auth;
    }
    public FirebaseFirestore getFirestoreInstance() {
        return firestore;
    }
    public FirebaseStorage getStorageInstance() {
        return storage;
    }
    public FirebaseDatabase getDatabaseInstance() {
        return database;
    }
}
