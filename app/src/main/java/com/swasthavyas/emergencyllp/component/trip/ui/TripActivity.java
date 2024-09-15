package com.swasthavyas.emergencyllp.component.trip.ui;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StrokeStyle;
import com.google.android.gms.maps.model.StyleSpan;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.trip.worker.AddTripHistoryWorker;
import com.swasthavyas.emergencyllp.component.trip.worker.FetchRoutePreviewWorker;
import com.swasthavyas.emergencyllp.component.trip.worker.RemoveTripWorker;
import com.swasthavyas.emergencyllp.databinding.ActivityTripBinding;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TripActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityTripBinding viewBinding;
    private GoogleMap gMap;
    private Trip trip;

    private FusedLocationProviderClient locationProviderClient;

    private Map<String, Object> tripPolylines;

    private static final String REQUEST_PICKUP = "pickup_polyline";
    private static final String REQUEST_DROP = "drop_polyline";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityTripBinding.inflate(getLayoutInflater());
        tripPolylines = new HashMap<>();

        EdgeToEdge.enable(this);
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });


        String ownerId = getIntent().getStringExtra("owner_id");
        String tripId = getIntent().getStringExtra("trip_id");

        if (ownerId == null || tripId == null) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        SupportMapFragment mapFragment = viewBinding.navigationMap.getFragment();
        mapFragment.getMapAsync(this);
        loadTrip(ownerId, tripId);

        viewBinding.navigateButton.setOnClickListener(v -> {
            if (trip == null) {
                Toast.makeText(this, "Current location or trip is null", Toast.LENGTH_SHORT).show();
                return;
            }

            switch (trip.getStatus()) {
                case INITIATED:
                    updateDriverStatus(TripStatus.CLIENT_PICKUP);
                    startGoogleMapsIntent(trip.getPickupLocation());

                    viewBinding.navigateButton.setVisibility(View.GONE);
                    viewBinding.pickupComplete.setVisibility(View.VISIBLE);
                    break;
                case CLIENT_PICKUP:
                    updateDriverStatus(TripStatus.CLIENT_DROP);
                    startGoogleMapsIntent(trip.getDropLocation());
                    break;
                case CLIENT_DROP:
                    startGoogleMapsIntent(trip.getDropLocation());
                    break;
            }

        });
        viewBinding.pickupComplete.setOnClickListener(v -> {
            checkDriverPickupStatus();
        });
        viewBinding.endRide.setOnClickListener(v -> {
            if (trip == null) {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (trip.getStatus() == TripStatus.CLIENT_DROP) {
                checkDriverDropStatus();
            } else {
                new MaterialAlertDialogBuilder(TripActivity.this)
                        .setTitle("Confirm")
                        .setMessage("You have not reached the destination yet. Are you sure you want to end?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            deleteTrip(TripStatus.CANCELLED);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {})
                        .show();
            }

        });

    }

    private void startGoogleMapsIntent(List<Double> destination) {
        @SuppressLint("DefaultLocale") String coordinates = String.format("%f,%f", destination.get(0), destination.get(1));

        String uriString = "google.navigation:q=" + coordinates;

        Uri navigationUri = Uri.parse(uriString);
        Intent directionsIntent = new Intent(Intent.ACTION_VIEW, navigationUri);
        directionsIntent.setPackage("com.google.android.apps.maps");
        startActivity(directionsIntent);
    }

    private void updateDriverStatus(TripStatus status) {
        if (trip == null) {
            return;
        }

        FirebaseDatabase database = FirebaseService.getInstance().getDatabaseInstance();

        database
                .getReference()
                .getRoot()
                .child("trips")
                .child(trip.getOwnerId())
                .child(trip.getId())
                .child("status")
                .setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "updateDriverStatus: driver status updated to " + status);
                    } else {
                        Log.d(TAG, "updateDriverStatus: " + task.getException());
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        switch (trip.getStatus()) {
            case INITIATED:
            case CLIENT_PICKUP:
                requestRoutePreview(googleMap, trip.getPickupLocation(), REQUEST_PICKUP);
                break;
            case CLIENT_DROP:
                requestRoutePreview(googleMap, trip.getDropLocation(), REQUEST_DROP);
                break;
        }

    }

    private void requestRoutePreview(@NonNull GoogleMap googleMap, List<Double> dest, String requestType) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationProviderClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Location location = task.getResult();
                if (location != null) {
                    Log.d("MYAPP", "Location:" + String.format("(%f, %f)", location.getLatitude(), location.getLongitude()));
                } else {
                    Log.d("MYAPP", "Location: null");
                }

                double[] origin = new double[2];
                double[] destination = new double[2];

                if (trip != null) {
                    origin = new double[]{location == null ? 21.1458 : location.getLatitude(), location == null ? 79.0882 : location.getLongitude()};
                    destination = new double[]{dest.get(0), dest.get(1)};
                }

                OneTimeWorkRequest getRoutePreviewRequest = new OneTimeWorkRequest.Builder(FetchRoutePreviewWorker.class)
                        .setInputData(new Data.Builder()
                                .putDoubleArray("origin", origin)
                                .putDoubleArray("destination", destination)
                                .build())
                        .build();

                WorkManager.getInstance(getApplicationContext())
                        .enqueue(getRoutePreviewRequest);

                WorkManager.getInstance(getApplicationContext())
                        .getWorkInfoByIdLiveData(getRoutePreviewRequest.getId())
                        .observe(this, workInfo -> {
                            if (workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                                Toast.makeText(this, "Route received", Toast.LENGTH_SHORT).show();

                                String encodedPolyline = workInfo.getOutputData().getString("polyline");
                                String rawDuration = workInfo.getOutputData().getString("duration");

                                if (encodedPolyline == null || rawDuration == null) {
                                    Log.d(TAG, "onMapReady: " + workInfo.getOutputData());
                                    return;
                                }
                                tripPolylines.putIfAbsent(requestType, encodedPolyline);
                                updateTripPolylines(encodedPolyline);
                                renderRoutePolyline(googleMap, dest, rawDuration, encodedPolyline, location);

                            } else if (workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                Toast.makeText(this, workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                                viewBinding.tripProgressbar.setVisibility(View.GONE);
                            } else if (workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                                viewBinding.navigateButton.setEnabled(false);
                                viewBinding.tripProgressbar.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });
    }

    private void updateTripPolylines(String encodedPolyline) {

        if(trip == null) {
            return;
        }

        FirebaseDatabase database = FirebaseService.getInstance().getDatabaseInstance();

        database
                .getReference()
                .getRoot()
                .child("trips")
                .child(trip.getOwnerId())
                .child(trip.getId())
                .child("routePolyline")
                .setValue(encodedPolyline)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Log.d(TAG, "updateTripPolylines: route sent to owner!");
                    } else {
                        Log.d(TAG, "updateTripPolylines: " + task.getException());
                    }
                });


    }

    private void renderRoutePolyline(@NonNull GoogleMap googleMap, List<Double> dest, String rawDuration, String encodedPolyline, Location location) {
        String duration = formatETA(rawDuration);
        List<LatLng> coordinates = PolyUtil.decode(encodedPolyline);

        googleMap.clear();
        PolylineOptions options = new PolylineOptions()
                .addAll(coordinates)
                .addSpan(new StyleSpan(StrokeStyle.gradientBuilder(Color.RED, Color.YELLOW).build()));

        Polyline routePreview = googleMap.addPolyline(options);

        routePreview.setColor(Color.BLUE);

        LatLng midpoint = getRouteMidpoint(routePreview.getPoints());

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .icon(getBitmapFromVector())
        );
        googleMap.addMarker(new MarkerOptions().position(new LatLng(dest.get(0), dest.get(1))));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(midpoint, 15f));
        viewBinding.navigateButton.setEnabled(true);
        viewBinding.eta.setText(getString(R.string.eta_text, duration));
        viewBinding.tripProgressbar.setVisibility(View.GONE);
    }

    private BitmapDescriptor getBitmapFromVector() {
        Drawable vectorDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.runninglocation);

        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private boolean isUnderRadius(Location location, List<Double> target, double radius) {

        if (trip == null) {
            return false;
        }
        LatLng currentLat = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng targetLat = new LatLng(target.get(0), target.get(1));

        double distance = SphericalUtil.computeDistanceBetween(currentLat, targetLat);

        return !(distance > radius);

    }


    private void checkDriverDropStatus() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationProviderClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, new CancellationToken() {
                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }

                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }
                })
                .addOnCompleteListener(task -> {
                    Location currentLocation = task.getResult();
                    if(currentLocation == null){
                        Toast.makeText(this, "Please enable GPS and try again", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(trip.getStatus() != TripStatus.CLIENT_DROP) {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(isUnderRadius(currentLocation, trip.getDropLocation(), 100.0)) {
                        new MaterialAlertDialogBuilder(TripActivity.this)
                                .setTitle("Confirm")
                                .setMessage("Are you sure you want to end this ride?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    deleteTrip(TripStatus.COMPLETED);
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {})
                                .show();
                    } else {
                        new MaterialAlertDialogBuilder(TripActivity.this)
                                .setTitle("Confirm")
                                .setMessage("You have not reached the drop location yet. Are you sure you want to end?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    deleteTrip(TripStatus.CANCELLED);
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {})
                                .show();
                    }

                });
    }
    
    private void checkDriverPickupStatus(){

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationProviderClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnCompleteListener(task -> {
            Location currentLocation = task.getResult();
            if(currentLocation == null){
                Toast.makeText(this, "Please enable GPS and try again", Toast.LENGTH_SHORT).show();
                return;
            }

            if(trip.getStatus() != TripStatus.CLIENT_PICKUP){
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }

            if(isUnderRadius(currentLocation, trip.getPickupLocation(), 100.0)){
                requestRoutePreview(gMap,trip.getDropLocation(), REQUEST_DROP);

                viewBinding.pickupComplete.setVisibility(View.GONE);
                viewBinding.navigateButton.setVisibility(View.VISIBLE);
            }
            else{
                Toast.makeText(this, "You have not reached client location yet", Toast.LENGTH_SHORT).show();
            }
            
        });         
    }

    private LatLng getRouteMidpoint(List<LatLng> points) {
        return points.get(Math.floorDiv(points.size(), 2));
    }

    private void loadTrip(@NonNull String ownerId, @NonNull String tripId) {
        FirebaseDatabase database = FirebaseService.getInstance().getDatabaseInstance();

        database
                .getReference()
                .getRoot()
                .child("trips")
                .child(ownerId)
                .child(tripId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            trip = snapshot.getValue(Trip.class);
                            assert trip != null;
                            viewBinding.pickupComplete.setEnabled(trip.getStatus() == TripStatus.CLIENT_PICKUP);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteTrip(TripStatus terminalState) {
        if(trip == null) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            return;
        }

        OneTimeWorkRequest tripDeleteRequest = new OneTimeWorkRequest.Builder(RemoveTripWorker.class)
                .setInputData(new Data.Builder()
                        .putString("owner_id", trip.getOwnerId())
                        .putString("trip_id", trip.getId())
                        .build())
                .build();

        OneTimeWorkRequest addHistoryRequest = new OneTimeWorkRequest.Builder(AddTripHistoryWorker.class)
                .setInputData(new Data.Builder()
                        .putAll(trip.toMap())
                        .putString("terminal_state", terminalState.name())
                        .putAll(tripPolylines)
                        .build())
                .build();

        WorkManager.getInstance(getApplicationContext())
                .beginWith(tripDeleteRequest)
                .then(addHistoryRequest)
                .enqueue();

        WorkManager.getInstance(getApplicationContext())
                .getWorkInfoByIdLiveData(addHistoryRequest.getId())
                .observe(this, workInfo -> {
                    if(workInfo.getState().isFinished() && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        Toast.makeText(this, "Trip Completed!", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                });
    }

    private String formatETA(String rawString) {
        int totalSeconds;

        try {
            totalSeconds = Integer.parseInt(rawString.replace("s", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid eta string");
        }

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder stringBuilder = new StringBuilder();

        if(days > 0) {
            stringBuilder.append(days).append(" day").append(days > 1 ? "s" : "");
        }
        if(hours > 0) {
            if(stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(hours).append(" hour").append(hours > 1 ? "s" : "");
        }
        if(minutes > 0) {
            if(stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
        }
        if(seconds > 0) {
            if(stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(seconds).append(" second").append(seconds > 1 ? "s" : "");
        }

        return stringBuilder.toString();

    }
}