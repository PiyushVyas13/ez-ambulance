package com.swasthavyas.emergencyllp.component.dashboard.owner.domain.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.ui.ManageAmbulanceFragment;
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.ui.ManageDriverFragment;

public class DisplayModeAdapter extends FragmentStateAdapter {
    public DisplayModeAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        if(position == 0) {
            return new ManageAmbulanceFragment();
        }

        return new ManageDriverFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
