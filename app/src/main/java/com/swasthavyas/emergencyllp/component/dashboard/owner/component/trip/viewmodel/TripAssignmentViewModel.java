package com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.viewmodel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.TripRegistrationStep;

import java.util.HashMap;
import java.util.Map;

public class TripAssignmentViewModel extends ViewModel {
    private final MutableLiveData<TripRegistrationStep> currentStep;
    private final MutableLiveData<Map<TripRegistrationStep, Data>> registrationData;

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

    public LiveData<Map<TripRegistrationStep, Data>> getRegistrationData() {
        return this.registrationData;
    }

    public void addRegistrationData(TripRegistrationStep step, Data data) {
        Map<TripRegistrationStep, Data> currentValue = this.registrationData.getValue();
        if(currentValue == null) {
            currentValue = new HashMap<>();
        }
        currentValue.putIfAbsent(step, data);

        this.registrationData.setValue(currentValue);
    }
}
