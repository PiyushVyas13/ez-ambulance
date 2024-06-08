package com.swasthavyas.emergencyllp.component.dashboard.owner.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.adapter.DisplayModeAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.worker.DashboardViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentHomeBinding;

import java.util.concurrent.atomic.AtomicReference;


public class HomeFragment extends Fragment {

    FragmentHomeBinding viewBinding;
    OwnerViewModel ownerViewModel;
    DashboardViewModel dashboardViewModel;

    public static final String MODE_AMBULANCE = "ambulance";
    public static final String MODE_DRIVER = "driver";


    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint({"DefaultLocale", "NotifyDataSetChanged"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);

        ownerViewModel.getOwner().observe(getViewLifecycleOwner(), owner -> {
            if(owner != null) {
                AtomicReference<String> currentMode = new AtomicReference<>(MODE_AMBULANCE);

                viewBinding.entityCountText.setText(String.format("You have %d ambulances registered", owner.getAmbulances().getValue().size()));


                viewBinding.viewpager2.setAdapter(new DisplayModeAdapter(this));

                new TabLayoutMediator(viewBinding.tablayout, viewBinding.viewpager2, (tab, i) -> {
                    switch (i) {
                        case 0:
                            tab.setText("Ambulance");
                            break;
                        case 1:
                            tab.setText("Driver");
                            break;
                    }
                }).attach();

                viewBinding.tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if(tab.getText().toString().equals("Ambulance")) {
                            currentMode.set(MODE_AMBULANCE);
                            viewBinding.entityCountText.setText(String.format("You have %d ambulances registered", owner.getAmbulances().getValue().size()));
                        }
                        else if(tab.getText().toString().equals("Driver")) {
                            currentMode.set(MODE_DRIVER);
                            viewBinding.entityCountText.setText(String.format("You have %d drivers registered", owner.getEmployees().getValue().size()));
                        }
                    }
                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}

                });

                viewBinding.addEntityBtn.setOnClickListener(v -> {
                    switch (currentMode.get()) {
                        case MODE_AMBULANCE:
                            Toast.makeText(requireActivity(), "Ambulance Mode", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(viewBinding.getRoot()).navigate(R.id.ownerAddAmbulanceFragment, null, new NavOptions.Builder().setEnterAnim(R.anim.slide_in_right).setExitAnim(android.R.anim.fade_out).build());
                            break;
                        case MODE_DRIVER:
                            Toast.makeText(requireActivity(), "Driver Mode", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(viewBinding.getRoot()).navigate(R.id.addDriverFragment, null, new NavOptions.Builder().setEnterAnim(R.anim.slide_in_right).setExitAnim(android.R.anim.fade_out).build());
                            break;
                    }
                });
            }
        });




        return viewBinding.getRoot();
    }
}