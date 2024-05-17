package com.swasthavyas.emergencyllp.component.registration.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.databinding.FragmentAddAmbulanceBinding;


public class AddAmbulanceFragment extends Fragment {
    FragmentAddAmbulanceBinding viewBinding;


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




        return viewBinding.getRoot();
    }
}