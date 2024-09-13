package com.swasthavyas.emergencyllp.component.dashboard.driver.ui;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel.DashboardViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel.EmployeeViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel.HistoryViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.TripHistory;
import com.swasthavyas.emergencyllp.component.history.domain.adapter.HistoryAdapter;
import com.swasthavyas.emergencyllp.databinding.FragmentHomeDriverBinding;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.service.LocationService;
import com.swasthavyas.emergencyllp.util.types.DriverStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class HomeFragment extends Fragment {
    FragmentHomeDriverBinding viewBinding;
    DashboardViewModel dashboardViewModel;
    EmployeeViewModel employeeViewModel;
    HistoryViewModel historyViewModel;
    FirebaseDatabase database;
    EmployeeDriver employeeDriver;


    public HomeFragment() {
        // Required empty public constructor
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentHomeDriverBinding.inflate(getLayoutInflater());
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        employeeViewModel = new ViewModelProvider(requireActivity()).get(EmployeeViewModel.class);
        historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);
        database = FirebaseService.getInstance().getDatabaseInstance();

        viewBinding.header.goTo.setEnabled(false);

        dashboardViewModel.getUserRole().observe(getViewLifecycleOwner(), userRole -> {
            switch (userRole) {
                case EMPLOYEE_DRIVER:
                    EmployeeViewModel employeeViewModel = new ViewModelProvider(requireActivity()).get(EmployeeViewModel.class);

                    employeeViewModel.getCurrentEmployee().observe(getViewLifecycleOwner(), employeeDriver -> {
                        if(employeeDriver != null) {
                            viewBinding.changeAmbulanceFab.setText(employeeDriver.getName());
                            viewBinding.assignedAmbulanceNumber.setText(employeeDriver.getAssignedAmbulanceNumber());
                            getRecentTrips(employeeDriver.getDriverId(), employeeDriver.getAssignedAmbulanceNumber());
                            Log.d(TAG, "onCreateView: " + employeeDriver);

                            dashboardViewModel.getDriverStatus().observe(getViewLifecycleOwner(), driverStatus -> {
                                switch (driverStatus) {
                                    case OFF_DUTY:
                                        viewBinding.header.offDuty.setImageResource(R.drawable.right);
                                        viewBinding.header.onDuty.setImageResource(R.drawable.number2_stepper_top);
                                        viewBinding.header.goTo.setImageResource(R.drawable.number3_stepper_top);
                                        viewBinding.header.offDuty.setEnabled(false);
                                        viewBinding.header.onDuty.setEnabled(true);
                                        break;
                                    case ON_DUTY:
                                        viewBinding.header.offDuty.setImageResource(R.drawable.number1_stepper_top);
                                        viewBinding.header.onDuty.setImageResource(R.drawable.right);
                                        viewBinding.header.goTo.setImageResource(R.drawable.number3_stepper_top);
                                        viewBinding.header.offDuty.setEnabled(true);
                                        viewBinding.header.onDuty.setEnabled(false);
                                        break;
                                    case ON_TRIP:
                                        viewBinding.header.offDuty.setImageResource(R.drawable.number1_stepper_top);
                                        viewBinding.header.onDuty.setImageResource(R.drawable.number2_stepper_top);
                                        viewBinding.header.goTo.setImageResource(R.drawable.right);
                                        viewBinding.header.offDuty.setEnabled(false);
                                        viewBinding.header.onDuty.setEnabled(false);

                                        // Navigate to preview fragment
                                        Navigation.findNavController(viewBinding.getRoot()).navigate(R.id.tripPreviewFragment);
                                        break;
                                }
                            });

                            viewBinding.header.onDuty.setOnClickListener(v -> {
                                showConfirmationDialog();
                            });

                            viewBinding.header.offDuty.setOnClickListener(v -> {
                                new MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Confirm status change")
                                        .setMessage("You will not receive any rides while you are off duty")
                                        .setPositiveButton("Ok", (dialog, which) -> {
                                            updateDriverActiveStatus(employeeDriver, DriverStatus.OFF_DUTY);
                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> {})
                                        .show();
                            });

                            viewBinding.historyBtn.setOnClickListener(v -> {
                                HomeFragmentDirections.HistoryAction action = HomeFragmentDirections
                                        .historyAction(employeeDriver.getDriverId(), "trip.assignedDriverId", employeeDriver.getAssignedAmbulanceNumber(), "driver");

                                Navigation.findNavController(v).navigate(action);
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

        employeeViewModel.getRideCount().observe(getViewLifecycleOwner(), rideCount -> viewBinding.rideCount.setText(getString(R.string.ride_count, rideCount)));
        employeeViewModel.getLastWeekRideCount().observe(getViewLifecycleOwner(), lastWeekCount -> viewBinding.lastWeekRideCount.setText(getString(R.string.ride_count, lastWeekCount)));
        employeeViewModel.getTotalEarning().observe(getViewLifecycleOwner(), earning -> viewBinding.totalEarning.setText(getString(R.string.driver_earning, earning)));
        employeeViewModel.getLastWeekRideEarning().observe(getViewLifecycleOwner(), lastWeekEarning -> viewBinding.lastWeekEarning.setText(getString(R.string.driver_earning, lastWeekEarning)));

        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }

    private void getRecentTrips(String driverId, String ambulanceNumber) {
        List<TripHistory> historyList = new ArrayList<>();

        Timestamp now = Timestamp.now();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);

        Timestamp sevenDaysAgo = new Timestamp(calendar.getTime());

        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        dbInstance
                .collection("trip_history")
                .whereEqualTo("trip.assignedDriverId", driverId)
                .whereLessThanOrEqualTo("completionTimestamp", now)
                .whereGreaterThanOrEqualTo("completionTimestamp", sevenDaysAgo)
                .orderBy("completionTimestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task ->  {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot snapshot : task.getResult()) {
                            Map<String, Object> historyMap = snapshot.getData();

                            assert historyMap != null;
                            TripHistory history = TripHistory.createFromMap(historyMap);

                            historyList.add(history);
                        }

                        HistoryAdapter adapter = new HistoryAdapter(requireContext(), historyList, ambulanceNumber, (v, history) -> {
                            historyViewModel.setSelectedTripHistory(history);

                            HomeFragmentDirections.RecentHistoryDetailAction action = HomeFragmentDirections.recentHistoryDetailAction(ambulanceNumber, "Ambulance");
                            Navigation.findNavController(v).navigate(action);
                        });
                        viewBinding.recentTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
                        viewBinding.recentTrips.setAdapter(adapter);
                    }
                });
    }

    private void updateDriverActiveStatus(EmployeeDriver employeeDriver, DriverStatus status) {

        DatabaseReference activeDriverReference = database
                .getReference()
                .getRoot()
                .child("active_drivers")
                .child(employeeDriver.getDriverId());

        switch (status) {
            case OFF_DUTY:
                activeDriverReference.removeValue()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                Toast.makeText(requireContext(), "Status Updated!", Toast.LENGTH_SHORT).show();
                                LocationService.stopService(requireContext(), employeeDriver.getEmail());
                            }
                            else {
                                Log.d(TAG, "onCreateView: " + task.getException());
                            }
                        });
                break;
            case ON_DUTY:
                List<Double> coords = employeeDriver.getLastLocation() == null ? Arrays.asList(21.1458, 79.0882) : employeeDriver.getLastLocation();

                activeDriverReference.setValue(coords)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                Toast.makeText(requireActivity(), "Status updated!", Toast.LENGTH_SHORT).show();
                                LocationService.startService(requireContext(), employeeDriver.getDriverId());
                            }
                            else {
                                Log.d(TAG, "updateDriverStatus: " + task.getException());
                            }
                        });
                break;
        }

    }

    private void showConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Status Change")
                .setMessage("You are about to change your status from 'OFF DUTY' to 'ON DUTY'." +
                        "This will allow the fleet owner to view your live location. To turn off location sharing, switch to OFF DUTY mode.")
                .setPositiveButton("I Understand", (dialog, which) -> {
                    updateDriverActiveStatus(employeeDriver, DriverStatus.ON_DUTY);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {})
                .show();


    }
}