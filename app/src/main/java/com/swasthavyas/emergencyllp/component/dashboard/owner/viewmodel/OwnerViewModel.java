package com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;

public class OwnerViewModel extends ViewModel {
    private final MutableLiveData<Owner> owner;

    public OwnerViewModel() {
        owner = new MutableLiveData<>(null);
    }

    public void setOwner(Owner owner) {
        this.owner.setValue(owner);
    }

    public LiveData<Owner> getOwner() {
        return this.owner;
    }


}
