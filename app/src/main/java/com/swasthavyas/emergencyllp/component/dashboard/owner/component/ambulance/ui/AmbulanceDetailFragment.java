package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.ui;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.driver.ui.dialog.DriverSearchFragment;
import com.swasthavyas.emergencyllp.component.dashboard.driver.worker.AssignAmbulanceWorker;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.TripViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentAmbulanceDetailBinding;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class AmbulanceDetailFragment extends Fragment implements OnMapReadyCallback {

    FragmentAmbulanceDetailBinding viewBinding;
    TripViewModel tripViewModel;


    public AmbulanceDetailFragment() {
        // Required empty public constructor
    }

    private Ambulance ambulance;
    private EmployeeDriver assignedDriver;
    private OwnerViewModel ownerViewModel;

    private GoogleMap ambulanceLocationMap;
    private Marker marker;

    private AtomicReference<Boolean> isActiveRef = new AtomicReference<>(false);
    private AtomicReference<Boolean> isOnRideRef = new AtomicReference<>(false);

    private DatabaseReference tripReference;
    private final ValueEventListener tripListener  = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()) {
                String status = (String) snapshot.getValue();
                TripStatus tripStatus = TripStatus.valueOf(status);

                switch (tripStatus) {
                    case PENDING_RESPONSE:
                        viewBinding.assignRideButton.setEnabled(false);
                        viewBinding.assignRideButton.setText("Awaiting driver's response");
                        break;
                    case INITIATED:
                        viewBinding.assignRideButton.setEnabled(false);
                        viewBinding.assignRideButton.setText("Driver Accepted Ride");
                        viewBinding.ambulanceRideIndicator.setVisibility(View.VISIBLE);
                        break;
                    case REJECTED:
                        Toast.makeText(requireActivity(), "Driver Rejected Ride", Toast.LENGTH_SHORT).show();
                        viewBinding.assignRideButton.setEnabled(true);
                        viewBinding.assignRideButton.setText("Assign Ride");
                        break;
                }

            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);


        if(getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ambulance = getArguments().getParcelable("ambulance", Ambulance.class);
            }
            else {
                ambulance = getArguments().getParcelable("ambulance");
            }


        }
        else  {
            Navigation.findNavController(viewBinding.getRoot()).popBackStack();
        }

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(viewBinding.getRoot())
                        .navigate(
                                R.id.ownerHomeFragment,
                                null,
                                new NavOptions.Builder().
                                        setEnterAnim(android.R.anim.fade_in).
                                        setExitAnim(android.R.anim.slide_out_right)
                                        .build()
                        );
            }
        };


        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

    }

    @Override
    @SuppressWarnings({"unchecked"})
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewBinding = FragmentAmbulanceDetailBinding.inflate(getLayoutInflater());
        tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);



        viewBinding.settingsList.setAdapter(new SettingsOptionAdapter(requireContext()));
        viewBinding.ambulanceDetailTitle.setText(ambulance.getVehicleNumber());

        ownerViewModel.getOwner().observe(getViewLifecycleOwner(), owner -> {

            owner.getEmployees().observe(getViewLifecycleOwner(), drivers -> {
                drivers.stream()
                        .filter(driver -> driver.getAssignedAmbulanceNumber().equals(ambulance.getVehicleNumber()))
                        .findFirst()
                        .ifPresent(driver -> assignedDriver = driver);


                if (assignedDriver == null || assignedDriver.getAssignedAmbulanceNumber().equals("None")) {
                    viewBinding.assignedDriverName.setText(R.string.tap_to_select_a_driver);
                    viewBinding.assignRideButton.setEnabled(false);

                    DriverSearchFragment dialogFragment = getDriverSearchFragment(owner);


                    viewBinding.assignedDriverName.setOnClickListener(v -> {

                        dialogFragment.show(getChildFragmentManager(), "SOME_TAG");
                        getChildFragmentManager().executePendingTransactions();

                        dialogFragment.getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


                    });
                } else {
                    viewBinding.assignedDriverName.setText(String.format("Assigned to: %s", assignedDriver.getName()));


                    Bundle bundle = new Bundle();
                    bundle.putParcelable("ambulance", ambulance);
                    bundle.putParcelable("assigned_driver", assignedDriver);
                    viewBinding.assignRideButton.setOnClickListener(v -> {
                        Navigation.findNavController(v).navigate(R.id.rideAssignmentFragment, bundle,
                                new NavOptions.Builder()
                                        .setEnterAnim(android.R.anim.slide_in_left)
                                        .setExitAnim(android.R.anim.fade_out)
                                        .setPopEnterAnim(android.R.anim.fade_in)
                                        .setPopExitAnim(R.anim.slide_out_left)
                                        .build());
                    });


                    FirebaseDatabase database = FirebaseService.getInstance().getDatabaseInstance();
//        database.useEmulator("10.0.2.2", 8000);

                    database
                            .getReference()
                            .getRoot()
                            .child("active_drivers")
                            .child(assignedDriver.getDriverId())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Log.d(TAG, "Location Changed: " + snapshot.getValue());
                                        List<Double> coords = (List<Double>) snapshot.getValue();

                                        viewBinding.assignedDriverName.setText(String.format("Assigned to: %s (active)", assignedDriver.getName()));
                                        isActiveRef.set(true);

                                        viewBinding.assignRideButton.setEnabled(!isOnRideRef.get() && isActiveRef.get());

                                        LatLng coordinates = new LatLng(coords.get(0), coords.get(1));
                                        if (ambulanceLocationMap != null) {
                                            if (marker == null) {
                                                marker = ambulanceLocationMap.addMarker(new MarkerOptions().position(coordinates).title("Driver Location"));
                                            } else {
                                                marker.setPosition(coordinates);
                                            }
                                            ambulanceLocationMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 20f));
                                        }


                                    } else {
                                        viewBinding.assignRideButton.setEnabled(false);
                                        viewBinding.assignedDriverName.setText(String.format("Assigned to: %s (inactive)", assignedDriver.getName()));
                                        isActiveRef.set(false);
                                        Log.d(TAG, "onDataChange: snapshot does not exist (yet).");
                                        if(assignedDriver.getLastLocation() != null) {
                                            LatLng coordinates = new LatLng(assignedDriver.getLastLocation().get(0), assignedDriver.getLastLocation().get(1));
                                            if (ambulanceLocationMap != null) {
                                                if (marker == null) {
                                                    marker = ambulanceLocationMap.addMarker(new MarkerOptions().position(coordinates).title("Driver Location"));
                                                } else {
                                                    marker.setPosition(coordinates);
                                                }
                                                ambulanceLocationMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 20f));
                                            }
                                        }
                                        //TODO: Get the last known location from persistent database.
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "onCancelled: " + error);
                                }
                            });

                    tripViewModel.getActiveTripsLiveData().observe(getViewLifecycleOwner(), trips -> {
                        Trip potentialTrip = trips
                                .stream()
                                .filter(trip -> trip.getAssignedAmbulanceId().equals(ambulance.getId()))
                                .findFirst()
                                .orElse(null);

                        if (potentialTrip != null) {
                            String tripId = potentialTrip.getId();
                            tripReference = database
                                    .getReference()
                                    .getRoot()
                                    .child("trips")
                                    .child(potentialTrip.getOwnerId())
                                    .child(tripId)
                                    .child("status");

                            isOnRideRef.set(true);
                            tripReference.addValueEventListener(tripListener);
                        } else {
                            viewBinding.assignRideButton.setEnabled(isActiveRef.get());
                            isOnRideRef.set(false);
                            viewBinding.assignRideButton.setText("Assign Ride");
                            viewBinding.ambulanceRideIndicator.setVisibility(View.GONE);
                        }
                    });


                }

            });

        });

        return viewBinding.getRoot();
    }

    @NonNull
    private DriverSearchFragment getDriverSearchFragment(Owner owner) {
        DriverSearchFragment.DriverSearchDialogListener searchDialogListener = (dialog, driver) -> {

            OneTimeWorkRequest assignAmbulanceRequest = new OneTimeWorkRequest.Builder(AssignAmbulanceWorker.class)
                    .setInputData(new Data.Builder()
                            .putString("ambulance_number", ambulance.getVehicleNumber())
                            .putString("driver_mail", driver.getEmail())
                            .build())
                    .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build();

            WorkManager.getInstance(requireContext())
                            .enqueue(assignAmbulanceRequest);

            WorkManager.getInstance(requireContext())
                            .getWorkInfoByIdLiveData(assignAmbulanceRequest.getId())
                                    .observe(getViewLifecycleOwner(), workInfo -> {
                                        if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                                            owner.assignAmbulanceToEmployee(ambulance.getVehicleNumber(), driver.getDriverId());
                                            dialog.dismiss();
                                        }
                                        else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                            dialog.dismiss();
                                            Toast.makeText(requireActivity(), workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                                        }
                                    });
        };

        DriverSearchFragment dialogFragment = new DriverSearchFragment(
                owner.getEmployees().getValue(), searchDialogListener
        );
        return dialogFragment;
    }

    private static final List<String> optionsList = Arrays.asList("About Ambulance", "History");
    private static final List<String> optionDescList = Arrays.asList(
            "Show information about the ambulance",
            "View past rides, earnings etc."
    );

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        ambulanceLocationMap = googleMap;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SupportMapFragment mapFragment = viewBinding.ambulanceLocationMap.getFragment();
        mapFragment.getMapAsync(this);
    }

    private static class SettingsOptionAdapter extends BaseAdapter {

        private final Context context;

        public SettingsOptionAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return optionsList.size();
        }

        @Override
        public Object getItem(int position) {
            return optionsList.get(position);

        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null) {
                convertView = LayoutInflater.from(context)
                        .inflate(R.layout.ambulance_options_list_item, parent, false);
            }

            TextView optionTitle = convertView.findViewById(R.id.option_title);
            TextView optionDesc = convertView.findViewById(R.id.option_desc);

            optionTitle.setText(optionsList.get(position));
            optionDesc.setText(optionDescList.get(position));

            return convertView;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(tripReference != null) {
            tripReference.removeEventListener(tripListener);
        }
    }
}