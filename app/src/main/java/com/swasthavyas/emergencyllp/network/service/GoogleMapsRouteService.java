package com.swasthavyas.emergencyllp.network.service;


import android.util.Log;

import com.swasthavyas.emergencyllp.network.ServiceGenerator;
import com.swasthavyas.emergencyllp.network.model.GoogleMapsDirectionsRequest;
import com.swasthavyas.emergencyllp.network.model.GoogleMapsDirectionsResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public final class GoogleMapsRouteService {
    private static final String BASE_URL = "https://routes.googleapis.com/";
    private static GoogleMapsRouteService instance;

    private final GoogleMapsRouteApi routeApi;


    private GoogleMapsRouteService() {
        routeApi = ServiceGenerator.createService(GoogleMapsRouteApi.class, BASE_URL);
    }

    public static GoogleMapsRouteService getInstance() {
        if(instance == null) {
            synchronized (GoogleMapsRouteService.class) {
                if(instance == null) {
                    instance = new GoogleMapsRouteService();
                }
            }
        }
        return instance;
    }

    public interface GoogleMapsRouteApi {
        @POST("directions/v2:computeRoutes")
        Call<GoogleMapsDirectionsResponse> getRoute(@HeaderMap Map<String, String> headers, @Body GoogleMapsDirectionsRequest directionsRequest);
    }

    public void getRoutePolyline(GoogleMapsDirectionsRequest request, Callback<GoogleMapsDirectionsResponse> callback) {
        // String apiKey = getApiKey();


        Map<String, String> headers = new HashMap<>();


        headers.put("Content-Type", "application/json");
        headers.put("X-Goog-Api-Key", "AIzaSyAi55rLBOI8bUDiK7ls9HhkDggCEuLhQUQ");
        headers.put("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline");

        Call<GoogleMapsDirectionsResponse> call = routeApi.getRoute(headers, request);
        call.enqueue(callback);
    }

    private String getApiKey() {
        Properties properties = new Properties();

        try{
            FileInputStream fis = new FileInputStream("secrets.properties");
            properties.load(fis);
            fis.close();
        } catch (IOException e) {
            Log.d("MYAPP", "getApiKey: " + e);
        }

        return properties.getProperty("MAPS_API_KEY");
    }

}
