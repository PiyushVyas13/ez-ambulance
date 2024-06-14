package com.swasthavyas.emergencyllp.component.dashboard.owner.trip.domain.model;

import com.google.type.LatLng;
import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.model.EmployeeDriver;

public class Trip {
    private String customerName;
    private int customerAge;
    private double price;
    private LatLng pickupLocation;
    private LatLng dropLocation;
    private EmployeeDriver assignedDriver;
    private Ambulance assignedAmbulance;
    private String ownerId;
}
