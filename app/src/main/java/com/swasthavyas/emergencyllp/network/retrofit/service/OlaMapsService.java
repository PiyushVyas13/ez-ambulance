package com.swasthavyas.emergencyllp.network.retrofit.service;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.ola.maps.navigation.v5.model.route.RouteInfoData;
import com.swasthavyas.emergencyllp.network.retrofit.model.GetDirectionsRouteRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

// This is not implemented as a class because it requires input provided from the user (UI thread)
public interface OlaMapsService {
    @POST("/routing/v1/directions")
    Call<RouteInfoData> getRoutesData(@Header("Authorization") String accessToken, @Query("api_key") String apiKey, @Query("origin") String origin, @Query("destination") String destination);
}
