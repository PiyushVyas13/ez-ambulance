package com.swasthavyas.emergencyllp.component.auth.ui;

import android.os.Bundle;



import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.databinding.FragmentSignUpBinding;


public class SignUpFragment extends Fragment {
    FragmentSignUpBinding viewBinding;

    public SignUpFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentSignUpBinding.inflate(getLayoutInflater());
        // Inflate the layout for this fragment
        viewBinding.signup.setOnClickListener(v -> {

            if(viewBinding.fullName.getText().toString().isEmpty() || viewBinding.email.getText().toString().isEmpty() || viewBinding.password.getText().toString().isEmpty() || viewBinding.confirmPassword.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(viewBinding.password.getText().length() < 6) {
                Toast.makeText(requireContext(), "Password should be greater than 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(user != null && user.getEmail() != null && user.getEmail().equals(viewBinding.email.getText().toString())) {
                Toast.makeText(requireContext(), "User with this email already exists.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!viewBinding.password.getText().toString().equals(viewBinding.confirmPassword.getText().toString())) {
                Toast.makeText(requireContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("name", viewBinding.fullName.getText().toString());
            bundle.putString("email", viewBinding.email.getText().toString());
            bundle.putString("password", viewBinding.password.getText().toString());

             Navigation.findNavController(v).navigate(R.id.action_signUpFragment_to_sendOTPFragment, bundle);
        });

        viewBinding.login.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });
        return viewBinding.getRoot();
    }
}