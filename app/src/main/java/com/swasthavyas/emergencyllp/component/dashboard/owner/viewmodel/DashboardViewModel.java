package com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.component.dashboard.owner.ui.HomeFragment;

public class DashboardViewModel extends ViewModel {
    private final MutableLiveData<String> displayMode;

    public DashboardViewModel() {
        displayMode = new MutableLiveData<>(HomeFragment.MODE_AMBULANCE);
    }


    public MutableLiveData<String> getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode.setValue(displayMode);
    }
}
