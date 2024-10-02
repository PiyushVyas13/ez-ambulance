package com.swasthavyas.emergencyllp.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TextLocalService {
    @POST("send")
    Call<String> sendSms(@Body SendSmsRequest smsRequest);
}
