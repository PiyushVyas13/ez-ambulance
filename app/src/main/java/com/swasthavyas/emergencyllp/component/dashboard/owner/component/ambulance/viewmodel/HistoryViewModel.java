package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.TripHistory;

public class HistoryViewModel extends ViewModel {
    private final MutableLiveData<TripHistory> selectedTripHistory;
    
    public HistoryViewModel() {
        selectedTripHistory = new MutableLiveData<>(null);
    }

    public MutableLiveData<TripHistory> getSelectedTripHistory() {
        return selectedTripHistory;
    }
    
    public void setSelectedTripHistory(TripHistory history) {
        this.selectedTripHistory.setValue(history);
    }
}
