package com.swasthavyas.emergencyllp.component.dashboard.driver.ui;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.swasthavyas.emergencyllp.AuthActivity;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel.EmployeeViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.ui.DriverDashboardFragment;
import com.swasthavyas.emergencyllp.component.trip.ui.TripActivity;
import com.swasthavyas.emergencyllp.databinding.FragmentProfileDriverBinding;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.service.LocationService;


public class ProfileFragment extends Fragment {

    FragmentProfileDriverBinding viewBinding;
    EmployeeViewModel employeeViewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        employeeViewModel = new ViewModelProvider(requireActivity()).get(EmployeeViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentProfileDriverBinding.inflate(getLayoutInflater());
        FirebaseDatabase database = FirebaseService.getInstance().getDatabaseInstance();

        employeeViewModel.getCurrentEmployee().observe(getViewLifecycleOwner(), employeeDriver -> {
            Log.d(TAG, "onCreateView: Profile fragment observer triggered");
            viewBinding.signOutBtn.setOnClickListener(v -> {
                DatabaseReference activeDriverReference = database
                        .getReference()
                        .getRoot()
                        .child("active_drivers")
                        .child(employeeDriver.getDriverId());

                activeDriverReference.removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        LocationService.stopService(requireContext(), employeeDriver.getEmail());
                                        FirebaseAuth.getInstance().signOut();
//                                        Intent intent = new Intent(requireActivity(), AuthActivity.class);
//                                        startActivity(intent);
//                                        requireActivity().finish();
                                    }
                                });
            });
        });


        return viewBinding.getRoot();
    }
}