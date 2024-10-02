package com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.work.Data;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.databinding.FragmentCustomerInfoBinding;
import com.swasthavyas.emergencyllp.util.steppernav.NavigationStepFragment;


public class CustomerInfoFragment extends NavigationStepFragment {
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

    @Override
    public boolean validateData() {
        Editable customerName = viewBinding.customerName.getText();
        Editable customerAge = viewBinding.customerAge.getText();
        Editable customerMobile = viewBinding.customerMobile.getText();
        Editable estimatedPrice = viewBinding.estimatedPrice.getText();

        if(customerName == null || customerAge == null || estimatedPrice == null || customerMobile == null) {
            Toast.makeText(requireActivity(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(customerName.toString().isEmpty() || customerAge.toString().isEmpty() || estimatedPrice.toString().isEmpty() || customerMobile.toString().isEmpty()) {
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

        if(customerMobile.toString().length() != 10) {
            Toast.makeText(requireActivity(), "Enter valid phone number!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

    @Override
    public Data collectData() {
        Data.Builder dataBuilder = new Data.Builder();

        Editable customerName = viewBinding.customerName.getText();
        Editable customerMobile = viewBinding.customerMobile.getText();
        int customerAge = Integer.parseInt(viewBinding.customerAge.getText().toString());
        double estimatedPrice = Double.parseDouble(viewBinding.estimatedPrice.getText().toString());
        boolean isEmergencyRide = viewBinding.emergencyRideCheckbox.isChecked();

        if(customerName == null || customerMobile == null || customerAge <= 0 || estimatedPrice < 0) {
            return null;
        }

        dataBuilder.putString(Trip.ModelColumns.CUSTOMER_NAME, customerName.toString());
        dataBuilder.putString(Trip.ModelColumns.CUSTOMER_MOBILE, "91" + customerMobile.toString());
        dataBuilder.putInt(Trip.ModelColumns.CUSTOMER_AGE, customerAge);
        dataBuilder.putDouble(Trip.ModelColumns.PRICE, estimatedPrice);
        dataBuilder.putBoolean(Trip.ModelColumns.IS_EMERGENCY_RIDE, isEmergencyRide);

        return dataBuilder.build();
    }
}