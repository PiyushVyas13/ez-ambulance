package com.swasthavyas.emergencyllp.component.trip.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.ola.maps.mapslibrary.models.OlaLatLng;
import com.ola.maps.mapslibrary.models.OlaMapsConfig;
import com.ola.maps.navigation.ui.v5.MapStatusCallback;
import com.ola.maps.navigation.v5.navigation.OlaMapView;
import com.swasthavyas.emergencyllp.network.AccessTokenInterceptor;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.databinding.ActivityTripBinding;


public class TripActivity extends AppCompatActivity implements MapStatusCallback {
    private ActivityTripBinding viewBinding;
    private OlaMapView olaMapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityTripBinding.inflate(getLayoutInflater());
        olaMapView = viewBinding.navigationMap;

        EdgeToEdge.enable(this);
        setContentView(viewBinding.getRoot());
        viewBinding.navigationMap.onCreate(savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });


        olaMapView.initialize(this, new OlaMapsConfig.Builder()
                        .setApplicationContext(getApplicationContext())
                        .setClientId("ORG-Ayh5S3jVQH")
                        .setMapBaseUrl("https://api.olamaps.io")
                        .setInterceptor(new AccessTokenInterceptor())
                        .setZoomLevel(15.0)
                        .setMinZoomLevel(3.0)
                        .setMaxZoomLevel(21.0)
                        .build());

    }

    @Override
    public void onMapReady() {
        olaMapView.addMarker(new LatLng(21.2720, 79.4864), "hello", R.drawable.ambulance, false);
        olaMapView.moveCameraToLatLong(new OlaLatLng(21.2720, 79.4864, 12.0), 15.0, 500);
    }

    @Override
    public void onMapLoadFailed(String s) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        olaMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        olaMapView.onDestroy();
    }
}