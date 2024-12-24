package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.ui;

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
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.adapter.AmbulanceAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel.AmbulanceViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.worker.DeleteAmbulanceWorker;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.TripViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentManageAmbulanceBinding;
import com.swasthavyas.emergencyllp.util.AppConstants;

import java.util.ArrayList;
import java.util.List;


public class ManageAmbulanceFragment extends Fragment {
    FragmentManageAmbulanceBinding viewBinding;
    OwnerViewModel ownerViewModel;
    AmbulanceViewModel ambulanceViewModel;
    TripViewModel tripViewModel;


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
        tripViewModel = new ViewModelProvider(requireActivity()).get(TripViewModel.class);
        ambulanceViewModel = new ViewModelProvider(requireActivity()).get(AmbulanceViewModel.class);

        Owner currentOwner = ownerViewModel.getOwner().getValue();

        if(currentOwner != null) {

            AmbulanceAdapter.OnDeleteCallback deleteCallback = (ambulanceId, ambulanceNumber, imageRef, position) -> {
                Data inputData = new Data.Builder()
                        .putString(Ambulance.ModelColumns.ID, ambulanceId)
                        .putString(Ambulance.ModelColumns.OWNER_ID, currentOwner.getId())
                        .putString(Ambulance.ModelColumns.IMAGE_REF, imageRef)
                        .putString(Ambulance.ModelColumns.VEHICLE_NUMBER, ambulanceNumber)
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
            };


            AmbulanceAdapter ambulanceAdapter = new AmbulanceAdapter(requireContext(), currentOwner.getAmbulances().getValue(), currentOwner.getEmployees().getValue(), new ArrayList<>(), deleteCallback, ((position, ambulance) -> {
                ambulanceViewModel.setCurrentAmbulance(ambulance);
                Navigation.findNavController(viewBinding.getRoot()).navigate(R.id.ambulanceDetailFragment, null, new NavOptions.Builder().setEnterAnim(R.anim.slide_in_right).setExitAnim(android.R.anim.fade_out).build());
            }));


            viewBinding.ambulanceList.setLayoutManager(new LinearLayoutManager(requireContext()));
            viewBinding.ambulanceList.setAdapter(ambulanceAdapter);

            tripViewModel.getActiveTripsLiveData().observe(getViewLifecycleOwner(), trips -> {
                List<String> activeAmbulanceIds = new ArrayList<>();

                for(Trip trip : trips) {
                    activeAmbulanceIds.add(trip.getAssignedAmbulanceId());
                }

                ambulanceAdapter.setAvailableAmbulanceIds(activeAmbulanceIds);
            });


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