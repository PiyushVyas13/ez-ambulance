package com.swasthavyas.emergencyllp.component.trip.domain;

import com.swasthavyas.emergencyllp.network.retrofit.ServiceGenerator;
import com.swasthavyas.emergencyllp.network.retrofit.model.SendSmsRequest;
import com.swasthavyas.emergencyllp.network.retrofit.service.TextLocalService;

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
