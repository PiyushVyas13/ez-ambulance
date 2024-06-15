package com.swasthavyas.emergencyllp.component.dashboard.owner.trip.ui;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceTypes;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.databinding.FragmentDropLocationBinding;

import java.util.Arrays;


public class DropLocationFragment extends Fragment implements OnMapReadyCallback {

    FragmentDropLocationBinding viewBinding;
    private GoogleMap dropLocationMap;
    private Marker marker;
    private LatLng locationCoordinates;
    private String dropLocationAddress;

    public DropLocationFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentDropLocationBinding.inflate(getLayoutInflater());



        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SupportMapFragment mapFragment = viewBinding.dropLocationMap.getFragment();
        mapFragment.getMapAsync(this);

        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(20.5558, 78.6304),
                new LatLng(21.2720, 79.4864)
        );
        AutocompleteSupportFragment autocompleteFragment = viewBinding.autocompleteFragment.getFragment();
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG, Place.Field.VIEWPORT, Place.Field.ADDRESS));
        autocompleteFragment.setCountries("IN");
        autocompleteFragment.setLocationRestriction(bounds);
        autocompleteFragment.setTypesFilter(
                Arrays.asList(
                        PlaceTypes.HOSPITAL,
                        PlaceTypes.PHARMACY
                )
        );

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Log.d(TAG, "onError: " + status.getStatusMessage());
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng coordinates = place.getLatLng();

                if(coordinates != null) {
                    Log.d(TAG, "onPlaceSelected: " + place.getAddress());

                    locationCoordinates = place.getLatLng();
                    dropLocationAddress = place.getAddress();

                    marker.setPosition(coordinates);
                    dropLocationMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 20f));
                }
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        dropLocationMap = googleMap;
        marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));

    }

    public boolean validateData() {
        if(dropLocationAddress == null || dropLocationAddress.isEmpty() || locationCoordinates == null) {
            Toast.makeText(requireActivity(), "Please select an address!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public Bundle collectData() {
        Bundle bundle = new Bundle();

        bundle.putString("address", dropLocationAddress);
        bundle.putParcelable("coordinates", locationCoordinates);

        return bundle;
    }
}