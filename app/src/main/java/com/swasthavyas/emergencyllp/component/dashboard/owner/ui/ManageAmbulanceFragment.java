package com.swasthavyas.emergencyllp.component.dashboard.owner.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
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
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.worker.AddAmbulanceWorker;
import com.swasthavyas.emergencyllp.databinding.FragmentManageAmbulanceBinding;
import com.swasthavyas.emergencyllp.util.types.AmbulanceType;


public class ManageAmbulanceFragment extends Fragment {
    OwnerViewModel ownerViewModel;
    FragmentManageAmbulanceBinding viewBinding;


    public ManageAmbulanceFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentManageAmbulanceBinding.inflate(getLayoutInflater());
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);

        ownerViewModel.getOwner().observe(getViewLifecycleOwner(), owner -> {
            if(owner != null) {
                Log.d("MYAPP", "ManageAmbulanceFragment.onCreateView: " + owner);
                Toast.makeText(requireContext(), owner.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        viewBinding.addAmbulanceBtn.setOnClickListener(v -> {
            if(ownerViewModel.getOwner().getValue() == null) {
                Toast.makeText(requireContext(), "Unauthorized", Toast.LENGTH_SHORT).show();
                return;
            }

            if(viewBinding.ambulanceType.getCheckedRadioButtonId() == -1 || viewBinding.vehicleNumber.getText().toString().isEmpty() || viewBinding.vehicleType.getText().toString().isEmpty()) {
                Toast.makeText(requireActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String ownerId = ownerViewModel.getOwner().getValue().getId();
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

            OneTimeWorkRequest addAmbulanceRequest = new OneTimeWorkRequest.Builder(AddAmbulanceWorker.class)
                    .setInputData(new Data.Builder()
                            .putString("ownerId", ownerId)
                            .putString("ambulanceType", ambulanceType.name())
                            .putString("vehicleType", vehicleType)
                            .putString("vehicleNumber", vehicleNumber)
                            .build())
                    .build();


            WorkManager.getInstance(requireContext())
                    .enqueue(addAmbulanceRequest);

            WorkManager.getInstance(requireContext())
                    .getWorkInfoByIdLiveData(addAmbulanceRequest.getId())
                    .observe(getViewLifecycleOwner(), workInfo -> {
                        if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                            Toast.makeText(requireActivity(), "Ambulance Added Successfully", Toast.LENGTH_SHORT).show();

                            if(ownerViewModel.getOwner().getValue() == null) {
                                Toast.makeText(requireContext(), "Unauthorized", Toast.LENGTH_SHORT).show();
                                return;
                            }

                             ownerViewModel.getOwner().getValue().addAmbulance(Ambulance.createFromMap(workInfo.getOutputData().getKeyValueMap()));

                            viewBinding.ambulanceType.clearCheck();
                            viewBinding.vehicleType.setText("");
                            viewBinding.vehicleType.setText("");
                            viewBinding.vehicleNumber.setText("");
                            viewBinding.addAmbulanceProgressbar.setVisibility(View.GONE);

                            Navigation.findNavController(v).navigate(R.id.ownerHomeFragment, null, new NavOptions.Builder().setEnterAnim(android.R.anim.slide_in_left).setExitAnim(android.R.anim.slide_out_right).build());
                        }
                        else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                            Toast.makeText(requireContext(), workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                            viewBinding.ambulanceType.clearCheck();
                            viewBinding.vehicleType.setText("");
                            viewBinding.vehicleType.setText("");
                            viewBinding.vehicleNumber.setText("");
                            viewBinding.addAmbulanceProgressbar.setVisibility(View.GONE);
                        }
                        else if(workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                            viewBinding.addAmbulanceProgressbar.setVisibility(View.VISIBLE);
                        }
                    });

        });


        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }
}