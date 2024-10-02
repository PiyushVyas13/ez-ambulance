package com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.ui;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.primitives.Doubles;
import com.google.firebase.Timestamp;
import com.google.firebase.database.FirebaseDatabase;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel.AmbulanceViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.TripRegistrationStep;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.viewmodel.TripAssignmentViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.worker.NotifyEmployeeWorker;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.TripViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentRideAssignmentBinding;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.steppernav.NavigationStepFragment;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


public class RideAssignmentFragment extends Fragment {
    FragmentRideAssignmentBinding viewBinding;
    TripAssignmentViewModel tripAssignmentViewModel;
    TripViewModel tripViewModel;
    AmbulanceViewModel ambulanceViewModel;
    NavController dashboardNavController, registrationNavController;

    private Ambulance ambulance;
    private EmployeeDriver assignedDriver;

    private final AtomicReference<TripRegistrationStep> currentStep;

    public RideAssignmentFragment() {
        // Required empty public constructor
        currentStep = new AtomicReference<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback onBackPressedCallback =  new OnBackPressedCallback(true) {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentRideAssignmentBinding.inflate(getLayoutInflater());
        ambulanceViewModel = new ViewModelProvider(requireActivity()).get(AmbulanceViewModel.class);

        ambulanceViewModel.getCurrentAmbulance().observe(getViewLifecycleOwner(), ambulance -> this.ambulance = ambulance);
        ambulanceViewModel.getAssignedDriver().observe(getViewLifecycleOwner(), assignedDriver -> this.assignedDriver = assignedDriver);

        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        dashboardNavController = Navigation.findNavController(viewBinding.getRoot());
        registrationNavController = NavHostFragment.findNavController(viewBinding.tripRegistrationContainer.getFragment());

        tripAssignmentViewModel = new ViewModelProvider(registrationNavController.getBackStackEntry(R.id.trip_assignment_graph)).get(TripAssignmentViewModel.class);
        tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);


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
                Log.d(TAG, "registrationData: " + tripRegistrationData);

                if(tripRegistrationData.containsKey(TripRegistrationStep.CUSTOMER_INFO) && tripRegistrationData.containsKey(TripRegistrationStep.PICKUP_LOCATION) && tripRegistrationData.containsKey(TripRegistrationStep.DROP_LOCATION)) {
                    Toast.makeText(requireActivity(), "Ready for registration", Toast.LENGTH_SHORT).show();

                    Trip trip = createNewTrip(ambulance.getId(), assignedDriver.getDriverId(), assignedDriver.getOwnerId(), tripRegistrationData);

                    saveTripToDatabase(trip);
                }
            }
        });

        viewBinding.nextOrAssignButton.setOnClickListener(v -> {
            Fragment currentFragment = viewBinding.tripRegistrationContainer.getFragment().getChildFragmentManager().getPrimaryNavigationFragment();
            if(currentFragment instanceof NavigationStepFragment) {
                NavigationStepFragment navigationStepFragment = (NavigationStepFragment) currentFragment;

                validateAndCollectData(navigationStepFragment, currentStep.get());
                switch (currentStep.get()) {
                    case CUSTOMER_INFO:
                        tripAssignmentViewModel.setCurrentStep(TripRegistrationStep.PICKUP_LOCATION);
                        registrationNavController.navigate(R.id.action_customerInfoFragment_to_pickupLocationFragment);
                        break;
                    case PICKUP_LOCATION:
                        tripAssignmentViewModel.setCurrentStep(TripRegistrationStep.DROP_LOCATION);
                        registrationNavController.navigate(R.id.action_pickupLocationFragment_to_dropLocationFragment);
                        break;

                }
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

    private void saveTripToDatabase(Trip trip) {
        FirebaseDatabase database = FirebaseService.getInstance().getDatabaseInstance();
        // database.useEmulator("10.0.2.2", 8000);

        database
                .getReference()
                .getRoot()
                .child("trips")
                .child(trip.getOwnerId())
                .child(trip.getId())
                .setValue(trip)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        String driverUid = assignedDriver.getUserId();
                        String customerName = trip.getCustomerName();
                        String customerMobile = trip.getCustomerMobile();
                        long customerAge = trip.getCustomerAge();
                        double estimatedPrice = trip.getPrice();
                        String pickupLocationAddress = trip.getPickupLocationAddress();
                        String dropLocationAddress = trip.getDropLocationAddress();

                        List<Double> dropLocation = trip.getDropLocation();
                        List<Double> pickupLocation = trip.getPickupLocation();

                        Data inputData = new Data.Builder()
                                .putString("trip_id", trip.getId())
                                .putBoolean("is_emergency_ride", trip.isEmergencyRide())
                                .putString("driver_uid", driverUid)
                                .putString("customer_name",customerName)
                                .putLong("customer_age", customerAge)
                                .putString("customer_mobile", customerMobile)
                                .putDouble("estimated_price", estimatedPrice)
                                .putString("pickup_location_address", pickupLocationAddress)
                                .putString("drop_location_address", dropLocationAddress)
                                .putDoubleArray("pickup_location_coordinates", Doubles.toArray(pickupLocation))
                                .putDoubleArray("drop_location_coordinates", Doubles.toArray(dropLocation))
                                .build();

                        notifyDriver(inputData);
                    }
                });
    }

    private void notifyDriver(Data inputData) {
        OneTimeWorkRequest notifyDriverRequest = new OneTimeWorkRequest.Builder(NotifyEmployeeWorker.class)
                .setInputData(inputData)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build();

        WorkManager.getInstance(requireContext())
                .enqueue(notifyDriverRequest);

        WorkManager.getInstance(requireContext())
                .getWorkInfoByIdLiveData(notifyDriverRequest.getId())
                .observe(getViewLifecycleOwner(), workInfo -> {
                    if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                        //TODO: Initiate Trip sequence
                        Toast.makeText(requireContext(), "Notification sent to driver.", Toast.LENGTH_SHORT).show();
                        dashboardNavController.navigateUp();
                    }
                    else if (workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                        Toast.makeText(requireContext(), "Could not send notification to driver.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "notifyDriver: " + workInfo.getOutputData().getString("message"));
                    }
                });
    }

    private void validateAndCollectData(NavigationStepFragment stepFragment, TripRegistrationStep step) {
        if(stepFragment.validateData()) {
            Data collectedData = stepFragment.collectData();

            tripAssignmentViewModel.addRegistrationData(step, collectedData);
        }
    }

    public Trip createNewTrip(String ambulanceId, String driverId, String ownerId, Map<TripRegistrationStep, Data> dataMap) {
        String tripId = UUID.randomUUID().toString();

        Map<String, Object> tripMap = new HashMap<>();

        Data customerInfoData = dataMap.get(TripRegistrationStep.CUSTOMER_INFO);
        Data pickupLocationData = dataMap.get(TripRegistrationStep.PICKUP_LOCATION);
        Data dropLocationData = dataMap.get(TripRegistrationStep.DROP_LOCATION);

        if(customerInfoData == null || pickupLocationData == null || dropLocationData == null) {
            throw new NullPointerException("data not provided.");
        }

        double[] pickupLocationCoordinates = pickupLocationData.getDoubleArray(Trip.ModelColumns.PICKUP_LOCATION);
        double[] dropLocationCoordinates = dropLocationData.getDoubleArray(Trip.ModelColumns.DROP_LOCATION);

        tripMap.put(Trip.ModelColumns.ID, tripId);
        tripMap.put(Trip.ModelColumns.ASSIGNED_AMBULANCE_ID, ambulanceId);
        tripMap.put(Trip.ModelColumns.ASSIGNED_DRIVER_ID, driverId);
        tripMap.put(Trip.ModelColumns.OWNER_ID, ownerId);
        tripMap.put(Trip.ModelColumns.STATUS, TripStatus.PENDING_RESPONSE);
        tripMap.put(Trip.ModelColumns.CREATED_AT, new Timestamp(new Date()));
        tripMap.putAll(customerInfoData.getKeyValueMap());
        tripMap.putAll(pickupLocationData.getKeyValueMap());
        tripMap.putAll(dropLocationData.getKeyValueMap());
        tripMap.replace(Trip.ModelColumns.PICKUP_LOCATION, sanitizeCoordinates(pickupLocationCoordinates));
        tripMap.replace(Trip.ModelColumns.DROP_LOCATION, sanitizeCoordinates(dropLocationCoordinates));



        return Trip.createFromMap(tripMap);
    }

    private List<Double> sanitizeCoordinates(double[] array) {
        List<Double> doubleList = new ArrayList<>();

        for(Double d : array) {
            doubleList.add(d);
        }

        return doubleList;
    }
}