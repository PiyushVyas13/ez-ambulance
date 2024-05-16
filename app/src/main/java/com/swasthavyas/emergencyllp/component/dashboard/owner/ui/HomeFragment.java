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

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.adapter.AmbulanceAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {

    FragmentHomeBinding viewBinding;
    OwnerViewModel ownerViewModel;

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

        ownerViewModel.getOwner().observe(getViewLifecycleOwner(), owner -> {
            if(owner != null) {
                owner.getAmbulances().observe(getViewLifecycleOwner(), ambulances -> {
                    if(ambulances != null) {
                        if (ambulances.isEmpty()) {
                            viewBinding.recyclerView.setVisibility(View.GONE);
                            viewBinding.btnAddAmbulance.setVisibility(View.VISIBLE);

                            viewBinding.btnAddAmbulance.setOnClickListener(v -> {
                                Navigation.findNavController(v).navigate(R.id.ownerManageAmbulanceFragment, null, new NavOptions.Builder().setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left).build());
                            });
                        }
                        else {
                            viewBinding.btnAddAmbulance.setVisibility(View.GONE);
                            viewBinding.recyclerView.setVisibility(View.VISIBLE);

                            viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            viewBinding.recyclerView.setAdapter(new AmbulanceAdapter(ambulances));
                        }

                        viewBinding.ambulanceCountTextView.setText(String.format("You have %d ambulance(s) registered", ambulances.size()));
                    }

                });

            }

        });


        return viewBinding.getRoot();
    }
}