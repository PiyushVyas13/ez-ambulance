package com.swasthavyas.emergencyllp.network;

public class SendSmsRequest {
    private String apikey;
    private String numbers;
    private String sender;
    private String message;

    public SendSmsRequest(String apiKey, String numbers, String sender, String message) {
        this.apikey = apiKey;
        this.numbers = numbers;
        this.sender = sender;
        this.message = message;
    }


    public String getApiKey() {
        return apikey;
    }

    public void setApiKey(String apiKey) {
        this.apikey = apiKey;
    }

    public String getNumbers() {
        return numbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}