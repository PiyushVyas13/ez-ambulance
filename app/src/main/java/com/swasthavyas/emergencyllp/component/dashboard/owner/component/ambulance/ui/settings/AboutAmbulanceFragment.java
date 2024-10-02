package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.ui.settings;

import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.driver.ui.dialog.DriverSearchFragment;
import com.swasthavyas.emergencyllp.component.dashboard.driver.worker.AssignAmbulanceWorker;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel.AmbulanceViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentAboutAmbulanceBinding;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class
AboutAmbulanceFragment extends Fragment {
    private FragmentAboutAmbulanceBinding viewBinding;
    private AmbulanceViewModel ambulanceViewModel;
    private OwnerViewModel ownerViewModel;

    DriverSearchFragment dialogFragment;

    public AboutAmbulanceFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ambulanceViewModel = new ViewModelProvider(requireActivity()).get(AmbulanceViewModel.class);
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);


        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(viewBinding.getRoot())
                        .navigate(
                                R.id.ambulanceDetailFragment,
                                null,
                                new NavOptions.Builder()
                                        .setEnterAnim(android.R.anim.fade_in)
                                        .setExitAnim(android.R.anim.slide_out_right)
                                        .build()
                        );
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentAboutAmbulanceBinding.inflate(getLayoutInflater());

        ambulanceViewModel.getCurrentAmbulance().observe(getViewLifecycleOwner(), ambulance -> {

            Owner owner = ownerViewModel.getOwner().getValue();

            if(owner == null) {
                Toast.makeText(requireActivity(), "Unauthorized!", Toast.LENGTH_SHORT).show();
                return;
            }

            dialogFragment = getDriverSearchFragment(ambulance, owner);
            viewBinding.ambulanceType.setText(ambulance.getAmbulanceType().name());
            viewBinding.vehicleType.setText(ambulance.getVehicleType());
            viewBinding.vehicleNumber.setText(ambulance.getVehicleNumber());

            Glide.with(requireContext())
                    .load(ambulance.getImageRef())
                    .into(viewBinding.ambulanceImage);
        });



        ambulanceViewModel.getAssignedDriver().observe(getViewLifecycleOwner(), assignedDriver -> {
            if(assignedDriver == null) {
                viewBinding.assignedDriverName.setText("No Driver Assigned");
            } else {
                viewBinding.assignedDriverName.setText(assignedDriver.getName());
            }

            viewBinding.assignDriverButton.setOnClickListener(v -> {
                dialogFragment.show(getChildFragmentManager(), "SOME_TAG");
                getChildFragmentManager().executePendingTransactions();

                dialogFragment.getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            });

        });


        return viewBinding.getRoot();
    }

    private DriverSearchFragment getDriverSearchFragment(Ambulance ambulance, Owner owner) {
        DriverSearchFragment.DriverSearchDialogListener searchDialogListener = (dialog, driver) -> {

            OneTimeWorkRequest assignAmbulanceRequest = new OneTimeWorkRequest.Builder(AssignAmbulanceWorker.class)
                    .setInputData(new Data.Builder()
                            .putString("ambulance_number", ambulance.getVehicleNumber())
                            .putString("driver_mail", driver.getEmail())
                            .build())
                    .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build();

            WorkManager.getInstance(requireContext())
                    .enqueue(assignAmbulanceRequest);

            WorkManager.getInstance(requireContext())
                    .getWorkInfoByIdLiveData(assignAmbulanceRequest.getId())
                    .observe(getViewLifecycleOwner(), workInfo -> {
                        if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                            owner.assignAmbulanceToEmployee(ambulance.getVehicleNumber(), driver.getDriverId());
                            dialog.dismiss();
                        }
                        else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                            dialog.dismiss();
                            Toast.makeText(requireActivity(), workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    });
        };

        List<EmployeeDriver> availableEmployees =
                owner.getEmployees().getValue()
                        .stream()
                        .filter(driver -> driver.getAssignedAmbulanceNumber() == null || driver.getAssignedAmbulanceNumber().equals("None"))
                        .collect(Collectors.toList());

        DriverSearchFragment dialogFragment = new DriverSearchFragment(
                availableEmployees, searchDialogListener
        );

        return dialogFragment;
    }
}