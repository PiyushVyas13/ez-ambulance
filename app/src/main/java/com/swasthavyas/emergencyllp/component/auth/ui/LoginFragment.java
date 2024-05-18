package com.swasthavyas.emergencyllp.component.auth.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.auth.viewmodel.AuthViewModel;
import com.swasthavyas.emergencyllp.component.auth.worker.SignInWorker;
import com.swasthavyas.emergencyllp.databinding.FragmentLoginBinding;


public class LoginFragment extends Fragment {

    FragmentLoginBinding viewBinding;
    AuthViewModel authViewModel;
    private FirebaseUser currentUser;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentLoginBinding.inflate(getLayoutInflater());
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        authViewModel.getCurrentUser().observe(requireActivity(), firebaseUser -> {
            this.currentUser = firebaseUser;
        });

        Toast.makeText(requireContext(), "In LOgin Fragment", Toast.LENGTH_SHORT).show();
        viewBinding.signup.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_signUpFragment);
        });

        viewBinding.resetPassword.setOnClickListener(v -> {
            if(this.currentUser != null) {
                Toast.makeText(requireActivity(), "You are already logged in!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(viewBinding.email.getText().toString().isEmpty()) {
                Toast.makeText(requireActivity(), "Please provide your registered email!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(viewBinding.email.getText().toString())
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Snackbar.make(v, "Email sent! Check your email for password reset link.", BaseTransientBottomBar.LENGTH_LONG).show();
                        }
                        else {
                            Snackbar.make(v, "Cannot send email", BaseTransientBottomBar.LENGTH_LONG).show();
                            Log.e("MYAPP", "onCreateView: password reset mail", task.getException());
                        }
                    });
        });

        viewBinding.loginBtn.setOnClickListener(v -> {
            if(viewBinding.email.getText().toString().isEmpty() || viewBinding.password.getText().toString().isEmpty()) {
                Toast.makeText(requireActivity(), "email and password are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            WorkRequest emailSignInRequest = new OneTimeWorkRequest.Builder(SignInWorker.class)
                    .setInputData(new Data.Builder()
                            .putString("mode", "email")
                            .putString("email", viewBinding.email.getText().toString())
                            .putString("password", viewBinding.password.getText().toString())
                            .build())
                    .build();

            WorkManager.getInstance(requireContext()).enqueue(emailSignInRequest);
            viewBinding.loginBtn.setEnabled(false);

            WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(emailSignInRequest.getId())
                    .observe(getViewLifecycleOwner(), workInfo -> {
                        if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                            viewBinding.emailLoginProgressbar.setVisibility(View.GONE);
                            Navigation.findNavController(viewBinding.getRoot()).navigate(R.id.action_loginFragment_to_mainActivity);
                            viewBinding.loginBtn.setEnabled(true);
                        } else if (workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                            // TODO: get exception class name and set appropriate message
                            String message = workInfo.getOutputData().getString("message");
                            Toast.makeText(requireActivity(), "Sign in Failed: " + message, Toast.LENGTH_SHORT).show();
                            viewBinding.emailLoginProgressbar.setVisibility(View.GONE);
                            viewBinding.loginBtn.setEnabled(true);

                        }
                        else if(workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                            // Start the progressbar
                            viewBinding.emailLoginProgressbar.setVisibility(View.VISIBLE);
                        }
                    });
        });

        // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(this.currentUser != null) {
            Toast.makeText(requireContext(), "User is  available", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(requireContext(), "User is not available", Toast.LENGTH_SHORT).show();
        }


        return viewBinding.getRoot();
    }
}