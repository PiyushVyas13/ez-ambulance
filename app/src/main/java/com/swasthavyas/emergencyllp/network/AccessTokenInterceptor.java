package com.swasthavyas.emergencyllp.network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AccessTokenInterceptor implements Interceptor{

    private static final String OLA_MAPS_API_KEY = "gZ3KRlqC78I2Hl9mEQ2iJ0Xmon8JqyugCdEPyx6S";
    private static final String TAG = "INTERCEPTOR";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Request originalRequest = chain.request();
        HttpUrl originalUrl = originalRequest.url();

        if(originalRequest.header("Authorization") != null) {
            return chain.proceed(originalRequest);
        }

        HttpUrl newUrl = originalUrl.newBuilder()
                .addQueryParameter("api_key", OLA_MAPS_API_KEY)
                .build();

        Request newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .method(originalRequest.method(), originalRequest.body())
                .build();

        Log.d(TAG, "Request Headers: " + newRequest.headers());
        Log.d(TAG, "Request URL: " + newRequest.url());

        // Log.d(TAG, "response with token: " + response.body().string());
        return chain.proceed(newRequest);

    }

}
