package com.swasthavyas.emergencyllp.component.dashboard.owner.employee.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.adapter.EmployeeAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.worker.DeleteDriverWorker;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentManageDriverBinding;
import com.swasthavyas.emergencyllp.util.AppConstants;


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
            EmployeeAdapter.OnDeleteCallback onDeleteCallback = new EmployeeAdapter.OnDeleteCallback() {
                @Override
                public void onDelete(String driverId, int position) {
                    Data inputData = new Data.Builder()
                            .putString("owner_id", currentOwner.getId())
                            .putString("owner_uid", currentOwner.getUserId())
                            .putString("driver_id", driverId)
                            .build();

                    OneTimeWorkRequest deleteDriverRequest = new OneTimeWorkRequest.Builder(DeleteDriverWorker.class)
                            .setInputData(inputData)
                            .build();

                    WorkManager.getInstance(requireContext())
                            .enqueue(deleteDriverRequest);

                    WorkManager.getInstance(requireContext())
                            .getWorkInfoByIdLiveData(deleteDriverRequest.getId())
                            .observe(getViewLifecycleOwner(), workInfo -> {
                                if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                                    Toast.makeText(requireContext(), "Driver deleted successfully!", Toast.LENGTH_SHORT).show();
                                    currentOwner.deleteEmployee(position);

                                } else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                    Log.d(AppConstants.TAG, "onDelete: " + workInfo.getOutputData().getString("message"));
                                    Toast.makeText(requireActivity(), workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            };

            EmployeeAdapter employeeAdapter = new EmployeeAdapter(requireContext(), onDeleteCallback, currentOwner.getEmployees().getValue());
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