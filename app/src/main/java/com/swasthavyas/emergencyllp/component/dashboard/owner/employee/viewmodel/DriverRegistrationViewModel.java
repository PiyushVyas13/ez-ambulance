package com.swasthavyas.emergencyllp.component.dashboard.owner.employee.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;

import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.model.RegistrationStep;

import java.util.HashMap;
import java.util.Map;

public class DriverRegistrationViewModel extends ViewModel {


    private final MutableLiveData<RegistrationStep> registrationStep;
    private final MutableLiveData<Map<RegistrationStep, Data>> registrationData;

    public DriverRegistrationViewModel() {
        registrationStep = new MutableLiveData<>(RegistrationStep.PERSONAL_DETAILS);
        registrationData = new MutableLiveData<>(null);
    }

    public MutableLiveData<RegistrationStep> getRegistrationStep() {
        return this.registrationStep;
    }

    public void setRegistrationStep(RegistrationStep registrationStep) {
        this.registrationStep.setValue(registrationStep);
    }


    public MutableLiveData<Map<RegistrationStep, Data>> getRegistrationData() {
        return registrationData;
    }

    public void setRegistrationData(@NonNull RegistrationStep step, @NonNull Data data) {
        Map<RegistrationStep, Data> currentValue = this.registrationData.getValue();

        if(currentValue == null) {
            currentValue = new HashMap<>();
        }

        currentValue.putIfAbsent(step, data);

        this.registrationData.setValue(currentValue);


    }
}
