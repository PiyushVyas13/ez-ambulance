package com.swasthavyas.emergencyllp.component.dashboard.ui;


import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.swasthavyas.emergencyllp.AuthActivity;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.auth.viewmodel.AuthViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel.DashboardViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel.EmployeeViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel.TripViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.driver.worker.FetchEmployeeWorker;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.databinding.FragmentDriverDashboardBinding;
import com.swasthavyas.emergencyllp.network.SendSmsRequest;
import com.swasthavyas.emergencyllp.network.TextLocalRepository;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.types.DriverStatus;
import com.swasthavyas.emergencyllp.util.types.TripStatus;
import com.swasthavyas.emergencyllp.util.types.UserRole;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Parent container fragment for the Driver's Dashboard
 * **/
public class DriverDashboardFragment extends Fragment {

    private FragmentDriverDashboardBinding viewBinding;

    private DashboardViewModel dashboardViewModel;
    private EmployeeViewModel employeeViewModel;
    private TripViewModel tripViewModel;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private NavController navController;
    private BroadcastReceiver requestReceiver;

    private UserRole userRole;
    private String receivedTripId;


    public DriverDashboardFragment() {
        // Required empty public constructor
    }

    public static DriverDashboardFragment newInstance(UserRole userRole) {
        DriverDashboardFragment fragment = new DriverDashboardFragment();
        Bundle args = new Bundle();
        args.putString("role", userRole.name());
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseService.getInstance().getDatabaseInstance();
//        database.useEmulator("10.0.2.2", 8000);
         dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        if (getArguments() != null) {
            this.userRole = UserRole.valueOf(getArguments().getString("role"));
        } else {
            this.userRole = UserRole.UNASSIGNED;
        }

        dashboardViewModel.setUserRole(this.userRole);

    }

    private void observeTripStatus(String tripId) {
        database
                .getReference()
                .getRoot()
                .child("trips")
                .child(employeeViewModel.getCurrentEmployee().getValue().getOwnerId())
                .child(tripId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            Trip trip = snapshot.getValue(Trip.class);
                            if(trip == null) {
                                return;
                            }

                            tripViewModel.setActiveTrip(trip);

                            switch (trip.getStatus()) {
                                case INITIATED:
                                    Toast.makeText(requireContext(), "Trip Initiated", Toast.LENGTH_SHORT).show();
                                    // TODO: Send SMS to user
                                    //sendUserConfirmationSms(trip.getCustomerMobile());

                                    dashboardViewModel.setDriverStatus(DriverStatus.ON_TRIP);
                                    break;
                            }


                        } else {
                            Log.d(TAG, "onDataChange: trip to observe does not exist.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void retrievePotentialTrip() {

        database
                .getReference()
                .getRoot()
                .child("trips")
                .child(employeeViewModel.getCurrentEmployee().getValue().getOwnerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Trip trip = dataSnapshot.getValue(Trip.class);
                            if(trip.getAssignedDriverId().equals(employeeViewModel.getCurrentEmployee().getValue().getDriverId())) {
                                observeTripStatus(trip.getId());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendUserConfirmationSms(String customerMobile) {
        final String SMS_SENDER = "600010";
        EmployeeDriver driver = employeeViewModel.getCurrentEmployee().getValue();

        String message = "Your EZ ride has been confirmed! Driver details are as follows: \n" +
                String.format("Driver Name: %s", driver.getName());

        SendSmsRequest smsRequest = new SendSmsRequest(AppConstants.TEXTLOCAL_API_KEY, customerMobile, SMS_SENDER, message);
        TextLocalRepository repository = new TextLocalRepository();
        repository.sendSMS(smsRequest, new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, Response<String> response) {
                Log.d(TAG, "onResponse: " + response.message());
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                Log.d(TAG, "onResponse: " + throwable);
            }
        });
    }

    private void observeDriverStatus(String driverId) {
        database
                .getReference()
                .getRoot()
                .child("active_drivers")
                .child(driverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            dashboardViewModel.setDriverStatus(DriverStatus.ON_DUTY);
                        }
                        else {
                            dashboardViewModel.setDriverStatus(DriverStatus.OFF_DUTY);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void registerBackgroundRequestReceiver() {
        if(requireActivity().getIntent() != null && requireActivity().getIntent().hasExtra("tripId")) {
            String tripId = requireActivity().getIntent().getStringExtra("tripId");
            Toast.makeText(requireActivity(), tripId, Toast.LENGTH_SHORT).show();
            receivedTripId = tripId;
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerForegroundRequestReceiver() {
        requestReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                      if(intent.getExtras() != null && intent.hasExtra("trip_id")) {
                          String tripId = intent.getStringExtra("trip_id");
                          Toast.makeText(context, tripId, Toast.LENGTH_SHORT).show();
                          receivedTripId = tripId;
                          observeTripStatus(tripId);
                          showRequestDialog(tripId);
                      }
            }
        };

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(requireContext());
        IntentFilter filter = new IntentFilter(AppConstants.SHOW_TRIP_REQUEST);

        broadcastManager.registerReceiver(requestReceiver, filter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentDriverDashboardBinding.inflate(getLayoutInflater());
        navController = NavHostFragment.findNavController(getChildFragmentManager().findFragmentById(R.id.driver_bottom_nav_container));
        AuthViewModel authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            if(firebaseUser == null) {
                Intent intent = new Intent(requireContext(), AuthActivity.class);
                startActivity(intent);
                requireActivity().finish();
                return;
            }
            String userId = firebaseUser.getUid();
            switch (userRole) {
                case EMPLOYEE_DRIVER:
                    employeeViewModel = new ViewModelProvider(requireActivity()).get(EmployeeViewModel.class);
                    tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);

                    OneTimeWorkRequest fetchEmployeeRequest = new OneTimeWorkRequest.Builder(FetchEmployeeWorker.class)
                            .setInputData(new Data.Builder()
                                    .putString("user_id", userId)
                                    .build())
                            .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                            .build();

                    WorkManager.getInstance(requireContext())
                            .enqueue(fetchEmployeeRequest);

                    WorkManager.getInstance(requireContext())
                            .getWorkInfoByIdLiveData(fetchEmployeeRequest.getId())
                            .observe(getViewLifecycleOwner(), workInfo -> {
                                if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                                    viewBinding.driverDashboardProgressbar.setVisibility(View.GONE);
                                    Map<String, Object> employeeMap = workInfo.getOutputData().getKeyValueMap();
                                    if(!employeeMap.isEmpty() && !employeeMap.containsKey("message")) {
                                        EmployeeDriver employee = EmployeeDriver.createFromMap(employeeMap);
                                        employeeViewModel.setEmployee(employee);
                                        if(receivedTripId != null) {
                                            observeTripStatus(receivedTripId);
                                            showRequestDialog(receivedTripId);
                                        }
                                        observeDriverStatus(employee.getDriverId());
                                        retrievePotentialTrip();
                                    }


                                }
                                else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                    viewBinding.driverDashboardProgressbar.setVisibility(View.GONE);
                                    String errorString = workInfo.getOutputData().getString("message") == null ? "Something went wrong." : workInfo.getOutputData().getString("message");
                                    Toast.makeText(requireActivity(), errorString, Toast.LENGTH_LONG).show();
                                    FirebaseService.getInstance().getAuthInstance().signOut();
                                    requireActivity().finish();
                                }
                                else if(workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                                    viewBinding.driverDashboardProgressbar.setVisibility(View.VISIBLE);
                                }
                            });

                    break;
                case DRIVER:
                    throw new UnsupportedOperationException("not yet implemented.");
            }

        });




        // Inflate the layout for this fragment

        NavigationBarView navigationBarView = viewBinding.bottomNavDriver;

        navigationBarView.setOnItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.navHome) {
                navigateToDestination(R.id.homeFragment);
            } else if (menuItem.getItemId() == R.id.navNotification) {
                navigateToDestination(R.id.notificationFragment);
            } else if (menuItem.getItemId() == R.id.navProfile) {
                navigateToDestination(R.id.profileFragment);
            }

            return true;
        });


        return viewBinding.getRoot();
    }

    private void navigateToDestination(@IdRes int destinationId) {
        int destinationTag = Integer.parseInt(navController.getGraph().findNode(destinationId).getLabel().toString());
        int currentDestinationTag = Integer.parseInt(navController.getCurrentDestination().getLabel().toString());

        int enterAnim;
        int exitAnim;

        if (destinationTag != currentDestinationTag) {
            if (destinationTag > currentDestinationTag) {
                enterAnim = R.anim.slide_in_right;
                exitAnim = R.anim.slide_out_left;
            } else {
                enterAnim = android.R.anim.slide_in_left;
                exitAnim = android.R.anim.slide_out_right;
            }

            navController.navigate(destinationId, null, new NavOptions.Builder().setEnterAnim(enterAnim).setExitAnim(exitAnim).build());
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        if(!hasLocationPermissions()) {
            List<String> permissions = new ArrayList<>();
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
            String[] permissionArray = permissions.toArray(new String[0]);

            ActivityCompat
                    .requestPermissions(requireActivity(), permissionArray, 111);
        }

        registerForegroundRequestReceiver();
        registerBackgroundRequestReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(requestReceiver != null) {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(requireContext());
            broadcastManager.unregisterReceiver(requestReceiver);
        }
    }

    private final DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG, "showRequestDialog: " + reference);
            reference.setValue(TripStatus.INITIATED);
        }
    };

    private void showRequestDialog(String tripId) {


        reference = database
                .getReference()
                .getRoot()
                .child("trips")
                .child(employeeViewModel.getCurrentEmployee().getValue().getOwnerId())
                .child(tripId)
                .child("status");


        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("New request")
                .setMessage("You have been assigned a new ride by your driver")
                .setPositiveButton("Accept", onClickListener)
                .setNegativeButton("Reject", (dialog, which) -> {
                    reference.setValue(TripStatus.REJECTED);
                })
                .show();
    }

    private boolean hasLocationPermissions() {
        return
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}