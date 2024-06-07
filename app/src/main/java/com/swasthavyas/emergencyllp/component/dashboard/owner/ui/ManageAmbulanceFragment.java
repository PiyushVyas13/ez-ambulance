package com.swasthavyas.emergencyllp.component.dashboard.owner.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.adapter.AmbulanceAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.worker.AddAmbulanceWorker;
import com.swasthavyas.emergencyllp.databinding.FragmentAddAmbulanceBinding;
import com.swasthavyas.emergencyllp.databinding.FragmentManageAmbulanceBinding;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.util.types.AmbulanceType;


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