package com.swasthavyas.emergencyllp.component.dashboard.driver.worker;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.common.primitives.Doubles;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

public class FetchEmployeeWorker extends ListenableWorkerAdapter {

    private final Context context;
    public FetchEmployeeWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        this.context = appContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        String userId = getInputData().getString("user_id");

        if(userId == null) {
            callback.onFailure(new IllegalArgumentException("User ID not provided"));
            return;
        }

        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        dbInstance
                .collection("employees")
                .whereEqualTo("user_id", userId)
                .limit(1)
                .get()
                .addOnCompleteListener(fetchEmployeeTask -> {
                    if(fetchEmployeeTask.isSuccessful()) {
                        QuerySnapshot querySnapshot = fetchEmployeeTask.getResult();

                        if(querySnapshot.isEmpty()) {
                            callback.onFailure(new NoSuchElementException("employee with given userId not found."));
                            return;
                        }

                        DocumentSnapshot snapshot = querySnapshot.getDocuments().get(0);
                        Data.Builder opDataBuilder = new Data.Builder()
                                .putString(EmployeeDriver.ModelColumns.EMAIL, snapshot.getId())
                                .putString(EmployeeDriver.ModelColumns.DRIVER_ID, snapshot.getString(EmployeeDriver.ModelColumns.DRIVER_ID))
                                .putString(EmployeeDriver.ModelColumns.OWNER_ID, snapshot.getString(EmployeeDriver.ModelColumns.OWNER_ID))
                                .putString(EmployeeDriver.ModelColumns.USER_ID, snapshot.getString(EmployeeDriver.ModelColumns.USER_ID))
                                .putInt(EmployeeDriver.ModelColumns.AGE,  ((Long) snapshot.get(EmployeeDriver.ModelColumns.AGE)).intValue())
                                .putString(EmployeeDriver.ModelColumns.AADHAAR_NUMBER, snapshot.getString(EmployeeDriver.ModelColumns.AADHAAR_NUMBER))
                                .putString(EmployeeDriver.ModelColumns.AADHAAR_IMAGE_REF, snapshot.getString(EmployeeDriver.ModelColumns.AADHAAR_IMAGE_REF))
                                .putString(EmployeeDriver.ModelColumns.LICENSE_IMAGE_REF, snapshot.getString(EmployeeDriver.ModelColumns.LICENSE_IMAGE_REF))
                                .putString(EmployeeDriver.ModelColumns.NAME, snapshot.getString(EmployeeDriver.ModelColumns.NAME))
                                .putString(EmployeeDriver.ModelColumns.PHONE_NUMBER, snapshot.getString(EmployeeDriver.ModelColumns.PHONE_NUMBER))
                                .putString(EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER, snapshot.getString(EmployeeDriver.ModelColumns.ASSIGNED_AMBULANCE_NUMBER))
                                .putString(EmployeeDriver.ModelColumns.PROFILE_IMAGE_REF, snapshot.getString(EmployeeDriver.ModelColumns.PROFILE_IMAGE_REF));

                        List<Double> lastLocation = (List<Double>) snapshot.get(EmployeeDriver.ModelColumns.LAST_LOCATION);
                        if(lastLocation != null) {
                            double[] location = Doubles.toArray(lastLocation);
                            opDataBuilder.putDoubleArray(EmployeeDriver.ModelColumns.LAST_LOCATION, location);
                        }

                        SharedPreferences preferences = context.getSharedPreferences("fcm_token", Context.MODE_PRIVATE);

                        if(preferences != null && preferences.contains("token")) {

                            Map<String, Object> inputMap = new HashMap<>();
                            inputMap.put("token", preferences.getString("token", null));
                            inputMap.put("timestamp", FieldValue.serverTimestamp());
                            dbInstance
                                    .collection("fcm_tokens")
                                    .document(Objects.requireNonNull(snapshot.getString(EmployeeDriver.ModelColumns.USER_ID)))
                                    .set(inputMap)
                                    .addOnCompleteListener(task -> {
                                        if(task.isSuccessful()) {
                                            Log.d(TAG, "FCM token created/updated: " + preferences.getString("token", null));
                                        }
                                        else {
                                            Log.e(TAG, "FCM token creation/updation failed", task.getException());
                                        }
                                    });
                        }

                        callback.onSuccess(opDataBuilder.build());

                    }
                    else {
                        callback.onFailure(fetchEmployeeTask.getException());
                    }
                });
    }
}
