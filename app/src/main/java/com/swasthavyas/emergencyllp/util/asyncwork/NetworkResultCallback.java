package com.swasthavyas.emergencyllp.util.asyncwork;

import androidx.work.Data;

public interface NetworkResultCallback {
    void onSuccess(Data data);
    void onFailure(Exception exception);
}
