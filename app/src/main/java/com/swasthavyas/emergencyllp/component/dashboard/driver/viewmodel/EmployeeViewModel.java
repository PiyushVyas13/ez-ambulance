package com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel;


import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.WorkManager;

import com.google.firebase.storage.FirebaseStorage;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;

public class EmployeeViewModel extends ViewModel {
    private final MutableLiveData<EmployeeDriver> employee;
    private final MutableLiveData<Long> rideCount;
    private final MutableLiveData<Long> lastWeekRideCount;
    private final MutableLiveData<Double> totalEarning;
    private final MutableLiveData<Double> lastWeekRideEarning;

    public EmployeeViewModel() {
        employee = new MutableLiveData<>(null);
        rideCount = new MutableLiveData<>(0L);
        lastWeekRideCount = new MutableLiveData<>(0L);
        totalEarning = new MutableLiveData<>(0.0);
        lastWeekRideEarning = new MutableLiveData<>(0.0);
    }

    public MutableLiveData<EmployeeDriver> getCurrentEmployee() {
        return this.employee;
    }

    public void setEmployee(EmployeeDriver employee) {
        this.employee.setValue(employee);
    }

    public MutableLiveData<Long> getRideCount() {return this.rideCount;}
    public MutableLiveData<Double> getTotalEarning() {return this.totalEarning;}

    public void updateRideCount(long count) {
        if(getCurrentEmployee().getValue() == null) {
            throw new NullPointerException("Employee does not exist yet.");
        }
        this.rideCount.setValue(count);
    }

    public void updateDriverEarning(double earning) {
        if(getCurrentEmployee().getValue() == null) {
            throw new NullPointerException("Employee does not exist yet");
        }
        this.totalEarning.setValue(earning);
    }

    public MutableLiveData<Long> getLastWeekRideCount() {
        return lastWeekRideCount;
    }

    public MutableLiveData<Double> getLastWeekRideEarning() {
        return lastWeekRideEarning;
    }

    public void setLastWeekRideCount(long count) {
        if(getCurrentEmployee().getValue() == null) {
            throw new NullPointerException("Employee does not exist yet");
        }
        this.lastWeekRideCount.setValue(count);
    }

    public void setLastWeekRideEarning(double earning) {
        if(getCurrentEmployee().getValue() == null) {
            throw new NullPointerException("Employee does not exist yet");
        }
        this.lastWeekRideEarning.setValue(earning);
    }

    public void updateDriverProfile(String profileRef) {
        EmployeeDriver currentEmployee = employee.getValue();

        if(currentEmployee != null) {
            currentEmployee.setProfileImageRef(FirebaseStorage.getInstance().getReferenceFromUrl(profileRef));
            setEmployee(currentEmployee);
        }
    }
}
