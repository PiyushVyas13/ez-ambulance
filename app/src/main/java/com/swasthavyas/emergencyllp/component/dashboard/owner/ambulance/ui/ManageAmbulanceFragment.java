package com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.adapter.AmbulanceAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentManageAmbulanceBinding;


public class ManageAmbulanceFragment extends Fragment {
    FragmentManageAmbulanceBinding viewBinding;
    OwnerViewModel ownerViewModel;


    public ManageAmbulanceFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentManageAmbulanceBinding.inflate(getLayoutInflater());
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);

        Owner currentOwner = ownerViewModel.getOwner().getValue();

        if(currentOwner != null) {
            AmbulanceAdapter ambulanceAdapter = new AmbulanceAdapter(currentOwner.getAmbulances().getValue());
            viewBinding.ambulanceList.setLayoutManager(new LinearLayoutManager(requireContext()));
            viewBinding.ambulanceList.setAdapter(ambulanceAdapter);

            currentOwner.getAmbulances().observe(getViewLifecycleOwner(), ambulances -> {
                if(ambulances != null) {
                    if(ambulances.isEmpty()) {
                        viewBinding.emptyText.setVisibility(View.VISIBLE);
                    }
                    else {
                        viewBinding.emptyText.setVisibility(View.GONE);
                    }
                    ambulanceAdapter.notifyDataSetChanged();
                }
            });
        }


        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }
}