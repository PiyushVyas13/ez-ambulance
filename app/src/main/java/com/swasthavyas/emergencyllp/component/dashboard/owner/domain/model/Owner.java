package com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;


public class Owner {
    private String id;
    private String userId;
    private String aadhaarNumber;

    private final MutableLiveData<List<Ambulance>> ambulances;

    public Owner(@NonNull String id, @NonNull String userId, @NonNull String aadhaarNumber) {
        this.id = id;
        this.userId = userId;
        this.aadhaarNumber = aadhaarNumber;
        this.ambulances = new MutableLiveData<>(new ArrayList<>());
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public LiveData<List<Ambulance>> getAmbulances() {
        return ambulances;
    }

    public void setAmbulances(List<Ambulance> ambulances) {
        this.ambulances.setValue(ambulances);
    }

    public void addAmbulance(Ambulance ambulance) {
        if(this.ambulances.getValue() != null) {
            this.ambulances.getValue().add(ambulance);
            setAmbulances(this.ambulances.getValue());
        }
        else {
            throw new NullPointerException("ambulances is null.");
        }

    }
}
