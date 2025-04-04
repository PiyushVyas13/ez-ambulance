package com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
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
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.worker.AddDriverWorker;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.viewmodel.DriverRegistrationViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentAddDriverBinding;
import com.swasthavyas.emergencyllp.util.AppConstants;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.DriverRegistrationStep;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;


public class AddDriverFragment extends Fragment {


    OwnerViewModel ownerViewModel;
    DriverRegistrationViewModel driverRegistrationViewModel;

    FragmentAddDriverBinding viewBinding;
    AtomicReference<DriverRegistrationStep> currentStep;


    public AddDriverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentAddDriverBinding.inflate(getLayoutInflater());
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);



        viewBinding.addButton.setOnClickListener(v -> {
            Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.driver_registration_container).getChildFragmentManager().getPrimaryNavigationFragment();
            if(currentStep.get().equals(DriverRegistrationStep.PERSONAL_DETAILS)) {

                if(currentFragment instanceof DriverPersonalDetailsFragment) {

                    if(((DriverPersonalDetailsFragment) currentFragment).validateData()) {
                        Log.d(AppConstants.TAG, "primaryNavigationFragment: " + ( (DriverPersonalDetailsFragment) currentFragment).collectData());

                        driverRegistrationViewModel.setRegistrationData(DriverRegistrationStep.PERSONAL_DETAILS, ((DriverPersonalDetailsFragment) currentFragment).collectData());

                        NavHostFragment.findNavController(viewBinding.driverRegistrationContainer.getFragment()).navigate(R.id.action_driverPersonalDetailsFragment_to_driverAmbulanceDetailsFragment);
                        driverRegistrationViewModel.setRegistrationStep(DriverRegistrationStep.AMBULANCE_DETAILS);
                    }

                }

            }
            else {

                if(currentFragment instanceof DriverAmbulanceDetailsFragment) {
                    if(((DriverAmbulanceDetailsFragment) currentFragment).validateData()) {
                        Log.d(AppConstants.TAG, "primaryNavigationFragment (2): " + ( (DriverAmbulanceDetailsFragment) currentFragment).collectData());

                        driverRegistrationViewModel.setRegistrationData(DriverRegistrationStep.AMBULANCE_DETAILS, ((DriverAmbulanceDetailsFragment) currentFragment).collectData());

                        Toast.makeText(requireActivity(), "Add Driver", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });

        viewBinding.cancelButton.setOnClickListener(v -> {
            if(currentStep.get().equals(DriverRegistrationStep.PERSONAL_DETAILS)) {

                Navigation.findNavController(v).navigate(R.id.ownerHomeFragment, null, new NavOptions.Builder().setEnterAnim(android.R.anim.slide_in_left).setExitAnim(android.R.anim.fade_out).build());
            }
            else {
                driverRegistrationViewModel.setRegistrationStep(DriverRegistrationStep.PERSONAL_DETAILS);
                NavHostFragment.findNavController(viewBinding.driverRegistrationContainer.getFragment()).popBackStack();
            }
        });


        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        NavController navController = NavHostFragment.findNavController(viewBinding.driverRegistrationContainer.getFragment());

        driverRegistrationViewModel = new ViewModelProvider(navController.getBackStackEntry(R.id.driver_registration_graph)).get(DriverRegistrationViewModel.class);

        currentStep = new AtomicReference<>(driverRegistrationViewModel.getRegistrationStep().getValue());

        driverRegistrationViewModel.getRegistrationStep().observe(getViewLifecycleOwner(), driverRegistrationStep -> {
            currentStep.set(driverRegistrationStep);
            if(driverRegistrationStep.equals(DriverRegistrationStep.PERSONAL_DETAILS)) {
                viewBinding.addButton.setText(getString(R.string.text_next));
                viewBinding.cancelButton.setText(getString(R.string.cancel));


            } else if(driverRegistrationStep.equals(DriverRegistrationStep.AMBULANCE_DETAILS)) {
                viewBinding.addButton.setText("Add");
                viewBinding.cancelButton.setText("Back");
            }
        });

        driverRegistrationViewModel.getRegistrationData().observe(getViewLifecycleOwner(), registrationDataMap -> {

            if(registrationDataMap != null) {

                if(registrationDataMap.containsKey(DriverRegistrationStep.PERSONAL_DETAILS) && registrationDataMap.containsKey(DriverRegistrationStep.AMBULANCE_DETAILS)) {
                    Toast.makeText(requireActivity(), "Ready for registration", Toast.LENGTH_SHORT).show();

                    Owner owner = ownerViewModel.getOwner().getValue();

                    if(owner == null) {
                        Toast.makeText(requireContext(), "Unauthorized", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Data personalData = Objects.requireNonNull(registrationDataMap.get(DriverRegistrationStep.PERSONAL_DETAILS));
                    Data ambulanceData = Objects.requireNonNull(registrationDataMap.get(DriverRegistrationStep.AMBULANCE_DETAILS));


                    OneTimeWorkRequest addDriverRequest = new OneTimeWorkRequest.Builder(AddDriverWorker.class)
                            .setInputData(new Data.Builder()
                                    .putInt("age", personalData.getInt("age", -1))
                                    .putString("name", personalData.getString("name"))
                                    .putString("phone_number", "+91" + personalData.getString("phone_number"))
                                    .putString("email", personalData.getString("email"))
                                    .putString("owner_uid", owner.getUserId())
                                    .putString("aadhaar_number", ambulanceData.getString("aadhaar_number"))
                                    .putString("assigned_ambulance_number", ambulanceData.getString("assigned_ambulance_number"))
                                    .putString("aadhaarUriString", ambulanceData.getString("aadhaarUriString"))
                                    .putString("licenceUriString", ambulanceData.getString("licenceUriString"))
                                    .putString("owner_id", owner.getId())
                                    .build())
                            .build();

                    WorkManager.getInstance(requireContext())
                            .enqueue(addDriverRequest);

                    WorkManager.getInstance(requireContext())
                            .getWorkInfoByIdLiveData(addDriverRequest.getId())
                            .observe(getViewLifecycleOwner(), workInfo -> {
                                if (workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                                    Toast.makeText(requireActivity(), "Driver Added Successfully", Toast.LENGTH_SHORT).show();

                                    owner.addEmployee(EmployeeDriver.createFromMap(workInfo.getOutputData().getKeyValueMap()));

                                    viewBinding.addDriverProgressbar.setVisibility(View.GONE);

                                    Navigation.findNavController(viewBinding.getRoot()).navigate(R.id.ownerHomeFragment, null, new NavOptions.Builder().setEnterAnim(android.R.anim.slide_in_left).setExitAnim(android.R.anim.fade_out).build());

                                } else if (workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                    Toast.makeText(requireContext(), workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();

                                    viewBinding.addDriverProgressbar.setVisibility(View.GONE);
                                }

                            });

                }
            }

        });

    }
}