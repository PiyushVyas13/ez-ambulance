package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.ui.settings;

import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel.AmbulanceViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.databinding.FragmentAboutAmbulanceBinding;

import java.util.Locale;


public class
AboutAmbulanceFragment extends Fragment {
    private FragmentAboutAmbulanceBinding viewBinding;
    private AmbulanceViewModel ambulanceViewModel;


    public AboutAmbulanceFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ambulanceViewModel = new ViewModelProvider(requireActivity()).get(AmbulanceViewModel.class);

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(viewBinding.getRoot())
                        .navigate(
                                R.id.ambulanceDetailFragment,
                                null,
                                new NavOptions.Builder()
                                        .setEnterAnim(android.R.anim.fade_in)
                                        .setExitAnim(android.R.anim.slide_out_right)
                                        .build()
                        );
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentAboutAmbulanceBinding.inflate(getLayoutInflater());

        ambulanceViewModel.getCurrentAmbulance().observe(getViewLifecycleOwner(), ambulance -> {
            viewBinding.ambulanceType.setText(ambulance.getAmbulanceType().name());
            viewBinding.vehicleType.setText(ambulance.getVehicleType());
            viewBinding.vehicleNumber.setText(ambulance.getVehicleNumber());

            Glide.with(requireContext())
                    .load(ambulance.getImageRef())
                    .into(viewBinding.ambulanceImage);
        });

        ambulanceViewModel.getAssignedDriver().observe(getViewLifecycleOwner(), assignedDriver -> {
            viewBinding.assignedDriverName.setText(assignedDriver == null ? "No Driver Assigned" : assignedDriver.getName());
        });


        return viewBinding.getRoot();
    }
}