package com.swasthavyas.emergencyllp.component.dashboard.driver.ui;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.swasthavyas.emergencyllp.AuthActivity;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.driver.viewmodel.EmployeeViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.driver.worker.DriverProfileUpdateWorker;
import com.swasthavyas.emergencyllp.component.dashboard.ui.DriverDashboardFragment;
import com.swasthavyas.emergencyllp.component.trip.ui.TripActivity;
import com.swasthavyas.emergencyllp.databinding.FragmentProfileDriverBinding;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;
import com.swasthavyas.emergencyllp.util.service.LocationService;

import java.util.Locale;


public class ProfileFragment extends Fragment {

    FragmentProfileDriverBinding viewBinding;
    EmployeeViewModel employeeViewModel;

    ActivityResultLauncher<PickVisualMediaRequest> uploadProfilePicRequest;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uploadProfilePicRequest = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if(uri != null) {
                Drawable currentDrawable = viewBinding.profileImage.getDrawable();
                viewBinding.profileImage.setImageURI(uri);
                uploadProfileImage(currentDrawable, uri);
            }
        });
        employeeViewModel = new ViewModelProvider(requireActivity()).get(EmployeeViewModel.class);
    }



    private void uploadProfileImage(Drawable currentDrawable, Uri newProfilePicUri) {
        Data inputData = new Data.Builder()
                .putString("profile_uri_string", newProfilePicUri.toString())
                .build();

        OneTimeWorkRequest profileUploadRequest = new OneTimeWorkRequest.Builder(DriverProfileUpdateWorker.class)
                .setInputData(inputData)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build();

        WorkManager.getInstance(requireContext()).enqueue(profileUploadRequest);

        WorkManager.getInstance(requireContext())
                .getWorkInfoByIdLiveData(profileUploadRequest.getId())
                .observe(getViewLifecycleOwner(), workInfo -> {
                    if(workInfo.getState().isFinished() && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        Toast.makeText(requireContext(), "Profile Successfully updated!", Toast.LENGTH_SHORT).show();
                    } else if(workInfo.getState().isFinished() && workInfo.getState() == WorkInfo.State.FAILED){
                        Toast.makeText(requireContext(), "Something Went wrong!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "uploadProfileImage: " + workInfo.getOutputData().getString("message"));
                        viewBinding.profileImage.setImageDrawable(currentDrawable);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentProfileDriverBinding.inflate(getLayoutInflater());
        FirebaseDatabase database = FirebaseService.getInstance().getDatabaseInstance();



        employeeViewModel.getCurrentEmployee().observe(getViewLifecycleOwner(), employeeDriver -> {
            Log.d(TAG, "onCreateView: Profile fragment observer triggered");


            viewBinding.driverName.setText(employeeDriver.getName());
            viewBinding.driverMobile.setText(employeeDriver.getPhoneNumber());
            viewBinding.driverMail.setText(employeeDriver.getEmail());
            viewBinding.driverAge.setText(String.format(Locale.ENGLISH, "%d", employeeDriver.getAge()));
            viewBinding.driverAadhaar.setText(employeeDriver.getAadhaarNumber());

            viewBinding.changeProfileImage.setOnClickListener(v -> {
                uploadProfilePicRequest.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            });

            viewBinding.signOutBtn.setOnClickListener(v -> {
                DatabaseReference activeDriverReference = database
                        .getReference()
                        .getRoot()
                        .child("active_drivers")
                        .child(employeeDriver.getDriverId());

                activeDriverReference.removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        LocationService.stopService(requireContext(), employeeDriver.getEmail());
                                        FirebaseAuth.getInstance().signOut();
                                    }
                                });
            });
        });


        return viewBinding.getRoot();
    }
}