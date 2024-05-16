package com.swasthavyas.emergencyllp.component.dashboard.owner.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.swasthavyas.emergencyllp.AuthActivity;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.databinding.FragmentProfileBinding;


public class ProfileFragment extends Fragment {
    FragmentProfileBinding viewBinding;

    public ProfileFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        viewBinding = FragmentProfileBinding.inflate(getLayoutInflater());

        viewBinding.signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return viewBinding.getRoot();
    }
}