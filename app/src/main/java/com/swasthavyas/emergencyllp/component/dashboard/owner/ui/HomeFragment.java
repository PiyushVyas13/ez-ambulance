package com.swasthavyas.emergencyllp.component.dashboard.owner.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.adapter.AmbulanceAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.adapter.EmployeeAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.DashboardViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentHomeBinding;


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

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        viewBinding.displayModeToggle.setOnClickListener(v -> {
            if(dashboardViewModel.getDisplayMode().getValue().equals(MODE_AMBULANCE)) {
                dashboardViewModel.setDisplayMode(MODE_DRIVER);
            }
            else {
                dashboardViewModel.setDisplayMode(MODE_AMBULANCE);
            }
        });

        ownerViewModel.getOwner().observe(getViewLifecycleOwner(), owner -> {
            if(owner != null) {

                dashboardViewModel.getDisplayMode().observe(getViewLifecycleOwner(), displayMode -> {
                    Toast.makeText(requireContext(), displayMode, Toast.LENGTH_SHORT).show();
                    if(displayMode.equals(MODE_AMBULANCE)) {

                        viewBinding.countTextView.setText("You have - ambulances registered");
                        viewBinding.displayModeText.setText("Driver");
                        viewBinding.recyclerView.setVisibility(View.GONE);
                        viewBinding.addBtn.setVisibility(View.GONE);

                        owner.getAmbulances().observe(getViewLifecycleOwner(), ambulances -> {
                            if(ambulances != null) {
                                if (ambulances.isEmpty()) {
                                    viewBinding.recyclerView.setVisibility(View.GONE);
                                    viewBinding.addBtn.setVisibility(View.VISIBLE);

                                    viewBinding.addBtn.setText("TAP TO ADD AMBULANCE");
                                    viewBinding.addBtn.setOnClickListener(v -> {
                                        Navigation.findNavController(v).navigate(R.id.ownerManageAmbulanceFragment, null, new NavOptions.Builder().setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left).build());
                                    });
                                }
                                else {
                                    viewBinding.addBtn.setVisibility(View.GONE);
                                    viewBinding.recyclerView.setVisibility(View.VISIBLE);

                                    viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    viewBinding.recyclerView.setAdapter(new AmbulanceAdapter(ambulances));
                                }

                                viewBinding.countTextView.setText(String.format("You have %d ambulance(s) registered", ambulances.size()));
                            }

                        });
                    }
                    else if(displayMode.equals(MODE_DRIVER)) {
                        viewBinding.countTextView.setText("You have - drivers registered");
                        viewBinding.displayModeText.setText("Ambulance");
                        viewBinding.recyclerView.setVisibility(View.GONE);
                        viewBinding.addBtn.setVisibility(View.GONE);


                        //TODO: start observing for changes in employee (observer goes here).
                        owner.getEmployees().observe(getViewLifecycleOwner(), employees -> {
                            if(employees != null) {
                                if (employees.isEmpty()) {
                                    viewBinding.recyclerView.setVisibility(View.GONE);
                                    viewBinding.addBtn.setVisibility(View.VISIBLE);

                                    viewBinding.addBtn.setText("TAP TO ADD DRIVER");
                                    viewBinding.addBtn.setOnClickListener(v -> {
                                        Navigation.findNavController(v).navigate(R.id.addDriverFragment, null, new NavOptions.Builder().setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left).build());
                                    });
                                }
                                else {
                                    viewBinding.addBtn.setVisibility(View.GONE);
                                    viewBinding.recyclerView.setVisibility(View.VISIBLE);
                                    viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                    viewBinding.recyclerView.setAdapter(new EmployeeAdapter(employees));
                                }

                                viewBinding.countTextView.setText(String.format("You have %d driver(s) registered", employees.size()));
                            }

                        });
                    }



                });



            }

        });


        return viewBinding.getRoot();
    }
}