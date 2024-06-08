package com.swasthavyas.emergencyllp.component.dashboard.owner.employee.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.work.Data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.viewmodel.DriverRegistrationViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentDriverPersonalDetailsBinding;
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.model.EmployeeDriver.ModelColumns;


public class DriverPersonalDetailsFragment extends Fragment {

    FragmentDriverPersonalDetailsBinding viewBinding;
    DriverRegistrationViewModel driverRegistrationViewModel;

    public DriverPersonalDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentDriverPersonalDetailsBinding.inflate(getLayoutInflater());

        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }


    private boolean isNumeric(@NonNull String string) {
        try {
            int someInt = Integer.parseInt(string);
            return true;
        }
        catch (NumberFormatException exception) {
            return false;
        }
    }


    public boolean validateData() {
        if(viewBinding.email.getText().toString().isEmpty() || viewBinding.driverAge.getText().toString().isEmpty() || viewBinding.nameDriver.getText().toString().isEmpty() || viewBinding.driverMobileNumber.getText().toString().isEmpty()) {
            Toast.makeText(requireActivity(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // TODO: Add email regex validation here
        // TODO: Add mobile number validation here

        if(!isNumeric(viewBinding.driverAge.getText().toString())) {
            Toast.makeText(requireActivity(), "Enter valid age", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public Data collectData() {
        Data.Builder dataBuilder = new Data.Builder();

        String email = viewBinding.email.getText().toString();
        String name = viewBinding.nameDriver.getText().toString();
        String age = viewBinding.driverAge.getText().toString();
        String mobile = viewBinding.driverMobileNumber.getText().toString();




        dataBuilder.putString(ModelColumns.EMAIL, email);
        dataBuilder.putString(ModelColumns.NAME, name);
        dataBuilder.putString(ModelColumns.PHONE_NUMBER, mobile);
        dataBuilder.putInt(ModelColumns.AGE, Integer.parseInt(age));

        return dataBuilder.build();
    }

}