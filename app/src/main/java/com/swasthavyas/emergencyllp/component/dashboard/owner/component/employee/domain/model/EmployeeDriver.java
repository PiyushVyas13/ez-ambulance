package com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model;

import java.util.Map;

public class EmployeeDriver {
    private String driverId;
    private String name;
    private String userId;
    private int age;

    private String email;

    private String aadhaarImageRef;
    private String licenceImageRef;
    private String assignedAmbulanceNumber;

    private String phoneNumber;

    private EmployeeDriver(String name, String email, String driverId, String userId, int age, String phoneNumber, String assignedAmbulanceNumber, String aadhaarImageRef, String licenceImageRef) {
        this.name = name;
        this.email = email;
        this.driverId = driverId;
        this.userId = userId;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.aadhaarImageRef = aadhaarImageRef;
        this.licenceImageRef = licenceImageRef;
        this.assignedAmbulanceNumber = assignedAmbulanceNumber;
    }

    public static EmployeeDriver createFromMap(Map<String, Object> map) {
        String driverId = (String) map.get(ModelColumns.DRIVER_ID);
        String userId = (String) map.get(ModelColumns.USER_ID);
        Object ageReceived = map.get(ModelColumns.AGE);
        int age = -1;

        if(ageReceived instanceof Double) {
            age = ((Double) ageReceived).intValue();
        }
        else if(ageReceived instanceof Integer) {
            age = ((Integer) ageReceived).intValue();
        } else if (ageReceived instanceof Long) {
            age = ((Long) ageReceived).intValue();
        }


        String phoneNumber = (String) map.get(ModelColumns.PHONE_NUMBER);
        String name = (String) map.get(ModelColumns.NAME);
        String aadhaarImageRef = (String) map.get(ModelColumns.AADHAAR_IMAGE_REF);
        String licenceImageRef = (String) map.get(ModelColumns.LICENSE_IMAGE_REF);
        String email = (String) map.get(ModelColumns.EMAIL);
        String assignedAmbulanceNumber = (String) map.get(ModelColumns.ASSIGNED_AMBULANCE_NUMBER);

        return new EmployeeDriver(name, email, driverId, userId, age, phoneNumber, assignedAmbulanceNumber, aadhaarImageRef, licenceImageRef);
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

    public String getAadhaarImageRef() {
        return aadhaarImageRef;
    }

    public void setAadhaarImageRef(String aadhaarImageRef) {
        this.aadhaarImageRef = aadhaarImageRef;
    }

    public String getLicenceImageRef() {
        return licenceImageRef;
    }

    public void setLicenceImageRef(String licenceImageRef) {
        this.licenceImageRef = licenceImageRef;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAssignedAmbulanceNumber() {
        return assignedAmbulanceNumber;
    }

    public void setAssignedAmbulanceNumber(String assignedAmbulanceNumber) {
        this.assignedAmbulanceNumber = assignedAmbulanceNumber;
    }

    public static class ModelColumns {
        public static final String EMAIL = "email";
        public static final String DRIVER_ID = "driver_id";
        public static final String USER_ID = "user_id";
        public static final String AGE = "age";
        public static final String NAME = "name";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String ASSIGNED_AMBULANCE_NUMBER = "assigned_ambulance_number";
        public static final String AADHAAR_NUMBER = "aadhaar_number";
        public static final String AADHAAR_IMAGE_REF = "aadhaar_image_ref";
        public static final String LICENSE_IMAGE_REF = "license_image_ref";

    }
}
