package com.swasthavyas.emergencyllp.component.dashboard.owner.employee.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.adapter.EmployeeAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentManageDriverBinding;


public class ManageDriverFragment extends Fragment {

    FragmentManageDriverBinding viewBinding;
    OwnerViewModel ownerViewModel;

    public ManageDriverFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewBinding = FragmentManageDriverBinding.inflate(getLayoutInflater());
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);

        Owner currentOwner = ownerViewModel.getOwner().getValue();

        if(currentOwner != null) {
            EmployeeAdapter employeeAdapter = new EmployeeAdapter(currentOwner.getEmployees().getValue());
            viewBinding.driverList.setLayoutManager(new LinearLayoutManager(requireContext()));
            viewBinding.driverList.setAdapter(employeeAdapter);

            currentOwner.getEmployees().observe(getViewLifecycleOwner(), employeeDrivers -> {
                if(employeeDrivers != null) {
                    employeeAdapter.notifyDataSetChanged();
                    if(employeeDrivers.isEmpty()) {
                        viewBinding.emptyText.setVisibility(View.VISIBLE);
                    }
                    else {
                        viewBinding.emptyText.setVisibility(View.GONE);
                    }
                }
            });
        }

        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }
}