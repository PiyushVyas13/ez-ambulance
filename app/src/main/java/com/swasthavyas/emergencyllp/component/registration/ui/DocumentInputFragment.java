package com.swasthavyas.emergencyllp.component.registration.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.swasthavyas.emergencyllp.component.registration.viewmodel.RegistrationViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentDocumentInputBinding;


public class DocumentInputFragment extends Fragment {
    FragmentDocumentInputBinding viewBinding;

    public DocumentInputFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RegistrationViewModel viewModel = new ViewModelProvider(requireActivity()).get(RegistrationViewModel.class);
        viewBinding = FragmentDocumentInputBinding.inflate(getLayoutInflater());


        viewBinding.finishButton.setOnClickListener(v -> {
            if(viewBinding.aadhaarNo.getText().toString().isEmpty()) {
                Toast.makeText(requireActivity(), "Please provide aadhaar number.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(viewBinding.aadhaarNo.getText().length() != 12) {
                Toast.makeText(requireActivity(), "Provide a valid aadhaar number.", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.setAadhaarNumber(viewBinding.aadhaarNo.getText().toString());

        });


        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }
}