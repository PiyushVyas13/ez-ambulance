package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentAmbulanceDetailBinding;

import java.util.Arrays;
import java.util.List;


public class AmbulanceDetailFragment extends Fragment implements OnMapReadyCallback {

    FragmentAmbulanceDetailBinding viewBinding;


    public AmbulanceDetailFragment() {
        // Required empty public constructor
    }

    private Ambulance ambulance;
    private EmployeeDriver assignedDriver;
    private OwnerViewModel ownerViewModel;

    private GoogleMap ambulanceLocationMap;



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

            List<EmployeeDriver> employees = ownerViewModel.getOwner().getValue().getEmployees().getValue();
            employees.stream()
                    .filter(employeeDriver -> employeeDriver.getAssignedAmbulanceNumber().equals(ambulance.getVehicleNumber()))
                    .findFirst()
                    .ifPresent(employeeDriver -> assignedDriver = employeeDriver);

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewBinding = FragmentAmbulanceDetailBinding.inflate(getLayoutInflater());


        viewBinding.settingsList.setAdapter(new SettingsOptionAdapter(requireContext()));
        viewBinding.ambulanceDetailTitle.setText(ambulance.getVehicleNumber());
        if(assignedDriver == null || assignedDriver.getAssignedAmbulanceNumber().equals("None")) {
            viewBinding.assignedDriverName.setText("Tap to select a driver");
        }
        else {
            viewBinding.assignedDriverName.setText("Assigned to: " + assignedDriver.getName());
        }

        viewBinding.assignRideButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.rideAssignmentFragment, null,
                    new NavOptions.Builder()
                            .setEnterAnim(android.R.anim.slide_in_left)
                            .setExitAnim(android.R.anim.fade_out)
                            .setPopEnterAnim(android.R.anim.fade_in)
                            .setPopExitAnim(R.anim.slide_out_left)
                            .build());
        });

        return viewBinding.getRoot();
    }

    private static final List<String> optionsList = Arrays.asList("About Ambulance", "History");
    private static final List<String> optionDescList = Arrays.asList(
            "Show information about the ambulance",
            "View past rides, earnings etc."
    );

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        ambulanceLocationMap = googleMap;
        googleMap.addMarker(
                new MarkerOptions()
                        .position(new LatLng(21.1458, 79.0882))
                        .title("Marker")
        );
        googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(new LatLng(21.1458, 79.0882), 15.0f)
        );
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
}