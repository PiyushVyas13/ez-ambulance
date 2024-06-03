package com.swasthavyas.emergencyllp.component.registration.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.util.types.UserRole;

import java.util.HashMap;
import java.util.Map;

public class RegistrationViewModel extends ViewModel {

    private final MutableLiveData<UserRole> userRoleLiveData;
    private final MutableLiveData<String> aadhaarNumberLiveData;
    private final MutableLiveData<Uri> aadhaarUri;

    private final MutableLiveData<Map<String, Object>> driverAmbulance;

    public RegistrationViewModel() {
        userRoleLiveData = new MutableLiveData<>(UserRole.UNASSIGNED);
        aadhaarNumberLiveData = new MutableLiveData<>(null);
        driverAmbulance = new MutableLiveData<>(new HashMap<>());
        aadhaarUri = new MutableLiveData<>(null);
    }

    public void setUserRole(UserRole role) {
        userRoleLiveData.setValue(role);
    }
    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumberLiveData.setValue(aadhaarNumber);
    }

    public LiveData<UserRole> getUserRole() {
        return this.userRoleLiveData;
    }


    public MutableLiveData<String> getAadhaarNumberLiveData() {
        return aadhaarNumberLiveData;
    }

    public LiveData<Map<String, Object>> getDriverAmbulance() {
        return this.driverAmbulance;
    }

    public void setDriverAmbulance(Map<String, Object> map) {
        this.driverAmbulance.setValue(map);
    }

    public MutableLiveData<Uri> getAadhaarUri() {
        return this.aadhaarUri;
    }

    public void setAadhaarUri(Uri aadhaarUri) {
        this.aadhaarUri.setValue(aadhaarUri);
    }

}
