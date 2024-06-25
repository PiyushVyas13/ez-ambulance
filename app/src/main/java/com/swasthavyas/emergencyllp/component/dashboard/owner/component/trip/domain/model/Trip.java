package com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

import java.util.List;
import java.util.Map;
@IgnoreExtraProperties
public class Trip {
    private String id;
    private String customerName;
    private int customerAge;
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


    private Trip(String tripId, String customerName, int customerAge, double price, List<Double> pickupLocation, List<Double> dropLocation, String pickupLocationAddress, String dropLocationAddress, String assignedDriverId, String assignedAmbulanceId, String ownerId, TripStatus status, boolean isEmergencyRide) {
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
        this.isEmergencyRide = isEmergencyRide;
    }

    public Trip() {
        // required public no-args constructor
    }

    @SuppressWarnings({"unchecked"})
    public static Trip createFromMap(Map<String, Object> map) {
        String tripId = (String) map.get("trip_id");
        String customerName = (String) map.get("customer_name");
        int customerAge = (int) map.get("customer_age");
        double estimatedPrice = (double) map.get("estimated_price");
        String pickupLocationAddress = (String) map.get("pickup_location_address");
        String dropLocationAddress = (String) map.get("drop_location_address");
        List<Double> pickupLocationCoordinates = (List<Double>) map.get("pickup_location_coordinates");
        List<Double> dropLocationCoordinates = (List<Double>) map.get("drop_location_coordinates");
        boolean isEmergencyRide = (boolean) map.get("is_emergency_ride");
        TripStatus tripStatus = (TripStatus) map.get("trip_status");
        String ownerId = (String) map.get("owner_id");
        String driverId = (String) map.get("assigned_driver_id");
        String ambulanceId = (String) map.get("ambulance_id");


        return new Trip(tripId, customerName, customerAge, estimatedPrice, pickupLocationCoordinates, dropLocationCoordinates, pickupLocationAddress, dropLocationAddress, driverId, ambulanceId ,ownerId, tripStatus,isEmergencyRide);
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

    public int getCustomerAge() {
        return customerAge;
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
}
