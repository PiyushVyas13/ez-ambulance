package com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceTypes;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.swasthavyas.emergencyllp.databinding.FragmentPickupLocationBinding;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.steppernav.NavigationStepFragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class PickupLocationFragment extends NavigationStepFragment implements OnMapReadyCallback {

    FragmentPickupLocationBinding viewBinding;
    GoogleMap pickupLocationMap;

    private PlacesClient placesClient;
    private Marker marker;
    private LatLng locationCoordinates;

    private final ActivityResultLauncher<Intent> placeAutocompleteWidget = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if(intent != null) {
                        Place place = Autocomplete.getPlaceFromIntent(intent);
                        Log.d(AppConstants.TAG, "AddressComponents: " + place.getAddressComponents());
                        fillAddress(place);
                    }
                }
                else if(result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.d(AppConstants.TAG, "onCreateView: autocomplete cancelled");
                }
            }
    );

    public PickupLocationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        placesClient = Places.createClient(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentPickupLocationBinding.inflate(getLayoutInflater());
        viewBinding.state.setEnabled(false);
        viewBinding.pincode.setEnabled(false);

        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(20.5558, 78.6304),
                new LatLng(21.2720, 79.4864)
        );

        viewBinding.streetAddressInput.setOnFocusChangeListener((view, hasFocus) -> {
            if(hasFocus && viewBinding.streetAddressInput.getText().toString().isEmpty()) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG, Place.Field.VIEWPORT);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setCountries(Collections.singletonList("IN"))
                        .setLocationRestriction(bounds)
                        .setTypesFilter(Arrays.asList(
                                PlaceTypes.STREET_NUMBER,
                                PlaceTypes.STREET_ADDRESS,
                                PlaceTypes.ROUTE,
                                PlaceTypes.LOCALITY,
                                PlaceTypes.SUBLOCALITY
                        ))
                        .build(requireContext());

                placeAutocompleteWidget.launch(intent);
            }

        });

        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SupportMapFragment mapFragment = viewBinding.pickupLocationMap.getFragment();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.pickupLocationMap = googleMap;
        marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
    }

    private void fillAddress(@NonNull Place place) {
        AddressComponents addressComponents = place.getAddressComponents();
        StringBuilder streetAddress = new StringBuilder();
        if(addressComponents != null) {
            for(AddressComponent addressComponent : addressComponents.asList()) {
                String type = addressComponent.getTypes().get(0);


                switch (type) {
                    case "street_number":
                        streetAddress.insert(0, addressComponent.getName());
                        break;
                    case "route":
                        if(!addressComponent.getName().isEmpty()) {
                            streetAddress.append(" ").append(addressComponent.getShortName()).append(", ");
                        }

                        break;
                    case "sublocality":
                    case "sublocality_level_1":
                    case "locality":
                        if(!addressComponent.getName().isEmpty()) {
                            streetAddress.append(addressComponent.getName()).append(", ");
                        }
                        break;
                    case "administrative_area_level_3":
                    case "city":
                        viewBinding.city.setText(addressComponent.getName());
                        break;
                    case "administrative_area_level_1":
                        viewBinding.state.setText(addressComponent.getName());
                        break;
                    case "postal_code":
                        viewBinding.pincode.setText(addressComponent.getName());
                        break;
                }
            }
            viewBinding.streetAddressInput.setText(sanitizeAddress(streetAddress.toString()));
            viewBinding.landmark.requestFocus();

            showMap(place);
            locationCoordinates = place.getLatLng();
        }

    }

    private static String sanitizeAddress(String rawAddressString) {
        if(rawAddressString == null || rawAddressString.isEmpty()) {
            return rawAddressString;
        }

        String sanitizedString = rawAddressString
                .trim()
                .replaceAll(",+", ",")
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*,\\s*", ",");

        if(sanitizedString.endsWith(",")) {
            sanitizedString = sanitizedString.substring(0, sanitizedString.length()-1);
        }

        return sanitizedString;
    }

    private void showMap(@NonNull Place place) {
        LatLng coordinates = place.getLatLng();
        if(pickupLocationMap != null && coordinates != null) {
            marker.setPosition(coordinates);
            pickupLocationMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15f));
        }

    }

    @Override
    public boolean validateData() {
        String streetAdress = viewBinding.streetAddressInput.getText().toString();
        String city = viewBinding.city.getText().toString();
        String state = viewBinding.state.getText().toString();
        String pincode = viewBinding.pincode.getText().toString();

        if(streetAdress.isEmpty() || city.isEmpty() || state.isEmpty() || pincode.isEmpty()) {
            Toast.makeText(requireContext(), "Please provide complete address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(locationCoordinates == null) {
            Log.d(AppConstants.TAG, "validateData: locationCoordinates are null.");
            return false;
        }

        return true;
    }
    @Override
    public Data collectData() {
        String streetAdress = viewBinding.streetAddressInput.getText().toString();
        String city = viewBinding.city.getText().toString();
        String landmark = viewBinding.landmark.getText().toString();
        String state = viewBinding.state.getText().toString();
        String pincode = viewBinding.pincode.getText().toString();

        Data.Builder dataBuilder = new Data.Builder();

        StringBuilder pickupLocationAddress = new StringBuilder();

        pickupLocationAddress
                .append(streetAdress)
                .append(", ");

        if(!landmark.isEmpty()) {
            pickupLocationAddress.append(landmark).append(", ");
        }

        pickupLocationAddress
                .append(city)
                .append(", ")
                .append(state)
                .append(", ")
                .append(pincode)
                .trimToSize();


        dataBuilder.putString("pickup_location_address", sanitizeAddress(pickupLocationAddress.toString()));
        dataBuilder.putDoubleArray("pickup_location_coordinates", new double[] {locationCoordinates.latitude, locationCoordinates.longitude});

        return dataBuilder.build();
    }
}