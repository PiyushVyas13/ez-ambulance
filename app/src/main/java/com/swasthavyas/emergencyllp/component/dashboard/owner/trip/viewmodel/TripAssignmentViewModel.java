package com.swasthavyas.emergencyllp.component.dashboard.owner.trip.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.component.dashboard.owner.trip.domain.TripRegistrationStep;

public class TripAssignmentViewModel extends ViewModel {
    private final MutableLiveData<TripRegistrationStep> currentStep;

    public TripAssignmentViewModel() {
        currentStep = new MutableLiveData<>(TripRegistrationStep.CUSTOMER_INFO);
    }

    public LiveData<TripRegistrationStep> getCurrentStep() {
        return this.currentStep;
    }

    public void setCurrentStep(TripRegistrationStep step) {
        this.currentStep.setValue(step);
    }
}
