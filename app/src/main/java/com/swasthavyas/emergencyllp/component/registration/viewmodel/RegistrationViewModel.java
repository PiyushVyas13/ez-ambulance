package com.swasthavyas.emergencyllp.component.registration.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.util.types.UserRole;

public class RegistrationViewModel extends ViewModel {

    private final MutableLiveData<UserRole> userRoleLiveData;
    private final MutableLiveData<String> aadhaarNumberLiveData;

    public RegistrationViewModel() {
        userRoleLiveData = new MutableLiveData<>(UserRole.UNASSIGNED);
        aadhaarNumberLiveData = new MutableLiveData<>(null);
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
}
