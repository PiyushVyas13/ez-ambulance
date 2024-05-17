package com.swasthavyas.emergencyllp.component.dashboard.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.swasthavyas.emergencyllp.AuthActivity;
import com.swasthavyas.emergencyllp.databinding.FragmentDriverDashboardBinding;
import com.swasthavyas.emergencyllp.util.types.UserRole;


public class DriverDashboardFragment extends Fragment {

    FragmentDriverDashboardBinding viewBinding;

    private UserRole userRole;
    private boolean isVerified;


    public DriverDashboardFragment() {
        // Required empty public constructor
    }

    public static DriverDashboardFragment newInstance(UserRole userRole) {
        DriverDashboardFragment fragment = new DriverDashboardFragment();
        Bundle args = new Bundle();
        args.putString("role", userRole.name());
        fragment.setArguments(args);
        return fragment;
    }

    public static DriverDashboardFragment newInstance(UserRole role, boolean isVerified) {
        DriverDashboardFragment fragment = new DriverDashboardFragment();
        Bundle args = new Bundle();
        args.putString("role", role.name());
        args.putBoolean("isVerified", isVerified);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.userRole = UserRole.valueOf(getArguments().getString("role"));
            this.isVerified = getArguments().getBoolean("isVerified", true);
        }
        else {
            this.userRole = UserRole.UNASSIGNED;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentDriverDashboardBinding.inflate(getLayoutInflater());

        // Inflate the layout for this fragment

        viewBinding.signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(requireActivity(), AuthActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });


        return viewBinding.getRoot();
    }
}