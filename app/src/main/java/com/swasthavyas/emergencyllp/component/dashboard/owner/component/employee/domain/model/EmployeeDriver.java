package com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.common.primitives.Doubles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeDriver implements Parcelable {
    private String driverId;
    private String name;
    private String userId;
    private int age;

    private String email;

    private String aadhaarImageRef;
    private String licenceImageRef;
    private String assignedAmbulanceNumber;

    private String phoneNumber;
    private String ownerId;
    private String aadhaarNumber;
    private List<Double> lastLocation;

    protected EmployeeDriver(Parcel in) {
        email = in.readString();
        driverId = in.readString();
        userId = in.readString();
        name = in.readString();
        age = in.readInt();
        aadhaarImageRef = in.readString();
        licenceImageRef = in.readString();
        assignedAmbulanceNumber = in.readString();
        phoneNumber = in.readString();
        ownerId = in.readString();
        aadhaarNumber = in.readString();
        List<Double> location = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            in.readList(location, null, Double.class);
        }
        else {
            in.readList(location, null);
        }

        lastLocation = location;
    }

    public static final Creator<EmployeeDriver> CREATOR = new Creator<EmployeeDriver>() {
        @Override
        public EmployeeDriver createFromParcel(Parcel in) {
            return new EmployeeDriver(in);
        }

        @Override
        public EmployeeDriver[] newArray(int size) {
            return new EmployeeDriver[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "EmployeeDriver{" + "driverId='" + driverId + '\'' +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", aadhaarNumber='" + aadhaarNumber + '\'' +
                ", aadhaarImageRef='" + aadhaarImageRef + '\'' +
                ", licenceImageRef='" + licenceImageRef + '\'' +
                ", assignedAmbulanceNumber='" + assignedAmbulanceNumber + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", lastLocation='" + "[" + lastLocation.get(0) + ", " + lastLocation.get(1) + "]" + '\'' +
                '}';
    }

    private EmployeeDriver(String name, String email, String ownerId, String driverId, String userId, int age, String phoneNumber, String assignedAmbulanceNumber, String aadhaarNumber, String aadhaarImageRef, String licenceImageRef, List<Double> lastLocation) {
        this.email = email;
        this.driverId = driverId;
        this.userId = userId;
        this.age = age;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.assignedAmbulanceNumber = assignedAmbulanceNumber;
        this.aadhaarNumber = aadhaarNumber;
        this.aadhaarImageRef = aadhaarImageRef;
        this.licenceImageRef = licenceImageRef;
        this.ownerId = ownerId;
        this.lastLocation = lastLocation;

    }

    @SuppressWarnings("unchecked")
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
        String ownerId = (String) map.get(ModelColumns.OWNER_ID);
        String aadhaarNumber = (String) map.get(ModelColumns.AADHAAR_NUMBER);
        List<Double> lastLocation = (List<Double>) map.get(ModelColumns.LAST_LOCATION);

        return new EmployeeDriver(name, email, ownerId, driverId, userId, age, phoneNumber, assignedAmbulanceNumber, aadhaarNumber, aadhaarImageRef, licenceImageRef, lastLocation);
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

    public String getAadhaarNumber() {
        return this.aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public void setAssignedAmbulanceNumber(String assignedAmbulanceNumber) {
        this.assignedAmbulanceNumber = assignedAmbulanceNumber;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<Double> getLastLocation() {
        return this.lastLocation;
    }

    public void setLastLocation(List<Double> location) {
        this.lastLocation = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.email);
        dest.writeString(this.driverId);
        dest.writeString(this.userId);
        dest.writeInt(this.age);
        dest.writeString(this.name);
        dest.writeString(this.phoneNumber);
        dest.writeString(this.assignedAmbulanceNumber);
        dest.writeString(this.aadhaarNumber);
        dest.writeString(this.aadhaarImageRef);
        dest.writeString(this.licenceImageRef);
        dest.writeString(this.ownerId);
        dest.writeList(this.lastLocation);
    }

    public Map<String, Object> getKeyValueMap() {
        Map<String, Object> map = new HashMap<>();


        map.put(ModelColumns.EMAIL, email);
        map.put(ModelColumns.DRIVER_ID, driverId);
        map.put(ModelColumns.USER_ID, userId);
        map.put(ModelColumns.AGE, age);
        map.put(ModelColumns.NAME, name);
        map.put(ModelColumns.PHONE_NUMBER, phoneNumber);
        map.put(ModelColumns.ASSIGNED_AMBULANCE_NUMBER, assignedAmbulanceNumber);
        map.put(ModelColumns.AADHAAR_NUMBER, aadhaarNumber);
        map.put(ModelColumns.AADHAAR_IMAGE_REF, aadhaarImageRef);
        map.put(ModelColumns.LICENSE_IMAGE_REF, licenceImageRef);
        map.put(ModelColumns.OWNER_ID, ownerId);

        return map;
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
        public static final String OWNER_ID = "owner_id";
        public static final String LAST_LOCATION = "last_location";

    }
}
