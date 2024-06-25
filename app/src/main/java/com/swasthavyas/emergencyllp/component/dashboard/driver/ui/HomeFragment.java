package com.swasthavyas.emergencyllp.component.dashboard.driver.ui;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.FirebaseDatabase;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel.DashboardViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel.EmployeeViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.databinding.FragmentHomeDriverBinding;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.service.LocationService;
import com.swasthavyas.emergencyllp.util.types.DriverStatus;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    FragmentHomeDriverBinding viewBinding;
    DashboardViewModel dashboardViewModel;
    EmployeeViewModel employeeViewModel;
    FirebaseDatabase database;
    EmployeeDriver employeeDriver;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentHomeDriverBinding.inflate(getLayoutInflater());
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        employeeViewModel = new ViewModelProvider(requireActivity()).get(EmployeeViewModel.class);
        database = FirebaseService.getInstance().getDatabaseInstance();

        viewBinding.goTo.setEnabled(false);

        dashboardViewModel.getUserRole().observe(getViewLifecycleOwner(), userRole -> {
            switch (userRole) {
                case EMPLOYEE_DRIVER:
                    EmployeeViewModel employeeViewModel = new ViewModelProvider(requireActivity()).get(EmployeeViewModel.class);

                    employeeViewModel.getCurrentEmployee().observe(getViewLifecycleOwner(), employeeDriver -> {
                        if(employeeDriver != null) {
                            viewBinding.changeAmbulanceFab.setText(employeeDriver.getName());
                            Log.d(TAG, "onCreateView: " + employeeDriver);

                            dashboardViewModel.getDriverStatus().observe(getViewLifecycleOwner(), driverStatus -> {
                                switch (driverStatus) {
                                    case OFF_DUTY:
                                        viewBinding.offDuty.setImageResource(R.drawable.right);
                                        viewBinding.onDuty.setImageResource(R.drawable.number2_stepper_top);
                                        viewBinding.goTo.setImageResource(R.drawable.number3_stepper_top);
                                        viewBinding.offDuty.setEnabled(false);
                                        viewBinding.onDuty.setEnabled(true);
                                        break;
                                    case ON_DUTY:
                                        viewBinding.offDuty.setImageResource(R.drawable.number1_stepper_top);
                                        viewBinding.onDuty.setImageResource(R.drawable.right);
                                        viewBinding.goTo.setImageResource(R.drawable.number3_stepper_top);
                                        viewBinding.offDuty.setEnabled(true);
                                        viewBinding.onDuty.setEnabled(false);
                                        break;
                                    case ON_TRIP:
                                        viewBinding.offDuty.setImageResource(R.drawable.number1_stepper_top);
                                        viewBinding.onDuty.setImageResource(R.drawable.number2_stepper_top);
                                        viewBinding.goTo.setImageResource(R.drawable.right);
                                        viewBinding.offDuty.setEnabled(false);
                                        viewBinding.onDuty.setEnabled(false);
                                        break;
                                }
                            });

                            viewBinding.onDuty.setOnClickListener(v -> {
                                showConfirmationDialog();
                            });

                            viewBinding.offDuty.setOnClickListener(v -> {
                                new MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Confirm status change")
                                        .setMessage("You will not receive any rides while you are off duty")
                                        .setPositiveButton("Ok", (dialog, which) -> {
                                            database
                                                    .getReference()
                                                    .getRoot()
                                                    .child("active_drivers")
                                                    .child(employeeDriver.getDriverId())
                                                    .removeValue()
                                                    .addOnCompleteListener(task -> {
                                                       if(task.isSuccessful()) {
                                                           Toast.makeText(requireContext(), "Status Updated!", Toast.LENGTH_SHORT).show();
                                                           LocationService.stopService(requireContext(), employeeDriver.getEmail());
                                                       }
                                                       else {
                                                           Log.d(TAG, "onCreateView: " + task.getException());
                                                       }
                                                    });
                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> {})
                                        .show();
                            });

                        }
                    });

                    break;
                case DRIVER:
                    throw new UnsupportedOperationException("Not yet implemented");
            }
        });



        employeeViewModel.getCurrentEmployee().observe(getViewLifecycleOwner(), employeeDriver -> {
            if(employeeDriver != null) {
                this.employeeDriver = employeeDriver;

            }
        });



        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }

    private void showConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Status Change")
                .setMessage("You are about to change your status from 'OFF DUTY' to 'ON DUTY'." +
                        "This will allow the fleet owner to view your live location. To turn off location sharing, switch to OFF DUTY mode.")
                .setPositiveButton("I Understand", (dialog, which) -> {
                    updateDriverStatus();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {})
                .show();
    }

    private void updateDriverStatus() {

        List<Double> coords = new ArrayList<>();
        coords.add(24.422342342);
        coords.add(53.535234234);

        database
                .getReference()
                .child("active_drivers")
                .child(employeeDriver.getDriverId())
                .setValue(coords)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(requireActivity(), "Status updated!", Toast.LENGTH_SHORT).show();
                        LocationService.startService(requireContext(), employeeDriver.getDriverId());
                    }
                    else {
                        Log.d(TAG, "updateDriverStatus: " + task.getException());
                    }
                });
    }
}