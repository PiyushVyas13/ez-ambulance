package com.swasthavyas.emergencyllp.component.dashboard.owner.trip.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.databinding.FragmentCustomerInfoBinding;


public class CustomerInfoFragment extends Fragment {
    FragmentCustomerInfoBinding viewBinding;


    public CustomerInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewBinding = FragmentCustomerInfoBinding.inflate(getLayoutInflater());



        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }

    private boolean isIntegerNumeric(String s) {
        try {
            int x =  Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDoubleNumeric(String s) {
        try {
            double x = Double.parseDouble(s);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
    public boolean validateData() {
        Editable customerName = viewBinding.customerName.getText();
        Editable customerAge = viewBinding.customerAge.getText();
        Editable estimatedPrice = viewBinding.estimatedPrice.getText();

        if(customerName == null || customerAge == null || estimatedPrice == null) {
            Toast.makeText(requireActivity(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(customerName.toString().isEmpty() || customerAge.toString().isEmpty() || estimatedPrice.toString().isEmpty()) {
            Toast.makeText(requireActivity(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!isIntegerNumeric(customerAge.toString()) || Integer.parseInt(customerAge.toString()) <= 0) {
            Toast.makeText(requireActivity(), "Enter valid age!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!isDoubleNumeric(estimatedPrice.toString()) || Double.parseDouble(estimatedPrice.toString()) < 0) {
            Toast.makeText(requireActivity(), "Enter valid price!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

    public Bundle collectData() {
        Bundle bundle = new Bundle();

        Editable customerName = viewBinding.customerName.getText();
        int customerAge = Integer.parseInt(viewBinding.customerAge.getText().toString());
        double estimatedPrice = Double.parseDouble(viewBinding.estimatedPrice.getText().toString());
        boolean isEmergencyRide = viewBinding.emergencyRideCheckbox.isChecked();

        if(customerName == null || customerAge <= 0 || estimatedPrice < 0) {
            return null;
        }

        bundle.putString("customer_name", customerName.toString());
        bundle.putInt("customer_age", customerAge);
        bundle.putDouble("estimated_price", estimatedPrice);
        bundle.putBoolean("is_emergency_ride", isEmergencyRide);

        return bundle;
    }
}