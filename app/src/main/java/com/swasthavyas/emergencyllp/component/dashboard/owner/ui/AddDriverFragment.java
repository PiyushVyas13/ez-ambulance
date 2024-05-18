package com.swasthavyas.emergencyllp.component.dashboard.owner.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.worker.AddDriverWorker;
import com.swasthavyas.emergencyllp.component.dashboard.owner.worker.CreateDriverWorker;
import com.swasthavyas.emergencyllp.databinding.FragmentAddDriverBinding;

import java.util.Map;


public class AddDriverFragment extends Fragment {

    OwnerViewModel ownerViewModel;

    FragmentAddDriverBinding viewBinding;

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

        viewBinding.addDriverButton.setOnClickListener(v -> {

            if(viewBinding.driverName.getText().toString().isEmpty() || viewBinding.driverNumber.getText().toString().isEmpty() || viewBinding.driverAge.getText().toString().isEmpty() || viewBinding.driverEmail.getText().toString().isEmpty()) {
                Toast.makeText(requireActivity(), "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(viewBinding.driverNumber.length() < 10) {
                Toast.makeText(requireActivity(), "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();
                return;
            }

            Owner owner = ownerViewModel.getOwner().getValue();

            if(owner == null) {
                Toast.makeText(requireContext(), "Unauthorized",Toast.LENGTH_SHORT).show();
                return;
            }



            OneTimeWorkRequest createDriverRequest = new OneTimeWorkRequest.Builder(CreateDriverWorker.class)
                    .setInputData(new Data.Builder()
                            .putString("name", viewBinding.driverName.getText().toString())
                            .putString("phone_number", "+91" + viewBinding.driverNumber.getText().toString())
                            .putString("email", viewBinding.driverEmail.getText().toString())
                            .build())
                    .build();

            OneTimeWorkRequest addDriverRequest = new OneTimeWorkRequest.Builder(AddDriverWorker.class)
                    .setInputData(new Data.Builder()
                            .putInt("age", Integer.parseInt(viewBinding.driverAge.getText().toString()))
                            .putString("owner_id", owner.getId())
                            .build())
                    .build();


            WorkManager.getInstance(requireContext())
                    .beginWith(createDriverRequest)
                    .then(addDriverRequest)
                    .enqueue();

            WorkManager.getInstance(requireContext())
                            .getWorkInfoByIdLiveData(createDriverRequest.getId())
                                    .observe(getViewLifecycleOwner(), workInfo -> {
                                        if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                            Toast.makeText(requireContext(), workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                                        }
                                        else if(workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                                            viewBinding.addDriverProgressbar.setVisibility(View.VISIBLE);
                                        }
                                    });


            WorkManager.getInstance(requireContext())
                    .getWorkInfoByIdLiveData(addDriverRequest.getId())
                    .observe(getViewLifecycleOwner(), workInfo -> {
                        if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                            Toast.makeText(requireActivity(), "Driver Added Successfully", Toast.LENGTH_SHORT).show();


                           owner.addEmployee(EmployeeDriver.createFromMap(workInfo.getOutputData().getKeyValueMap()));


                            viewBinding.driverName.setText("");
                            viewBinding.driverNumber.setText("");
                            viewBinding.driverAge.setText("");
                            viewBinding.addDriverProgressbar.setVisibility(View.GONE);

                            Navigation.findNavController(v).navigate(R.id.ownerHomeFragment, null, new NavOptions.Builder().setEnterAnim(android.R.anim.slide_in_left).setExitAnim(android.R.anim.slide_out_right).build());
                        }
                        else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                            Toast.makeText(requireContext(), workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                            viewBinding.driverName.setText("");
                            viewBinding.driverNumber.setText("");
                            viewBinding.driverAge.setText("");
                            viewBinding.addDriverProgressbar.setVisibility(View.GONE);
                        }

                    });


        });

        return viewBinding.getRoot();
    }
}