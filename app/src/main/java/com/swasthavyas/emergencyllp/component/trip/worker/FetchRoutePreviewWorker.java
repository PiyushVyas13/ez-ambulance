package com.swasthavyas.emergencyllp.component.trip.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.swasthavyas.emergencyllp.network.model.GoogleMapsDirectionsRequest;
import com.swasthavyas.emergencyllp.network.model.GoogleMapsDirectionsResponse;
import com.swasthavyas.emergencyllp.network.service.GoogleMapsRouteService;
import com.swasthavyas.emergencyllp.util.asyncwork.ListenableWorkerAdapter;
import com.swasthavyas.emergencyllp.util.asyncwork.NetworkResultCallback;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FetchRoutePreviewWorker extends ListenableWorkerAdapter {
    private static final String BASE_URL = "";

    public FetchRoutePreviewWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private GoogleMapsDirectionsRequest createRouteRequest(double[] origin, double[] destination) {
        GoogleMapsDirectionsRequest request = new GoogleMapsDirectionsRequest();

        GoogleMapsDirectionsRequest.LatLngWrapper originLatLng = new GoogleMapsDirectionsRequest.LatLngWrapper();
        originLatLng.latitude = origin[0];
        originLatLng.longitude = origin[1];


        GoogleMapsDirectionsRequest.CoordinateWrapper originWrapper = new GoogleMapsDirectionsRequest.CoordinateWrapper();
        originWrapper.latLng = originLatLng;

        GoogleMapsDirectionsRequest.LatLngWrapper destLatLng = new GoogleMapsDirectionsRequest.LatLngWrapper();
        destLatLng.latitude = destination[0];
        destLatLng.longitude = destination[1];

        GoogleMapsDirectionsRequest.CoordinateWrapper destWrapper = new GoogleMapsDirectionsRequest.CoordinateWrapper();
        destWrapper.latLng = destLatLng;

        GoogleMapsDirectionsRequest.LocationWrapper originLocation = new GoogleMapsDirectionsRequest.LocationWrapper();
        originLocation.location = originWrapper;

        GoogleMapsDirectionsRequest.LocationWrapper destinationLocation = new GoogleMapsDirectionsRequest.LocationWrapper();
        destinationLocation.location = destWrapper;

        request.origin = originLocation;
        request.destination = destinationLocation;
        request.travelMode = "DRIVE";
        request.routingPreference = "TRAFFIC_AWARE";
        request.computeAlternativeRoutes = false;

        GoogleMapsDirectionsRequest.RouteModifier routeModifiers = new GoogleMapsDirectionsRequest.RouteModifier();
        routeModifiers.avoidFerries = false;
        routeModifiers.avoidHighways = false;
        routeModifiers.avoidTolls = false;

        request.routeModifiers = routeModifiers;
        request.languageCode = "en-US";
        request.units = "IMPERIAL";

        return request;

    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        double[] originLatLng = getInputData().getDoubleArray("origin");
        double[] destinationLatLng = getInputData().getDoubleArray("destination");

        if(originLatLng == null || destinationLatLng == null) {
            callback.onFailure(new IllegalArgumentException("origin and/or destination coordinates not provided"));
            return;
        }

        GoogleMapsRouteService routeService = GoogleMapsRouteService.getInstance();

        routeService.getRoutePolyline(createRouteRequest(originLatLng, destinationLatLng), new Callback<GoogleMapsDirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GoogleMapsDirectionsResponse> call, @NonNull Response<GoogleMapsDirectionsResponse> response) {
                GoogleMapsDirectionsResponse routeResponse = response.body();

                if(routeResponse != null) {
                    Log.d("MYAPP", "onResponse: " + routeResponse.routes[0].polyline.encodedPolyline);
                    callback.onSuccess(new Data.Builder()
                            .putString("duration",routeResponse.routes[0].duration)
                            .putString("polyline", routeResponse.routes[0].polyline.encodedPolyline)
                            .build());
                }
                else {
                    callback.onFailure(new Exception("Something went wrong!"));
                }

            }

            @Override
            public void onFailure(@NonNull Call<GoogleMapsDirectionsResponse> call, @NonNull Throwable throwable) {
                callback.onFailure(new Exception(throwable.getMessage()));
            }
        });

    }
}
