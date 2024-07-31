package com.swasthavyas.emergencyllp.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.swasthavyas.emergencyllp.network.retrofit.service.OlaMapsAuthService;
import com.swasthavyas.emergencyllp.util.asyncwork.TokenCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TokenManager {
    private static final String OLA_MAPS_CLIENT_ID = "86e1613d-e884-47bc-a90f-4f27cc3e9dfd";
    private static final String OLA_MAPS_CLIENT_SECRET = "zgVtW7wLY8BDKUh89oi61T1FEAtuxvx9";
    private static final String TAG = "TokenManager";

    private static TokenManager instance;
    private OlaMapsAuthService authService;
    private String accessToken;
    private boolean isFetchingToken = false;

    private TokenManager() {
        authService = OlaMapsAuthService.getInstance();
    }

    public static TokenManager getInstance() {
        if(instance == null) {
            synchronized (TokenManager.class) {
                if(instance == null) {
                    instance = new TokenManager();
                }
            }
        }
        return instance;
    }

    public void getAccessToken(TokenCallback callback) {
        if(accessToken == null || isTokenExpired()) {
            fetchNewAccessToken(callback);
        } else {
            callback.onTokenReceived(accessToken);
        }
    }

    private void fetchNewAccessToken(TokenCallback callback) {
        if(isFetchingToken) return;

        isFetchingToken = true;

        OlaMapsAuthService.FetchTokenRequest fetchTokenRequest = new OlaMapsAuthService.FetchTokenRequest(
                "client_credentials",
                "openid",
                OLA_MAPS_CLIENT_ID,
                OLA_MAPS_CLIENT_SECRET
        );

        authService.fetchAccessToken(fetchTokenRequest, new Callback<OlaMapsAuthService.FetchTokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<OlaMapsAuthService.FetchTokenResponse> call, @NonNull Response<OlaMapsAuthService.FetchTokenResponse> response) {
                isFetchingToken = false;
                if(response.isSuccessful() && response.body() != null) {
                    accessToken = response.body().getAccessToken();
                    callback.onTokenReceived(accessToken);
                } else {
                    Log.d(TAG, "onResponse: " + response.body());
                    callback.onTokenFailure("Token fetch failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<OlaMapsAuthService.FetchTokenResponse> call, @NonNull Throwable throwable) {
                isFetchingToken = false;
                callback.onTokenFailure(throwable.getMessage());
            }
        });
    }

    private boolean isTokenExpired() {
        //TODO: check if token is expired. (store token expiry in preferences/localdb/cache)
        return false;
    }
}
