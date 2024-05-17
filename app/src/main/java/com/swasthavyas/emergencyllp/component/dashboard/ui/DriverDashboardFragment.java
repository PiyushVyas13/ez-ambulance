package com.swasthavyas.emergencyllp.component.dashboard.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NavigationRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.swasthavyas.emergencyllp.AuthActivity;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.databinding.FragmentDriverDashboardBinding;
import com.swasthavyas.emergencyllp.util.types.UserRole;


public class DriverDashboardFragment extends Fragment {

    FragmentDriverDashboardBinding viewBinding;

    private UserRole userRole;
    private boolean isVerified;
    NavController navController;


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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentDriverDashboardBinding.inflate(getLayoutInflater());
        navController = NavHostFragment.findNavController(getChildFragmentManager().findFragmentById(R.id.driver_bottom_nav_container));

        // Inflate the layout for this fragment

        NavigationBarView navigationBarView = viewBinding.bottomNavDriver;

        navigationBarView.setOnItemSelectedListener(menuItem -> {
            if(menuItem.getItemId() == R.id.navHome) {
                navigateToDestination(R.id.homeFragment);
            }
            else if(menuItem.getItemId() == R.id.navEarning) {
                navigateToDestination(R.id.earningFragment);
            }
            else if(menuItem.getItemId() == R.id.navSwapDriver) {
                navigateToDestination(R.id.swapDriverFragment);
            }
            else if(menuItem.getItemId() == R.id.navNotification) {
                navigateToDestination(R.id.notificationFragment);
            }
            else if(menuItem.getItemId() == R.id.navProfile) {
                navigateToDestination(R.id.profileFragment);
            }

            return true;
        });




        return viewBinding.getRoot();
    }

    private void navigateToDestination(@IdRes int destinationId) {
        int destinationTag = Integer.parseInt(navController.getGraph().findNode(destinationId).getLabel().toString());
        int currentDestinationTag = Integer.parseInt(navController.getCurrentDestination().getLabel().toString());

        int enterAnim = -1;
        int exitAnim = -1;

        if(destinationTag != currentDestinationTag) {
            if(destinationTag > currentDestinationTag) {
                enterAnim = R.anim.slide_in_right;
                exitAnim = R.anim.slide_out_left;
            }
            else {
                enterAnim = android.R.anim.slide_in_left;
                exitAnim = android.R.anim.slide_out_right;
            }

            navController.navigate(destinationId, null, new NavOptions.Builder().setEnterAnim(enterAnim).setExitAnim(exitAnim).build());
        }

    }
}