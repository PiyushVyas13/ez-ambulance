package com.swasthavyas.emergencyllp.component.registration.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.auth.viewmodel.AuthViewModel;
import com.swasthavyas.emergencyllp.component.registration.viewmodel.RegistrationViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentAddAmbulanceBinding;
import com.swasthavyas.emergencyllp.util.types.AmbulanceType;

import java.util.HashMap;
import java.util.Map;


public class AddAmbulanceFragment extends Fragment {
    FragmentAddAmbulanceBinding viewBinding;
    AuthViewModel authViewModel;
    RegistrationViewModel registrationViewModel;


    public AddAmbulanceFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        viewBinding = FragmentAddAmbulanceBinding.inflate(getLayoutInflater());
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        registrationViewModel = new ViewModelProvider(requireActivity()).get(RegistrationViewModel.class);


        if(authViewModel.getCurrentUser().getValue() == null) {
            Toast.makeText(requireContext(), "Unauthorized", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(getParentFragmentManager().findFragmentById(R.id.registration_nav_container)).popBackStack();
        }

        viewBinding.addAmbulanceBtn.setOnClickListener(v -> {

            if(viewBinding.ambulanceType.getCheckedRadioButtonId() == -1 || viewBinding.vehicleNumber.getText().toString().isEmpty() || viewBinding.vehicleType.getText().toString().isEmpty()) {
                Toast.makeText(requireActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            AmbulanceType ambulanceType = AmbulanceType.NONE;

            if(viewBinding.ambulanceType.getCheckedRadioButtonId() == viewBinding.advanceLifeSupport.getId()) {
                ambulanceType = AmbulanceType.ADVANCED;
            }
            else if(viewBinding.ambulanceType.getCheckedRadioButtonId() == viewBinding.basicLifeSupport.getId()) {
                ambulanceType = AmbulanceType.BASIC;
            }
            else if(viewBinding.ambulanceType.getCheckedRadioButtonId() == viewBinding.transport.getId()) {
                ambulanceType = AmbulanceType.MORTUARY;
            }

            String vehicleNumber = viewBinding.vehicleNumber.getText().toString();
            String vehicleType = viewBinding.vehicleType.getText().toString();


            Map<String, Object> ambulanceArgs = new HashMap<>();

            ambulanceArgs.put("ambulance_type", ambulanceType.name());
            ambulanceArgs.put("vehicle_number", vehicleNumber);
            ambulanceArgs.put("vehicle_type", vehicleType);

            registrationViewModel.setDriverAmbulance(ambulanceArgs);

            Navigation.findNavController(v).navigate(R.id.action_addAmbulanceFragment_to_documentInputFragment);


        });





        return viewBinding.getRoot();
    }
}