package com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.model;

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
        String id = (String) map.get(ModelColumns.ID);
        String ownerId = (String) map.get(ModelColumns.OWNER_ID);
        AmbulanceType ambulanceType = AmbulanceType.valueOf((String) map.get(ModelColumns.AMBULANCE_TYPE));
        String vehicleNumber = (String) map.get(ModelColumns.VEHICLE_NUMBER);
        String vehicleType = (String) map.get(ModelColumns.AMBULANCE_TYPE);

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

    public static class ModelColumns {
        public static final String ID = "id";
        public static final String OWNER_ID = "owner_id";
        public static final String AMBULANCE_TYPE = "ambulanceType";
        public static final String VEHICLE_NUMBER = "vehicle_number";
        public static final String VEHICLE_TYPE = "vehicle_type";
        public static final String IMAGE_REF = "image_ref";
    }
}
