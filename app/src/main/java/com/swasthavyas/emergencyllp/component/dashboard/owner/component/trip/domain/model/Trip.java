package com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model;

import androidx.annotation.NonNull;

import com.google.common.base.MoreObjects;
import com.google.firebase.Timestamp;
import com.google.firebase.database.IgnoreExtraProperties;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@IgnoreExtraProperties
public class Trip {
    private String id;
    private String customerName;
    private long customerAge;
    private double price;
    private List<Double> pickupLocation;
    private List<Double> dropLocation;
    private String pickupLocationAddress;
    private String dropLocationAddress;
    private String assignedDriverId;
    private String assignedAmbulanceId;
    private String ownerId;
    private boolean isEmergencyRide;
    private TripStatus status;
    private String customerMobile;

    private Timestamp createdAt;


    private Trip(String tripId, String customerName, long customerAge, double price, List<Double> pickupLocation, List<Double> dropLocation, String pickupLocationAddress, String dropLocationAddress, String assignedDriverId, String assignedAmbulanceId, String ownerId, TripStatus status, String customerMobile, boolean isEmergencyRide, Timestamp createdAt) {
        this.id = tripId;
        this.customerName = customerName;
        this.customerAge = customerAge;
        this.price = price;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.pickupLocationAddress = pickupLocationAddress;
        this.dropLocationAddress = dropLocationAddress;
        this.assignedAmbulanceId = assignedAmbulanceId;
        this.assignedDriverId = assignedDriverId;
        this.status = status;
        this.ownerId = ownerId;
        this.customerMobile = customerMobile;
        this.isEmergencyRide = isEmergencyRide;
        this.createdAt = createdAt;
    }

    public Trip() {
        // required public no-args constructor
    }

    public Timestamp getCreationDate() {
        return createdAt;
    }

    @SuppressWarnings({"unchecked"})
    public static Trip createFromMap(Map<String, Object> map) {
        String tripId = (String) map.get(ModelColumns.ID);
        String customerName = (String) map.get(ModelColumns.CUSTOMER_NAME);
        Object customerAgeRec = map.get(ModelColumns.CUSTOMER_AGE);

        long customerAge = 0;

        if(customerAgeRec instanceof Long) {
            customerAge = (long) customerAgeRec;
        } else if (customerAgeRec instanceof Integer) {
            customerAge = (int) customerAgeRec;
        }


        double estimatedPrice = (double) map.get(ModelColumns.PRICE);
        String pickupLocationAddress = (String) map.get(ModelColumns.PICKUP_LOCATION_ADDRESS);
        String dropLocationAddress = (String) map.get(ModelColumns.DROP_LOCATION_ADDRESS);
        List<Double> pickupLocationCoordinates = (List<Double>) map.get(ModelColumns.PICKUP_LOCATION);
        List<Double> dropLocationCoordinates = (List<Double>) map.get(ModelColumns.DROP_LOCATION);


        boolean isEmergencyRide;

        if(map.containsKey(ModelColumns.IS_EMERGENCY_RIDE)) {
            isEmergencyRide= (boolean) map.get(ModelColumns.IS_EMERGENCY_RIDE);
        } else {
            isEmergencyRide = (boolean) map.get("isEmergencyRide");
        }


        Object tripStatusRec = map.get(ModelColumns.STATUS);
        TripStatus tripStatus = null;

        if(tripStatusRec instanceof String) {
            tripStatus = TripStatus.valueOf(((String) tripStatusRec));
        } else if(tripStatusRec instanceof TripStatus) {
            tripStatus = (TripStatus) tripStatusRec;
        }


        String ownerId = (String) map.get(ModelColumns.OWNER_ID);
        String driverId = (String) map.get(ModelColumns.ASSIGNED_DRIVER_ID);
        String ambulanceId = (String) map.get(ModelColumns.ASSIGNED_AMBULANCE_ID);
        String customerMobile = (String) map.get(ModelColumns.CUSTOMER_MOBILE);
        Timestamp createdAt = (Timestamp) map.get(ModelColumns.CREATED_AT);

        return new Trip(tripId, customerName, customerAge, estimatedPrice, pickupLocationCoordinates, dropLocationCoordinates, pickupLocationAddress, dropLocationAddress, driverId, ambulanceId ,ownerId, tripStatus, customerMobile, isEmergencyRide, createdAt);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> tripMap = new HashMap<>();

        tripMap.put("id", this.id);
        tripMap.put("customerName", this.customerName);
        tripMap.put("customerAge", this.customerAge);
        tripMap.put("price", this.price);

        double[] pickupLocation = new double[] {this.pickupLocation.get(0), this.pickupLocation.get(1)};
        double[] dropLocation = new double[] {this.dropLocation.get(0), this.pickupLocation.get(1)};

        tripMap.put("pickupLocation", pickupLocation);
        tripMap.put("dropLocation", dropLocation);
        tripMap.put("pickupLocationAddress", this.pickupLocationAddress);
        tripMap.put("dropLocationAddress", this.dropLocationAddress);
        tripMap.put("assignedAmbulanceId", this.assignedAmbulanceId);
        tripMap.put("assignedDriverId", this.assignedDriverId);
        tripMap.put("status", this.status.name());
        tripMap.put("ownerId", this.ownerId);
        tripMap.put("customerMobile", this.customerMobile);
        tripMap.put("isEmergencyRide", this.isEmergencyRide);
        tripMap.put("createdAt",this.createdAt);

        return tripMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public long getCustomerAge() {
        return customerAge;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String mobile) {
        this.customerMobile = mobile;
    }

    public void setCustomerAge(int customerAge) {
        this.customerAge = customerAge;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<Double> getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(List<Double> pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public List<Double> getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(List<Double> dropLocation) {
        this.dropLocation = dropLocation;
    }

    public String getPickupLocationAddress() {
        return pickupLocationAddress;
    }

    public void setPickupLocationAddress(String pickupLocationAddress) {
        this.pickupLocationAddress = pickupLocationAddress;
    }

    public String getDropLocationAddress() {
        return dropLocationAddress;
    }

    public void setDropLocationAddress(String dropLocationAddress) {
        this.dropLocationAddress = dropLocationAddress;
    }

    public String getAssignedDriverId() {
        return assignedDriverId;
    }

    public void setAssignedDriverId(String assignedDriverId) {
        this.assignedDriverId = assignedDriverId;
    }

    public String getAssignedAmbulanceId() {
        return assignedAmbulanceId;
    }

    public void setAssignedAmbulanceId(String assignedAmbulanceId) {
        this.assignedAmbulanceId = assignedAmbulanceId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isEmergencyRide() {
        return isEmergencyRide;
    }

    public void setEmergencyRide(boolean emergencyRide) {
        isEmergencyRide = emergencyRide;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    @NonNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("customerName", customerName)
                .add("customerAge", customerAge)
                .add("price", price)
                .add("pickupLocation", pickupLocation)
                .add("dropLocation", dropLocation)
                .add("pickupLocationAddress", pickupLocationAddress)
                .add("dropLocationAddress", dropLocationAddress)
                .add("assignedDriverId", assignedDriverId)
                .add("assignedAmbulanceId", assignedAmbulanceId)
                .add("ownerId", ownerId)
                .add("isEmergencyRide", isEmergencyRide)
                .add("status", status)
                .add("customerMobile", customerMobile)
                .add("createdAt", createdAt)
                .toString();
    }

    public static class ModelColumns {
        public static final String ID = "id";
        public static final String CUSTOMER_NAME = "customerName";
        public static final String CUSTOMER_AGE = "customerAge";
        public static final String CUSTOMER_MOBILE = "customerMobile";
        public static final String PRICE = "price";
        public static final String PICKUP_LOCATION_ADDRESS = "pickupLocationAddress";
        public static final String PICKUP_LOCATION = "pickupLocation";
        public static final String DROP_LOCATION_ADDRESS = "dropLocationAddress";
        public static final String DROP_LOCATION = "dropLocation";
        public static final String ASSIGNED_AMBULANCE_ID = "assignedAmbulanceId";
        public static final String ASSIGNED_DRIVER_ID = "assignedDriverId";
        public static final String STATUS = "status";
        public static final String OWNER_ID = "ownerId";
        public static final String IS_EMERGENCY_RIDE = "emergencyRide";
        public static final String CREATED_AT = "creationDate";

    }
}
