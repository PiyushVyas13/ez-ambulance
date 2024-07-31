package com.swasthavyas.emergencyllp.util.asyncwork;

public interface TokenCallback {
    void onTokenReceived(String accessToken);
    void onTokenFailure(String error);
}
