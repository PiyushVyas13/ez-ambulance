package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.ui.settings;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel.HistoryViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.TripHistory;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentHistoryItemBinding;
import com.swasthavyas.emergencyllp.util.TimestampUtility;

import java.util.List;


public class HistoryItemFragment extends Fragment {
    private HistoryViewModel historyViewModel;
    private FragmentHistoryItemBinding viewBinding;
    private OwnerViewModel ownerViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry backStackEntry = navController.getBackStackEntry(R.id.ambulanceHistoryFragment);

        historyViewModel = new ViewModelProvider(backStackEntry).get(HistoryViewModel.class);
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);

        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(viewBinding.getRoot()).navigateUp();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

    }

    private String getDriverName(String driverId) {
        Owner currentOwner = ownerViewModel.getOwner().getValue();

        assert currentOwner != null;

        List<EmployeeDriver> employees = currentOwner.getEmployees().getValue();

        assert employees != null && !employees.isEmpty();

        EmployeeDriver driver = employees
                .stream()
                .filter(employeeDriver -> employeeDriver.getDriverId().equals(driverId))
                .findFirst()
                .orElse(null);

        if(driver == null) {
            Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "getDriverName: driver with given driverId not found");
            return null;
        }

        return driver.getName();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentHistoryItemBinding.inflate(getLayoutInflater());

        historyViewModel.getSelectedTripHistory().observe(getViewLifecycleOwner(), history -> {

            if(history == null) {
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
                return;
            }

            Trip trip = history.getTrip();

            viewBinding.name.setText(getDriverName(trip.getAssignedDriverId()));
            viewBinding.customerName.setText(trip.getCustomerName());
            viewBinding.earning.setText(String.valueOf(trip.getPrice()));
            viewBinding.customerAge.setText(String.valueOf(trip.getCustomerAge()));
            viewBinding.customerMobile.setText(trip.getCustomerMobile());
            viewBinding.pickupLocation.setText(trip.getPickupLocationAddress());
            viewBinding.dropLocation.setText(trip.getDropLocationAddress());

            TimestampUtility timestampUtility = new TimestampUtility(history.getCompletionTimestamp());

            String dateString = timestampUtility.getFormattedDate("MMM d, YYYY");
            String timeString = timestampUtility.getFormattedDate("hh:mm a");

            viewBinding.timestamp.setText(getString(R.string.history_timestamp, dateString, timeString));
        });


        return viewBinding.getRoot();
    }
}