package com.swasthavyas.emergencyllp.component.auth.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swasthavyas.emergencyllp.component.auth.domain.model.UserInfo;
import com.swasthavyas.emergencyllp.component.auth.viewmodel.AuthViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentOtpVerificationBinding;
import com.swasthavyas.emergencyllp.util.AppConstants;


public class OTPVerificationFragment extends Fragment {

    FragmentOtpVerificationBinding viewBinding;

    public OTPVerificationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        viewBinding = FragmentOtpVerificationBinding.inflate(getLayoutInflater());

        viewBinding.verifyOtpBtn.setOnClickListener(v -> {
            verifyOtp();
        });

        return viewBinding.getRoot();
    }

    private void verifyOtp() {
        // TODO: Implement OTP verification logic

        if(getArguments() != null) {
            String verificationId  = getArguments().getString("verificationId");
            String email = getArguments().getString("email");
            String password = getArguments().getString("password");
            String phone = getArguments().getString("phone");
            String name = getArguments().getString("name");


            if(verificationId == null || email == null || password == null || phone == null || name == null) {
                Log.d("<MYAPP>", "verifyOtp: one of the arguments is null");
                return;
            }

            String otp = viewBinding.otpView.getOtp();

            if(otp.length() < 6) {
                Log.d(AppConstants.TAG, "verifyOtp: otp length is less than 6");
                return;
            }

            AuthViewModel authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

            UserInfo userInfo = new UserInfo();

            userInfo.setEmail(email);
            userInfo.setPassword(password);
            userInfo.setVerificationId(verificationId);
            userInfo.setPhone(phone);
            userInfo.setOtp(otp);
            userInfo.setName(name);

            authViewModel.setUser(userInfo);

        }
        else {
            Log.d(AppConstants.TAG, "verifyOtp: arguments are empty");
        }
    }
}