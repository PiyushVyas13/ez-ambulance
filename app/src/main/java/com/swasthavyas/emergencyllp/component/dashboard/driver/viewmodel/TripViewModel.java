package com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.FirebaseDatabase;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

public class TripViewModel extends ViewModel {
    private final MutableLiveData<Trip> activeTrip;
    private final FirebaseDatabase database;

    public TripViewModel() {
        activeTrip = new MutableLiveData<>(null);
        database = FirebaseService.getInstance().getDatabaseInstance();
    }

    public MutableLiveData<Trip> getActiveTrip() {return activeTrip;}
    public void setActiveTrip(Trip trip) {this.activeTrip.setValue(trip);}

    public void updateTripStatus(TripStatus status) {
        Trip trip = this.activeTrip.getValue();
        if(trip == null) {
            return;
        }
        database
                .getReference()
                .getRoot()
                .child("trips")
                .child(trip.getOwnerId())
                .child(trip.getId())
                .child("status")
                .setValue(status)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        trip.setStatus(status);
                        activeTrip.setValue(trip);
                    }
                });

    }
}
