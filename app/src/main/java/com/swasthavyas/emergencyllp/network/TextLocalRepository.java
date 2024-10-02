package com.swasthavyas.emergencyllp.network;

import retrofit2.Call;
import retrofit2.Callback;

public class TextLocalRepository {
    private static final String BASE_URL = "https://api.textlocal.in/";
    private final TextLocalService textLocalService;

    public TextLocalRepository() {
        textLocalService = ServiceGenerator.createService(TextLocalService.class, BASE_URL);
    }

    public void sendSMS(SendSmsRequest smsRequest, Callback<String> callback) {
        Call<String> smsCall = textLocalService.sendSms(smsRequest);
        smsCall.enqueue(callback);
    }
}
