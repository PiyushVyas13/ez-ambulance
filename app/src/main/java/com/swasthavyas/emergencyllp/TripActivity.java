package com.swasthavyas.emergencyllp;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationRequest;
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
import androidx.work.WorkRequest;

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
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.trip.worker.FetchRoutePreviewWorker;
import com.swasthavyas.emergencyllp.databinding.ActivityTripBinding;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

import java.util.List;


public class TripActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityTripBinding viewBinding;
    private GoogleMap gMap;
    private Trip trip;

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

    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        LatLng latLng = new LatLng(20.5558, 78.6304);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        googleMap.addMarker(new MarkerOptions().position(latLng));

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
               if(location != null) {
                    Log.d("MYAPP", "Location:" + String.format("(%f, %f)", location.getLatitude(), location.getLongitude()));
               } else {
                   Log.d("MYAPP", "Location: null");
               }

               double[] origin = new double[2];
               double[] destination = new double[2];

               if(trip != null) {
                   origin = new double[]{location == null ? 21.1458 : location.getLatitude(), location == null ? 79.0882 : location.getLongitude()};
                   destination = new double[]{trip.getDropLocation().get(0), trip.getDropLocation().get(1)};
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
                               String duration = workInfo.getOutputData().getString("duration");

                               if(encodedPolyline == null || duration == null) {
                                   return;
                               }
                               List<LatLng> coordinates =  PolyUtil.decode(encodedPolyline);

                               PolylineOptions options = new PolylineOptions()
                                       .addAll(coordinates)
                                       .addSpan(new StyleSpan(StrokeStyle.gradientBuilder(Color.RED, Color.YELLOW).build()));

                               Polyline routePreview = googleMap.addPolyline(options);

                               viewBinding.eta.setText(getString(R.string.eta_text, duration));
                               viewBinding.tripProgressbar.setVisibility(View.GONE);
                           }
                           else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                               Toast.makeText(this, workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                               viewBinding.tripProgressbar.setVisibility(View.GONE);
                           }
                           else if(workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                               viewBinding.tripProgressbar.setVisibility(View.VISIBLE);
                           }
                       });
           }
        });

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
}