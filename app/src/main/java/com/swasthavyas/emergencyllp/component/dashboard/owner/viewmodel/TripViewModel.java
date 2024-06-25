package com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TripViewModel extends ViewModel {
    private Map<String, MutableLiveData<Trip>> activeTrips;
    private final MutableLiveData<List<Trip>> activeTripsLiveData;


    public TripViewModel() {
        activeTrips =  new HashMap<>();
        activeTripsLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    public MutableLiveData<Trip> getTripByIdLiveData(String tripId) {
        if(!activeTrips.containsKey(tripId)) {
            throw new IllegalArgumentException("Trip with given ID does not exist");
        }
        return activeTrips.get(tripId);
    }

    public void addTrip(Trip trip) {
        MutableLiveData<Trip> tripLiveData = new MutableLiveData<>(trip);

        this.activeTrips.putIfAbsent(trip.getId(), tripLiveData);
        this.activeTripsLiveData.getValue().add(trip);
        this.activeTripsLiveData.setValue(this.activeTripsLiveData.getValue());
    }

    public void setActiveTrips(List<Trip> trips) {
        Map<String, MutableLiveData<Trip>> tripMap = new HashMap<>();

        for(Trip trip : trips) {
            MutableLiveData<Trip> tripMutableLiveData = new MutableLiveData<>(trip);
            tripMap.put(trip.getId(), tripMutableLiveData);
        }

        this.activeTrips = tripMap;
        this.activeTripsLiveData.setValue(trips);
    }

    public MutableLiveData<List<Trip>> getActiveTripsLiveData() {
        return this.activeTripsLiveData;
    }

    public void removeTrip(String tripId) {
        List<Trip> trips = this.activeTripsLiveData.getValue();

        List<Trip> updatedTrips = trips.stream().filter(trip -> !trip.getId().equals(tripId)).collect(Collectors.toList());

        this.activeTripsLiveData.setValue(updatedTrips);

    }

}
