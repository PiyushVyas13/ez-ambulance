package com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.adapter.AmbulanceAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.worker.DeleteAmbulanceWorker;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentManageAmbulanceBinding;
import com.swasthavyas.emergencyllp.util.AppConstants;


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

            AmbulanceAdapter.OnDeleteCallback deleteCallback = new AmbulanceAdapter.OnDeleteCallback() {
                @Override
                public void onDelete(String ambulanceId, String imageRef, int position) {
                    Data inputData = new Data.Builder()
                            .putString(Ambulance.ModelColumns.ID, ambulanceId)
                            .putString(Ambulance.ModelColumns.OWNER_ID, currentOwner.getId())
                            .putString(Ambulance.ModelColumns.IMAGE_REF, imageRef)
                            .build();

                    OneTimeWorkRequest deleteAmbulanceRequest = new OneTimeWorkRequest.Builder(DeleteAmbulanceWorker.class)
                            .setInputData(inputData)
                            .build();


                    WorkManager.getInstance(requireContext())
                            .enqueue(deleteAmbulanceRequest);


                    WorkManager.getInstance(requireContext())
                            .getWorkInfoByIdLiveData(deleteAmbulanceRequest.getId())
                            .observe(getViewLifecycleOwner(), workInfo -> {
                                if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                                    Toast.makeText(requireActivity(), "Ambulance Deleted Successfully!", Toast.LENGTH_SHORT).show();
                                    currentOwner.deleteAmbulance(position);
                                }
                                else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                    Log.d(AppConstants.TAG, "onDelete: " + workInfo.getOutputData().getString("message"));
                                    Toast.makeText(requireActivity(), workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            };


            AmbulanceAdapter ambulanceAdapter = new AmbulanceAdapter(requireContext(), currentOwner.getAmbulances().getValue(), deleteCallback);
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