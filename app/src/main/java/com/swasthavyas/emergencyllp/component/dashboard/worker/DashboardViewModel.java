package com.swasthavyas.emergencyllp.component.dashboard.worker;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Data;

import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.ui.AddDriverFragment;

import java.util.HashMap;
import java.util.Map;

public class DashboardViewModel extends ViewModel {


    private final MutableLiveData<AddDriverFragment.RegistrationStep> registrationStep;
    private final MutableLiveData<Map<String, Data>> registrationData;

    public DashboardViewModel() {
        registrationStep = new MutableLiveData<>(AddDriverFragment.RegistrationStep.PERSONAL_DETAIL);
        registrationData = new MutableLiveData<>(null);
    }

    public MutableLiveData<AddDriverFragment.RegistrationStep> getRegistrationStep() {
        return this.registrationStep;
    }

    public void setRegistrationStep(AddDriverFragment.RegistrationStep registrationStep) {
        this.registrationStep.setValue(registrationStep);
    }


    public MutableLiveData<Map<String, Data>> getRegistrationData() {
        return registrationData;
    }

    public void setRegistrationData(@NonNull  String step, @NonNull Data data) {
        Map<String, Data> currentValue = this.registrationData.getValue();

        if(currentValue == null) {
            currentValue = new HashMap<>();
        }

        currentValue.putIfAbsent(step, data);

        this.registrationData.setValue(currentValue);


    }
}
