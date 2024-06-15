package com.swasthavyas.emergencyllp.component.dashboard.owner.trip.viewmodel;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.component.dashboard.owner.trip.domain.TripRegistrationStep;

import java.util.HashMap;
import java.util.Map;

public class TripAssignmentViewModel extends ViewModel {
    private final MutableLiveData<TripRegistrationStep> currentStep;
    private final MutableLiveData<Map<TripRegistrationStep, Bundle>> registrationData;

    public TripAssignmentViewModel() {
        currentStep = new MutableLiveData<>(TripRegistrationStep.CUSTOMER_INFO);
        registrationData = new MutableLiveData<>(new HashMap<>());
    }

    public LiveData<TripRegistrationStep> getCurrentStep() {
        return this.currentStep;
    }

    public void setCurrentStep(TripRegistrationStep step) {
        this.currentStep.setValue(step);
    }

    public LiveData<Map<TripRegistrationStep, Bundle>> getRegistrationData() {
        return this.registrationData;
    }

    public void addRegistrationData(TripRegistrationStep step, Bundle data) {
        Map<TripRegistrationStep, Bundle> currentValue = this.registrationData.getValue();
        if(currentValue == null) {
            currentValue = new HashMap<>();
        }
        currentValue.putIfAbsent(step, data);

        this.registrationData.setValue(currentValue);
    }
}
