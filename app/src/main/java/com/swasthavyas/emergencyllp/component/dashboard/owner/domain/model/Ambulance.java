package com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model;

import com.swasthavyas.emergencyllp.util.types.AmbulanceType;

import java.util.Map;

public class Ambulance {


    private String id;
    private String ownerId;
    private AmbulanceType ambulanceType;
    private String vehicleNumber;
    private String vehicleType;

    private Ambulance() {

    }

    private Ambulance(String id, String ownerId, AmbulanceType ambulanceType, String vehicleNumber, String vehicleType)  {
        this.id = id;
        this.ownerId = ownerId;
        this.ambulanceType = ambulanceType;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;

    }

    public static Ambulance createFromMap(Map<String, Object> map) {
        String id = (String) map.get("id");
        String ownerId = (String) map.get("owner_id");
        AmbulanceType ambulanceType = AmbulanceType.valueOf((String) map.get("ambulance_type"));
        String vehicleNumber = (String) map.get("vehicle_number");
        String vehicleType = (String) map.get("vehicle_type");

        return new Ambulance(id, ownerId, ambulanceType, vehicleNumber, vehicleType);
    }

    public static Ambulance getInstance() {
        return new Ambulance();
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public AmbulanceType getAmbulanceType() {
        return ambulanceType;
    }

    public void setAmbulanceType(AmbulanceType ambulanceType) {
        this.ambulanceType = ambulanceType;
    }


    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}
