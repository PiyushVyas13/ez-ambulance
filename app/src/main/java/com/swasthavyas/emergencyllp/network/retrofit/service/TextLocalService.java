package com.swasthavyas.emergencyllp.network.retrofit.service;

import com.swasthavyas.emergencyllp.network.retrofit.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.POST;

public final class TextLocalService {
    private static final String TEXTLOCAL_BASE_URL = "https://api.textlocal.in/";
    private static TextLocalService instance;

    private final TextLocalApi textLocalApi;


    private TextLocalService() {
        textLocalApi = ServiceGenerator.createService(TextLocalApi.class, TEXTLOCAL_BASE_URL);
    }

    public static TextLocalService getInstance() {
        if(instance == null) {
            synchronized (TextLocalService.class) {
                if(instance == null) {
                    instance = new TextLocalService();
                }
            }
        }
        return instance;
    }

    public void sendConfirmationMessage(SendSmsRequest smsRequest, Callback<String> callback) {
        Call<String> sendSmsCall = textLocalApi.sendSms(smsRequest);
        sendSmsCall.enqueue(callback);
    }

    public static class SendSmsRequest {
        private final String apikey;
        private final String numbers;
        private final String sender;
        private final String message;

        public SendSmsRequest(String apiKey, String numbers, String sender, String message) {
            this.apikey = apiKey;
            this.numbers = numbers;
            this.sender = sender;
            this.message = message;
        }
    }

    public interface TextLocalApi {
        @POST("send")
        Call<String> sendSms(@Body SendSmsRequest sendSmsRequest);
    }


}
