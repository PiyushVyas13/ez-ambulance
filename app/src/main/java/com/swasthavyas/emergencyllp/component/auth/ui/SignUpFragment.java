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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {
    FragmentSignUpBinding viewBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

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