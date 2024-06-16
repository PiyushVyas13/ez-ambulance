package com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.DriverRegistrationStep;

import java.util.HashMap;
import java.util.Map;

public class DriverRegistrationViewModel extends ViewModel {


    private final MutableLiveData<DriverRegistrationStep> registrationStep;
    private final MutableLiveData<Map<DriverRegistrationStep, Data>> registrationData;

    public DriverRegistrationViewModel() {
        registrationStep = new MutableLiveData<>(DriverRegistrationStep.PERSONAL_DETAILS);
        registrationData = new MutableLiveData<>(null);
    }

    public MutableLiveData<DriverRegistrationStep> getRegistrationStep() {
        return this.registrationStep;
    }

    public void setRegistrationStep(DriverRegistrationStep driverRegistrationStep) {
        this.registrationStep.setValue(driverRegistrationStep);
    }


    public MutableLiveData<Map<DriverRegistrationStep, Data>> getRegistrationData() {
        return registrationData;
    }

    public void setRegistrationData(@NonNull DriverRegistrationStep step, @NonNull Data data) {
        Map<DriverRegistrationStep, Data> currentValue = this.registrationData.getValue();

        if(currentValue == null) {
            currentValue = new HashMap<>();
        }

        currentValue.putIfAbsent(step, data);

        this.registrationData.setValue(currentValue);


    }
}
