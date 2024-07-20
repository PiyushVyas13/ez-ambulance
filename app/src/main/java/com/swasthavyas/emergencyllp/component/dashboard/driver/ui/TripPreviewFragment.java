package com.swasthavyas.emergencyllp.component.dashboard.driver.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.trip.ui.TripActivity;
import com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel.TripViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentTripPreviewBinding;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

public class TripPreviewFragment extends Fragment {
    private FragmentTripPreviewBinding viewBinding;
    private TripViewModel tripViewModel;

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
        tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);

        // Set header image resources
        viewBinding.header.goTo.setImageResource(R.drawable.right);
        viewBinding.header.offDuty.setImageResource(R.drawable.number1_stepper_top);


        // Get latest trip data
        tripViewModel.getActiveTrip().observe(getViewLifecycleOwner(), trip -> {
            if(trip == null) {
                //Navigate back to home page
                Navigation.findNavController(viewBinding.getRoot()).navigateUp();
                return;
            }


            String customerName = getString(R.string.customer_name_text, trip.getCustomerName());
            String pickupLocation = getString(R.string.pickup_location, trip.getPickupLocationAddress());
            String dropLocation = getString(R.string.drop_location_text, trip.getDropLocationAddress());
            String customerMobile = getString(R.string.mobileno_text, formatMobileNumber(trip.getCustomerMobile()));

            viewBinding.customerName.setText(customerName);
            viewBinding.pickupLocation.setText(pickupLocation);
            viewBinding.dropLocation.setText(dropLocation);
            viewBinding.customerMobile.setText(customerMobile);

            if(trip.isEmergencyRide()) {
                viewBinding.emergencyRideAlertBox.setVisibility(View.VISIBLE);
                startBlinkAnimation();
            }

            viewBinding.startTripButton.setOnClickListener(v -> {
                tripViewModel.updateTripStatus(TripStatus.IN_PROGRESS);

                Intent intent = new Intent(requireActivity(), TripActivity.class);
                intent.putExtra("owner_id", trip.getOwnerId());
                intent.putExtra("trip_id", trip.getId());

                startActivity(intent);

            });

        });

        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }

    private String formatMobileNumber(String mobileNumber) {
        return mobileNumber.replaceFirst("91", "+91 ");
    }

    private void startBlinkAnimation() {
        ObjectAnimator blinkAnimation = ObjectAnimator.ofFloat(
                viewBinding.emergencyRideAlertBox,
                "alpha",
                0.0f, 1.0f
        );


        blinkAnimation.setDuration(500);
        blinkAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        blinkAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        blinkAnimation.setRepeatMode(ObjectAnimator.REVERSE);

        blinkAnimation.start();
    }
}