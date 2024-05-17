package com.swasthavyas.emergencyllp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseUser;
import com.swasthavyas.emergencyllp.component.auth.viewmodel.AuthViewModel;
import com.swasthavyas.emergencyllp.component.registration.viewmodel.RegistrationViewModel;
import com.swasthavyas.emergencyllp.component.registration.worker.RoleAssignmentWorker;
import com.swasthavyas.emergencyllp.component.registration.worker.UserRegistrationWorker;
import com.swasthavyas.emergencyllp.databinding.ActivityRegistrationBinding;
import com.swasthavyas.emergencyllp.util.types.UserRole;

import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    RegistrationViewModel viewModel;
    AuthViewModel authViewModel;
    ActivityRegistrationBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        viewBinding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        viewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);


        viewModel.getAadhaarNumberLiveData().observe(this, aadhaarNumber -> {
            if( viewModel.getUserRole().getValue() != null && viewModel.getUserRole().getValue() != UserRole.UNASSIGNED && aadhaarNumber != null && aadhaarNumber.length() == 12) {
                // chain two work requests
                // 1. create an entry in the user role collection
                // 2. create an entry in the driver/owner collection

                FirebaseUser currentUser = authViewModel.getCurrentUser().getValue();

                if(currentUser == null) {
                    Toast.makeText(this, "Unauthorized", Toast.LENGTH_SHORT).show();
                    return;
                }
                String roleString = String.valueOf(viewModel.getUserRole().getValue()).toLowerCase();
                String userId = currentUser.getUid();

                OneTimeWorkRequest roleAssignmentRequest = new OneTimeWorkRequest.Builder(RoleAssignmentWorker.class)
                        .setInputData(new Data.Builder()
                                .putString("role", roleString)
                                .putString("userId", userId)
                                .build())
                        .build();

                OneTimeWorkRequest.Builder registrationRequestBuilder = new OneTimeWorkRequest.Builder(UserRegistrationWorker.class);

                if(viewModel.getUserRole().getValue().equals(UserRole.DRIVER)) {
                    Map<String, Object> driverAmbulance = viewModel.getDriverAmbulance().getValue();

                    if(driverAmbulance.isEmpty()) {
                        Toast.makeText(this, "Driver Ambulance is null.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    registrationRequestBuilder
                            .setInputData(new Data.Builder()
                                    .putString("aadhaar_number", aadhaarNumber)
                                    .putString("role", roleString)
                                    .putString("userId", userId)
                                    .putAll(driverAmbulance)
                                    .build());

                }
                else if(viewModel.getUserRole().getValue().equals(UserRole.OWNER)) {
                    registrationRequestBuilder
                            .setInputData(new Data.Builder()
                                    .putString("aadhaar_number", aadhaarNumber)
                                    .putString("role", roleString)
                                    .putString("userId", userId)
                                    .build());

                }

                OneTimeWorkRequest registrationRequest = registrationRequestBuilder.build();

                WorkManager.getInstance(getApplicationContext())
                        .beginWith(roleAssignmentRequest)
                        .then(registrationRequest)
                        .enqueue();

                WorkManager.getInstance(getApplicationContext())
                        .getWorkInfoByIdLiveData(registrationRequest.getId())
                        .observe(this, workInfo -> {
                            if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                                Toast.makeText(this, "Registration Complete!", Toast.LENGTH_SHORT).show();
                                viewBinding.registrationProgressbar.setVisibility(View.GONE);

                                NavOptions options = new NavOptions.Builder()
                                        .setPopUpTo(R.id.registrationSuccessFragment, true)
                                                .setEnterAnim(R.anim.slide_in_right)
                                                        .setExitAnim(R.anim.slide_out_left)
                                                                .build();


                                NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.registration_nav_container)).navigate(R.id.registrationSuccessFragment, null, options);
                            }
                            else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                Toast.makeText(this, workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                                viewBinding.registrationProgressbar.setVisibility(View.GONE);
                                NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.registration_nav_container)).popBackStack();
                            } else if (workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                                viewBinding.registrationProgressbar.setVisibility(View.VISIBLE);
                            }

                        });

            }
        });
    }
}