package com.swasthavyas.emergencyllp.network.retrofit.service;


import com.swasthavyas.emergencyllp.network.retrofit.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public final class OlaMapsAuthService {
    private static final String BASE_URL = "https://account.olamaps.io/";
    private static OlaMapsAuthService instance;

    private final OlaMapsAuthApi olaMapsAuthApi;

    private OlaMapsAuthService() {
        olaMapsAuthApi = ServiceGenerator.createService(OlaMapsAuthApi.class, BASE_URL);
    }

    public static OlaMapsAuthService getInstance() {
        if(instance == null) {
            synchronized (OlaMapsAuthService.class) {
                if(instance == null) {
                    instance = new OlaMapsAuthService();
                }
            }
        }
        return instance;
    }

    public void fetchAccessToken(FetchTokenRequest authRequest, Callback<FetchTokenResponse> callback) {
        Call<FetchTokenResponse> authResponseCall =olaMapsAuthApi.getAccessToken(authRequest.getGrantType(), authRequest.getScope(), authRequest.getClientId(), authRequest.getClientSecret());
        authResponseCall.enqueue(callback);
    }

    public interface OlaMapsAuthApi {
        @FormUrlEncoded
        @POST("realms/olamaps/protocol/openid-connect/token")
        Call<FetchTokenResponse> getAccessToken(@Field("grant_type") String grantType, @Field("scope") String scope, @Field("client_id") String clientId, @Field("client_secret") String clientSecret);
    }

    public static class FetchTokenResponse {
        private final String access_token;

        public FetchTokenResponse(String accessToken) {
            access_token = accessToken;
        }

        public String getAccessToken() {
            return access_token;
        }
    }

    public static class FetchTokenRequest {
        private final String grant_type;
        private final String scope;
        private final String client_id;
        private final String client_secret;

        public FetchTokenRequest(String grantType, String scope, String clientId, String clientSecret) {
            grant_type = grantType;
            this.scope = scope;
            client_id = clientId;
            client_secret = clientSecret;
        }

        public String getGrantType() {
            return grant_type;
        }

        public String getScope() {
            return scope;
        }

        public String getClientId() {
            return client_id;
        }

        public String getClientSecret() {
            return client_secret;
        }
    }

}


