package com.swasthavyas.emergencyllp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.swasthavyas.emergencyllp.component.auth.viewmodel.AuthViewModel;
import com.swasthavyas.emergencyllp.component.auth.worker.AnonymousSignInWorker;
import com.swasthavyas.emergencyllp.component.auth.worker.EmailAuthLinkWorker;
import com.swasthavyas.emergencyllp.component.auth.worker.PhoneAuthLinkWorker;
import com.swasthavyas.emergencyllp.databinding.ActivityAuthBinding;

import java.util.Map;

public class AuthActivity extends AppCompatActivity {

    NavController navController;
    ActivityAuthBinding viewBinding;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityAuthBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(viewBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        navController = NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_auth_container));

        int startDestination = navController.getGraph().getStartDestination();
        NavOptions options = new NavOptions.Builder().setPopUpTo(startDestination, true)
                .setExitAnim(android.R.anim.fade_out).setEnterAnim(android.R.anim.fade_in).build();

        authViewModel.getCurrentUser().observe(this, firebaseUser -> {
            this.currentUser = firebaseUser;
        });

        authViewModel.getUserInfoLiveData().observe(this, user -> {

            if(user != null) {
                if(user.getEmail() != null && user.getPassword() != null && user.getPhone() != null && user.getOtp() != null && user.getVerificationId() != null && user.getName() != null) {
                    OneTimeWorkRequest anonUserRequest = new OneTimeWorkRequest.Builder(AnonymousSignInWorker.class).build();

                    OneTimeWorkRequest phoneAuthLinkRequest = new OneTimeWorkRequest.Builder(PhoneAuthLinkWorker.class)
                            .setInputData(new Data.Builder()
                                    .putString("verificationId", user.getVerificationId())
                                    .putString("otp", user.getOtp())
                                    .build())
                            .build();

                    OneTimeWorkRequest emailAuthLinkRequest = new OneTimeWorkRequest.Builder(EmailAuthLinkWorker.class)
                            .setInputData(new Data.Builder()
                                    .putString("email", user.getEmail())
                                    .putString("password", user.getPassword())
                                    .build())
                            .build();

                    WorkManager.getInstance(getApplicationContext())
                            .beginWith(anonUserRequest)
                            .then(phoneAuthLinkRequest)
                            .then(emailAuthLinkRequest)
                            .enqueue();

                    WorkManager.getInstance(getApplicationContext())
                            .getWorkInfoByIdLiveData(anonUserRequest.getId())
                            .observe(this, workInfo -> {

                                if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {


                                    Toast.makeText(this, workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                                    if(this.currentUser != null) {
                                        this.currentUser.delete()
                                                .addOnCompleteListener(task ->  {
                                                    if(task.isSuccessful()) {
                                                        Log.d("MYAPP", "deleteAnonUser: user deleted");
                                                    }
                                                    else {
                                                        Log.d("MYAPP", "deleteAnonUser: cannot delete user - [" + task.getException().getMessage() + "]");
                                                    }
                                                });
                                    }

//                                NavController navController = NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_auth_container));
                                    viewBinding.loginProgressbar.setVisibility(View.GONE);
                                    navController.navigate(startDestination, null, options);
                                }
                                else if (workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                                    viewBinding.loginProgressbar.setVisibility(View.VISIBLE);
                                }
                            });


                    WorkManager.getInstance(getApplicationContext())
                            .getWorkInfoByIdLiveData(phoneAuthLinkRequest.getId())
                            .observe(this, workInfo -> {
                                if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                    Data message = workInfo.getOutputData();

                                    Map<String, Object> map = message.getKeyValueMap();

                                    if(!map.isEmpty()) {
                                        Log.d("MYAPP", "phoneAuthLinkFailed: output data is empty");
                                        Toast.makeText(this, workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                                        if(this.currentUser != null) {
                                            this.currentUser.delete()
                                                    .addOnCompleteListener(task ->  {
                                                        if(task.isSuccessful()) {
                                                            Log.d("MYAPP", "deleteAnonUser: user deleted");
                                                        }
                                                        else {
                                                            Log.d("MYAPP", "deleteAnonUser: cannot delete user - [" + task.getException().getMessage() + "]");
                                                        }
                                                    });
                                        }

                                    }


//                                NavController navController = NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.nav_auth_container));
                                    viewBinding.loginProgressbar.setVisibility(View.GONE);
                                    navController.navigate(startDestination, null, options);
                                }
                            });

                    WorkManager.getInstance(getApplicationContext())
                            .getWorkInfoByIdLiveData(emailAuthLinkRequest.getId())
                            .observe(this, workInfo -> {
                                if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                    Data message = workInfo.getOutputData();

                                    Map<String, Object> map = message.getKeyValueMap();

                                    if(!map.isEmpty()) {
                                        Log.d("MYAPP", "emailAuthLinkFailed: output data is empty");
                                        Toast.makeText(this, workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                                        if(this.currentUser != null) {
                                            this.currentUser.delete()
                                                    .addOnCompleteListener(task ->  {
                                                        if(task.isSuccessful()) {
                                                            Log.d("MYAPP", "deleteAnonUser: user deleted");
                                                        }
                                                        else {
                                                            Log.d("MYAPP", "deleteAnonUser: cannot delete user - [" + task.getException().getMessage() + "]");
                                                        }
                                                    });
                                        }
                                        viewBinding.loginProgressbar.setVisibility(View.GONE);
                                        navController.navigate(startDestination, null, options);
                                    }

                                }
                                else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();



                                    if(this.currentUser != null && this.currentUser.getDisplayName() == null) {
                                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(user.getName())
                                                .build();

                                        this.currentUser.updateProfile(request)
                                                .addOnCompleteListener(task -> {
                                                    if(task.isSuccessful()) {
                                                        Log.d("MYAPP", "addDisplayName: name updated");
                                                    }
                                                    else {
                                                        Log.d("MYAPP", "addDisplayName: cannot update name - [" + task.getException().getMessage() + "]");
                                                    }
                                                });
                                    }


                                    FirebaseAuth.getInstance().signOut();
                                    viewBinding.loginProgressbar.setVisibility(View.GONE);
                                    navController.navigate(startDestination, null, options);
                                }
                            });



                }

            }
        });
        Toast.makeText(this, "In auth activity", Toast.LENGTH_SHORT).show();
    }


}