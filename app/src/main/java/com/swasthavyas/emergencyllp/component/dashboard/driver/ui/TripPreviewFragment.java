package com.swasthavyas.emergencyllp.component.dashboard.driver.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.databinding.FragmentTripPreviewBinding;

public class TripPreviewFragment extends Fragment {
    private FragmentTripPreviewBinding viewBinding;

    public TripPreviewFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentTripPreviewBinding.inflate(getLayoutInflater());

        // Set header image resources
        viewBinding.header.goTo.setImageResource(R.drawable.right);
        viewBinding.header.offDuty.setImageResource(R.drawable.number1_stepper_top);


        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }
}