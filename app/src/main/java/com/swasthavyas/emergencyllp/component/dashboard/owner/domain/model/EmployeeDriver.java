package com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model;

import java.util.Map;

public class EmployeeDriver {
    private String driverId;
    private String name;
    private String userId;
    private int age;

    private String phoneNumber;

    private EmployeeDriver(String name, String driverId, String userId, int age, String phoneNumber) {
        this.name = name;
        this.driverId = driverId;
        this.userId = userId;
        this.age = age;
        this.phoneNumber = phoneNumber;
    }

    public static EmployeeDriver createFromMap(Map<String, Object> map) {
        String driverId = (String) map.get("driver_id");
        String userId = (String) map.get("user_id");
        Object ageReceived = map.get("age");
        int age = -1;

        if(ageReceived instanceof Double) {
            age = ((Double) ageReceived).intValue();
        }
        else if(ageReceived instanceof Integer) {
            age = ((Integer) ageReceived).intValue();
        } else if (ageReceived instanceof Long) {
            age = ((Long) ageReceived).intValue();
        }


        String phoneNumber = (String) map.get("phone_number");
        String name = (String) map.get("name");

        return new EmployeeDriver(name, driverId, userId, age, phoneNumber);
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
