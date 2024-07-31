package com.swasthavyas.emergencyllp.component.trip.ui;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.ola.maps.mapslibrary.models.OlaLatLng;
import com.ola.maps.mapslibrary.models.OlaMapsConfig;
import com.ola.maps.navigation.ui.v5.MapStatusCallback;
import com.ola.maps.navigation.ui.v5.NavigationStatusCallback;
import com.ola.maps.navigation.ui.v5.NavigationViewOptions;
import com.ola.maps.navigation.ui.v5.instruction.InstructionModel;
import com.ola.maps.navigation.ui.v5.listeners.RouteProgressListener;
import com.ola.maps.navigation.v5.model.route.NavigationErrorInfo;
import com.ola.maps.navigation.v5.model.route.RouteInfoData;
import com.ola.maps.navigation.v5.navigation.MapboxNavigationOptions;
import com.ola.maps.navigation.v5.navigation.NavigationMapRoute;
import com.ola.maps.navigation.v5.navigation.OlaMapView;
import com.ola.maps.navigation.v5.navigation.direction.DirectionTransformationKt;
import com.swasthavyas.emergencyllp.component.trip.domain.OlaMapsRepository;
import com.swasthavyas.emergencyllp.network.AccessTokenInterceptor;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.databinding.ActivityTripBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TripActivity extends AppCompatActivity implements MapStatusCallback {
    private static final String TAG = "TripActivity";

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

        String origin = "21.1786813,79.0565564";
        String destination = "21.184705,79.058819";

        NavigationMapRoute navigationMapRoute = olaMapView.getNavigationMapRoute();
        List<DirectionsRoute> directionsRouteList = new ArrayList<>();

        OlaMapsRepository repository = new OlaMapsRepository();

        repository.transformRouteData(origin, destination, new Callback<RouteInfoData>() {
            @Override
            public void onResponse(@NonNull Call<RouteInfoData> call, @NonNull Response<RouteInfoData> response) {
                if(response.isSuccessful() && response.body() != null) {
                   DirectionsRoute directionsRoute = DirectionTransformationKt.transform(response.body());


                    Log.d(TAG, "onResponse: " + directionsRoute.legs());
                    Log.d(TAG, "onResponse: " + directionsRoute.geometry());

                   NavigationViewOptions options = NavigationViewOptions.builder()
                           .directionsRoute(directionsRoute)
                           .navigationOptions(MapboxNavigationOptions.builder().isDebugLoggingEnabled(true).build())
                           .build();

                   olaMapView.addRouteProgressListener(new RouteProgressListener() {
                       @Override
                       public void onRouteProgressChange(InstructionModel instructionModel) {
                           Toast.makeText(TripActivity.this, "onPRogress Chaged", Toast.LENGTH_SHORT).show();
                       }

                       @Override
                       public void onOffRoute(Location location) {
                            Toast.makeText(getApplicationContext(), "onOffToute", Toast.LENGTH_SHORT).show();
                       }

                       @Override
                       public void onArrival() {
                            Toast.makeText(getApplicationContext(), "onArrival", Toast.LENGTH_SHORT).show();
                       }
                   });
                   olaMapView.registerNavigationStatusCallback(new NavigationStatusCallback() {
                       @Override
                       public void onNavigationReady() {
                           Toast.makeText(TripActivity.this, "onNavreadt", Toast.LENGTH_SHORT).show();
                       }

                       @Override
                       public void onNavigationInitError(NavigationErrorInfo navigationErrorInfo) {
                           Log.d(TAG, "onNavigationInitError: " + navigationErrorInfo.getNavigationErrors().getErrorMessage());
                       }
                   });
                   olaMapView.startNavigation(options, false, false, null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RouteInfoData> call, @NonNull Throwable throwable) {
                Log.d(TAG, "onFailure: " + throwable.getMessage());
            }
        });
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