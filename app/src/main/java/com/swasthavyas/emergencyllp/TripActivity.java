package com.swasthavyas.emergencyllp;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StrokeStyle;
import com.google.android.gms.maps.model.StyleSpan;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.trip.worker.FetchRoutePreviewWorker;
import com.swasthavyas.emergencyllp.databinding.ActivityTripBinding;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

import java.util.List;


public class
TripActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityTripBinding viewBinding;
    private GoogleMap gMap;
    private Trip trip;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityTripBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });


        String ownerId = getIntent().getStringExtra("owner_id");
        String tripId = getIntent().getStringExtra("trip_id");

        if(ownerId == null || tripId == null) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SupportMapFragment mapFragment = viewBinding.navigationMap.getFragment();
        mapFragment.getMapAsync(this);
        loadTrip(ownerId, tripId);

        viewBinding.navigateButton.setOnClickListener(v -> {
            updateDriverStatus(TripStatus.CLIENT_PICKUP);
            if(trip == null || currentLocation == null) {
                Toast.makeText(this, "Current location or trip is null", Toast.LENGTH_SHORT).show();
                return;
            }

//            @SuppressLint("DefaultLocale") String currentLocationCoordinates = String.format("%f,%f", currentLocation.getLatitude(), currentLocation.getLongitude());
            @SuppressLint("DefaultLocale") String pickupLocationCoordinates = String.format("%f,%f", trip.getPickupLocation().get(0), trip.getPickupLocation().get(1));

            String uriString = "google.navigation:q="+pickupLocationCoordinates;

            Uri pickupLocationNavigationUri = Uri.parse(uriString);
            Intent directionsIntent = new Intent(Intent.ACTION_VIEW, pickupLocationNavigationUri);
            directionsIntent.setPackage("com.google.android.apps.maps");
            startActivity(directionsIntent);
        });
        viewBinding.pickupComplete.setOnClickListener(v -> {
            checkDriverLocation();
        });

    }

    private void updateDriverStatus(TripStatus status) {
        if(trip == null) {
            return;
        }

        FirebaseDatabase database = FirebaseService.getInstance().getDatabaseInstance();

        database
                .getReference()
                .getRoot()
                .child(trip.getOwnerId())
                .child(trip.getId())
                .child("status")
                .setValue(status)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Log.d(TAG, "updateDriverStatus: driver status updated");
                    }
                    else {
                        Log.d(TAG, "updateDriverStatus: " + task.getException());
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        switch (trip.getStatus()){
            case CLIENT_PICKUP:
                requestRoutePreview(googleMap,trip.getPickupLocation());
                break;
            case CLIENT_DROP:
                requestRoutePreview(googleMap,trip.getDropLocation());
        }

    }

    private void requestRoutePreview(@NonNull GoogleMap googleMap,List<Double> dest) {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        client.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, new CancellationToken() {
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
           if(task.isSuccessful()) {

               Location location = task.getResult();
               currentLocation = location;
               if(location != null) {
                    Log.d("MYAPP", "Location:" + String.format("(%f, %f)", location.getLatitude(), location.getLongitude()));
               } else {
                   Log.d("MYAPP", "Location: null");
               }

               double[] origin = new double[2];
               double[] destination = new double[2];

               if(trip != null) {
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
                           if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                               Toast.makeText(this, "Route received", Toast.LENGTH_SHORT).show();

                               String encodedPolyline = workInfo.getOutputData().getString("polyline");
                               String rawDuration = workInfo.getOutputData().getString("duration");

                               if(encodedPolyline == null || rawDuration == null) {
                                   Log.d(TAG, "onMapReady: " + workInfo.getOutputData());
                                   return;
                               }
                               String duration = formatETA(rawDuration);
                               List<LatLng> coordinates =  PolyUtil.decode(encodedPolyline);

                               PolylineOptions options = new PolylineOptions()
                                       .addAll(coordinates)
                                       .addSpan(new StyleSpan(StrokeStyle.gradientBuilder(Color.RED, Color.YELLOW).build()));

                               Polyline routePreview = googleMap.addPolyline(options);

                               routePreview.setColor(Color.BLUE);

                               LatLng midpoint = getRouteMidpoint(routePreview.getPoints());

                               googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
                               googleMap.addMarker(new MarkerOptions().position(new LatLng(trip.getPickupLocation().get(0), trip.getPickupLocation().get(1))));

                               googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(midpoint, 15f));
                               viewBinding.navigateButton.setEnabled(true);
                               viewBinding.eta.setText(getString(R.string.eta_text, duration));
                               viewBinding.tripProgressbar.setVisibility(View.GONE);
                           }
                           else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                               Toast.makeText(this, workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                               viewBinding.tripProgressbar.setVisibility(View.GONE);
                           }
                           else if(workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                               viewBinding.navigateButton.setEnabled(false);
                               viewBinding.tripProgressbar.setVisibility(View.VISIBLE);
                           }
                       });
           }
        });
    }

    private boolean isUnderRadius(Location location){
        
        if(trip == null) {
            return false;
        }
        LatLng currentLat = new LatLng(location.getLatitude(),location.getLongitude());
        LatLng pickupLat = new LatLng(trip.getPickupLocation().get(0),trip.getPickupLocation().get(1));

        double distance = SphericalUtil.computeDistanceBetween(currentLat,pickupLat);
        
        if(distance > 100.0){
            return false;
        }
        
        return true;
        
    }
    
    private void checkDriverLocation(){
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        client.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, new CancellationToken() {
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

            if(isUnderRadius(currentLocation)){
                updateDriverStatus(TripStatus.CLIENT_DROP);
                requestRoutePreview(gMap,trip.getDropLocation());
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
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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