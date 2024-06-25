package com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;

public class EmployeeViewModel extends ViewModel {
    private final MutableLiveData<EmployeeDriver> employee;

    public EmployeeViewModel() {
        employee = new MutableLiveData<>(null);
    }

    public MutableLiveData<EmployeeDriver> getCurrentEmployee() {
        return this.employee;
    }

    public void setEmployee(EmployeeDriver employee) {
        this.employee.setValue(employee);
    }
}
