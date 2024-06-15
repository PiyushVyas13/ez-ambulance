package com.swasthavyas.emergencyllp.component.dashboard.owner.trip.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.swasthavyas.emergencyllp.component.dashboard.owner.trip.domain.TripRegistrationStep;
import com.swasthavyas.emergencyllp.component.dashboard.owner.trip.viewmodel.TripAssignmentViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentRideAssignmentBinding;
import com.swasthavyas.emergencyllp.util.AppConstants;

import java.util.concurrent.atomic.AtomicReference;


public class RideAssignmentFragment extends Fragment {
    FragmentRideAssignmentBinding viewBinding;
    TripAssignmentViewModel tripAssignmentViewModel;

    private final AtomicReference<TripRegistrationStep> currentStep;

    public RideAssignmentFragment() {
        // Required empty public constructor
        currentStep = new AtomicReference<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentRideAssignmentBinding.inflate(getLayoutInflater());


        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        NavController dashboardNavController = Navigation.findNavController(viewBinding.getRoot());
        NavController registrationNavController = NavHostFragment.findNavController(viewBinding.tripRegistrationContainer.getFragment());

        tripAssignmentViewModel = new ViewModelProvider(registrationNavController.getBackStackEntry(R.id.trip_assignment_graph)).get(TripAssignmentViewModel.class);

        tripAssignmentViewModel.getCurrentStep().observe(getViewLifecycleOwner(), step -> {
            if(step != null) {
                currentStep.set(step);
                switch (step) {
                    case CUSTOMER_INFO:
                        viewBinding.stepTitle.setText(getString(R.string.enter_customer_information));
                        viewBinding.nextOrAssignButton.setText(getString(R.string.text_next));
                        viewBinding.cancelOrBackButton.setText(getString(R.string.cancel));
                        break;
                    case PICKUP_LOCATION:
                        viewBinding.stepTitle.setText(R.string.enter_pickup_location);
                        viewBinding.nextOrAssignButton.setText(R.string.text_next);
                        viewBinding.cancelOrBackButton.setText(R.string.back);
                        break;
                    case DROP_LOCATION:
                        viewBinding.stepTitle.setText(R.string.enter_drop_location);
                        viewBinding.nextOrAssignButton.setText(R.string.book_ride);
                        viewBinding.cancelOrBackButton.setText(R.string.back);
                        break;
                }
            }
        });

        tripAssignmentViewModel.getRegistrationData().observe(getViewLifecycleOwner(), tripRegistrationData -> {
            if(tripRegistrationData != null) {
                Log.d(AppConstants.TAG, "registrationData: " + tripRegistrationData);

                if(tripRegistrationData.containsKey(TripRegistrationStep.CUSTOMER_INFO) && tripRegistrationData.containsKey(TripRegistrationStep.PICKUP_LOCATION) && tripRegistrationData.containsKey(TripRegistrationStep.DROP_LOCATION)) {
                    Toast.makeText(requireActivity(), "Ready for registration", Toast.LENGTH_SHORT).show();

                    Bundle customerInfoData = tripRegistrationData.get(TripRegistrationStep.CUSTOMER_INFO);
                    Bundle pickupLocationData = tripRegistrationData.get(TripRegistrationStep.PICKUP_LOCATION);
                    Bundle dropLocationData = tripRegistrationData.get(TripRegistrationStep.DROP_LOCATION);

                    //TODO: Process this data here (Call a method)
                }
            }
        });

        viewBinding.nextOrAssignButton.setOnClickListener(v -> {
            Fragment currentFragment = viewBinding.tripRegistrationContainer.getFragment().getChildFragmentManager().getPrimaryNavigationFragment();
            switch (currentStep.get()) {
                case CUSTOMER_INFO:
                    if(currentFragment instanceof CustomerInfoFragment) {
                        CustomerInfoFragment customerInfoFragment = (CustomerInfoFragment) currentFragment;

                        if(customerInfoFragment.validateData()) {
                            Bundle customerInfoData = customerInfoFragment.collectData();
                            tripAssignmentViewModel.addRegistrationData(TripRegistrationStep.CUSTOMER_INFO, customerInfoData);
                            tripAssignmentViewModel.setCurrentStep(TripRegistrationStep.PICKUP_LOCATION);
                            registrationNavController.navigate(R.id.action_customerInfoFragment_to_pickupLocationFragment);
                        }

                    }
                    else {
                        Toast.makeText(requireActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case PICKUP_LOCATION:
                    if(currentFragment instanceof PickupLocationFragment) {
                        PickupLocationFragment pickupLocationFragment =(PickupLocationFragment) currentFragment;

                        if(pickupLocationFragment.validateData()) {
                            Bundle pickupLocationData = pickupLocationFragment.collectData();
                            tripAssignmentViewModel.addRegistrationData(TripRegistrationStep.PICKUP_LOCATION, pickupLocationData);
                            tripAssignmentViewModel.setCurrentStep(TripRegistrationStep.DROP_LOCATION);
                            registrationNavController.navigate(R.id.action_pickupLocationFragment_to_dropLocationFragment);
                        }
                    }
                    else {
                        Toast.makeText(requireActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DROP_LOCATION:

                    if(currentFragment instanceof DropLocationFragment) {
                        DropLocationFragment dropLocationFragment = (DropLocationFragment) currentFragment;

                        if(dropLocationFragment.validateData()) {
                            Bundle dropLocationData = dropLocationFragment.collectData();
                            tripAssignmentViewModel.addRegistrationData(TripRegistrationStep.DROP_LOCATION, dropLocationData);

                            //TODO: Final Navigation flow yet to be decided for this action.
                            dashboardNavController.navigateUp();
                        }
                    }
                    break;
            }
        });

        viewBinding.cancelOrBackButton.setOnClickListener(v -> {
            if(currentStep.get() == TripRegistrationStep.CUSTOMER_INFO) {
                dashboardNavController.navigateUp();
            }
            else {
                if(currentStep.get() == TripRegistrationStep.PICKUP_LOCATION) {
                    tripAssignmentViewModel.setCurrentStep(TripRegistrationStep.CUSTOMER_INFO);
                }
                else {
                    tripAssignmentViewModel.setCurrentStep(TripRegistrationStep.PICKUP_LOCATION);
                }
                registrationNavController.popBackStack();
            }
        });
    }
}