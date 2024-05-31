package com.swasthavyas.emergencyllp.component.registration.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.registration.viewmodel.RegistrationViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentRoleSelectBinding;
import com.swasthavyas.emergencyllp.util.types.UserRole;


public class RoleSelectFragment extends Fragment {
    FragmentRoleSelectBinding viewBinding;
    RegistrationViewModel viewModel;




    public RoleSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        viewBinding = FragmentRoleSelectBinding.inflate(getLayoutInflater());
//        viewBindings = FragmentRoleSelectBinding.inflate(getLayoutInflater());


        // Inflate the layout for this fragment

        viewModel = new ViewModelProvider(requireActivity()).get(RegistrationViewModel.class);

        viewBinding.ownerCard.setOnClickListener(v -> {
            viewBinding.driverCard.setChecked(false);
            viewBinding.ownerCard.setChecked(true);
        });

        viewBinding.driverCard.setOnClickListener(v -> {
            viewBinding.ownerCard.setChecked(false);
            viewBinding.driverCard.setChecked(true);
        });

        viewBinding.nextBtn.setOnClickListener(v ->  {

            if(viewBinding.ownerCard.isChecked()) {
                viewModel.setUserRole(UserRole.OWNER);
                Navigation.findNavController(v).navigate(R.id.action_roleSelectFragment_to_documentInputFragment);
            }
            else if(viewBinding.driverCard.isChecked()) {
                viewModel.setUserRole(UserRole.DRIVER);
                Navigation.findNavController(v).navigate(R.id.action_roleSelectFragment_to_addAmbulanceFragment);
            }

        });

        return viewBinding.getRoot();
    }
}