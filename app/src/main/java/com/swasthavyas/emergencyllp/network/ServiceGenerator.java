package com.swasthavyas.emergencyllp.network;

import retrofit2.Retrofit;

public class ServiceGenerator {
    public static <S> S createService(Class<S> serviceClass, String baseUrl) {
        Retrofit retrofit  = RetrofitClient.getClient(baseUrl);

        return retrofit.create(serviceClass);
    }
}
