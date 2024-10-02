package com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.util.types.DriverStatus;
import com.swasthavyas.emergencyllp.util.types.UserRole;


public class DashboardViewModel extends ViewModel {
    private final MutableLiveData<UserRole> userRole;
    private final MutableLiveData<DriverStatus> driverStatus;

    public DashboardViewModel() {
        userRole = new MutableLiveData<>(UserRole.UNASSIGNED);
        driverStatus = new MutableLiveData<>(DriverStatus.OFF_DUTY);
    }

    public MutableLiveData<UserRole> getUserRole() {
        return this.userRole;
    }
    public MutableLiveData<DriverStatus> getDriverStatus() {
        return this.driverStatus;
    }

    public void setDriverStatus(DriverStatus status) {
        this.driverStatus.setValue(status);
    }

    public void setUserRole(UserRole role) {
        this.userRole.setValue(role);
    }
}
