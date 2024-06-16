package com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model;

import com.google.type.LatLng;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;

public class Trip {
    private String customerName;
    private int customerAge;
    private double price;
    private LatLng pickupLocation;
    private LatLng dropLocation;
    private EmployeeDriver assignedDriver;
    private Ambulance assignedAmbulance;
    private String ownerId;
    private boolean isEmergencyRide;
}
