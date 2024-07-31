package com.swasthavyas.emergencyllp.component.trip.domain;

import android.util.Log;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.ola.maps.navigation.v5.model.route.Leg;
import com.ola.maps.navigation.v5.model.route.Route;
import com.ola.maps.navigation.v5.model.route.RouteInfoData;
import com.swasthavyas.emergencyllp.network.TokenManager;
import com.ola.maps.navigation.v5.navigation.direction.DirectionTransformationKt;
import com.swasthavyas.emergencyllp.network.retrofit.service.OlaMapsService;
import com.swasthavyas.emergencyllp.util.asyncwork.TokenCallback;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OlaMapsRepository {
    private static final String BASE_URL = "https://api.olamaps.io/";
    private static final String OLA_MAPS_API_KEY = "gZ3KRlqC78I2Hl9mEQ2iJ0Xmon8JqyugCdEPyx6S";
    private static final String TAG = "OlaMapsRepository";

    private final TokenManager tokenManager;
    private final OlaMapsService olaMapsService;

    private final Retrofit retrofit;

    public OlaMapsRepository() {
        tokenManager = TokenManager.getInstance();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build();

        olaMapsService = retrofit.create(OlaMapsService.class);
    }

    public void transformRouteData(String origin, String destination, Callback<RouteInfoData> callback) {

        tokenManager.getAccessToken(new TokenCallback() {
            @Override
            public void onTokenReceived(String accessToken) {

                Call<RouteInfoData> routeCall = olaMapsService.getRoutesData(accessToken, OLA_MAPS_API_KEY, origin, destination);
                routeCall.enqueue(callback);
            }

            @Override
            public void onTokenFailure(String error) {
                Log.d(TAG, "onTokenFailure: " + error);
            }
        });
    }
}
