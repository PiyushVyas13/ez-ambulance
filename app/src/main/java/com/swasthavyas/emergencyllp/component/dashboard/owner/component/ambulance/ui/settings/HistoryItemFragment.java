package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.ui.settings;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StrokeStyle;
import com.google.android.gms.maps.model.StyleSpan;
import com.google.maps.android.PolyUtil;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel.HistoryViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentHistoryItemBinding;
import com.swasthavyas.emergencyllp.util.TimestampUtility;

import java.util.List;


public class HistoryItemFragment extends Fragment implements OnMapReadyCallback {
    private HistoryViewModel historyViewModel;
    private FragmentHistoryItemBinding viewBinding;
    private List<String> routePolyLines;

    private String displayName;
    private String displayLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HistoryItemFragmentArgs fragmentArgs = HistoryItemFragmentArgs.fromBundle(getArguments());
        displayName = fragmentArgs.getDisplayName();
        displayLabel = fragmentArgs.getDisplayLabel();


        historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);

        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(viewBinding.getRoot()).navigateUp();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

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

            viewBinding.displayLabel.setText(String.format("%s: ", displayLabel));
            viewBinding.name.setText(displayName);
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
            routePolyLines = history.getRoutePolyLines();
        });


        return viewBinding.getRoot();
    }

    private String getAmbulanceNumber(String assignedAmbulanceId) {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SupportMapFragment routeMapFragment = viewBinding.routePreviewMap.getFragment();
        routeMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        if(routePolyLines != null && !routePolyLines.isEmpty()) {
            renderPolylines(googleMap, routePolyLines);
        }
    }

    private void renderPolylines(@NonNull GoogleMap map, List<String> routePolyLines) {
        map.clear();

        List<LatLng> pickupRouteCoordinates = PolyUtil.decode(routePolyLines.get(0));
        List<LatLng> dropRouteCoordinates = PolyUtil.decode(routePolyLines.get(1));

        PolylineOptions pickupRouteOptions = new PolylineOptions()
                .addAll(pickupRouteCoordinates)
                .addSpan(new StyleSpan(StrokeStyle.gradientBuilder(Color.RED, Color.YELLOW).build()));

        PolylineOptions dropRouteOptions = new PolylineOptions()
                .addAll(dropRouteCoordinates)
                .addSpan(new StyleSpan(StrokeStyle.gradientBuilder(Color.RED, Color.YELLOW).build()));

        LatLng startLocation = pickupRouteCoordinates.get(0);
        LatLng pickupLocation = pickupRouteCoordinates.get(pickupRouteCoordinates.size() - 1);
        LatLng dropLocation = dropRouteCoordinates.get(dropRouteCoordinates.size() - 1);


        map.addPolyline(pickupRouteOptions);
        map.addPolyline(dropRouteOptions);

        map.addMarker(new MarkerOptions().position(startLocation).icon(getBitmapFromVector()));
        map.addMarker(new MarkerOptions().position(pickupLocation));
        map.addMarker(new MarkerOptions().position(dropLocation));

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 12f));
    }

    private BitmapDescriptor getBitmapFromVector() {
        Drawable vectorDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.runninglocation);

        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}