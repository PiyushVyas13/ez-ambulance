package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;

public class AmbulanceViewModel extends ViewModel {

    private final MutableLiveData<Ambulance> currentAmbulance;
    private final MutableLiveData<EmployeeDriver> assignedDriver;

    public AmbulanceViewModel() {
        currentAmbulance = new MutableLiveData<>(null);
        assignedDriver = new MutableLiveData<>(null);
    }


    public void setCurrentAmbulance(Ambulance ambulance) {
        this.currentAmbulance.setValue(ambulance);
    }

    public void setAssignedDriver(EmployeeDriver driver) {
        this.assignedDriver.setValue(driver);
    }

    public MutableLiveData<Ambulance> getCurrentAmbulance() {return this.currentAmbulance;}
    public MutableLiveData<EmployeeDriver> getAssignedDriver() {return assignedDriver;}
}
