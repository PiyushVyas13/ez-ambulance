package com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;

import java.util.ArrayList;
import java.util.List;


public class Owner {
    private String id;
    private String userId;
    private String aadhaarNumber;

    private final MutableLiveData<List<Ambulance>> ambulances;
    private final MutableLiveData<List<EmployeeDriver>> employees;

    public Owner(@NonNull String id, @NonNull String userId, @NonNull String aadhaarNumber) {
        this.id = id;
        this.userId = userId;
        this.aadhaarNumber = aadhaarNumber;
        this.ambulances = new MutableLiveData<>(new ArrayList<>());
        this.employees = new MutableLiveData<>(new ArrayList<>());
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public LiveData<List<Ambulance>> getAmbulances() {
        return ambulances;
    }

    public void setAmbulances(List<Ambulance> ambulances) {
        this.ambulances.setValue(ambulances);
    }

    public void addAmbulance(Ambulance ambulance) {
        if(this.ambulances.getValue() != null) {
            this.ambulances.getValue().add(ambulance);
            setAmbulances(this.ambulances.getValue());
        }
        else {
            throw new NullPointerException("ambulances is null.");
        }

    }

    public void deleteAmbulance(int position) {
        if(this.ambulances.getValue() == null) {
            throw new NullPointerException("Ambulances is null.");
        }

        this.ambulances.getValue().remove(position);

        this.ambulances.setValue(this.ambulances.getValue());
    }

    public LiveData<List<EmployeeDriver>> getEmployees() {
        return this.employees;
    }

    public void setEmployees(List<EmployeeDriver> employees) {
        this.employees.setValue(employees);
    }

    public void addEmployee(EmployeeDriver employee) {
        if(this.employees.getValue() != null) {
            this.employees.getValue().add(employee);
            setEmployees(this.employees.getValue());
        }
        else {
            throw new NullPointerException("employees is null.");
        }
    }

    public void deleteEmployee(int position) {
        if(this.employees.getValue() == null) {
            throw new NullPointerException("Employee is null");
        }

        this.employees.getValue().remove(position);
        this.employees.setValue(this.employees.getValue());
    }
}
