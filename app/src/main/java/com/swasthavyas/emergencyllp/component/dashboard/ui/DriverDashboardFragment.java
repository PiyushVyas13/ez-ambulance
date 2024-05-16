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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriverDashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverDashboardFragment extends Fragment {

    FragmentDriverDashboardBinding viewBinding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DriverDashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DriverDashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DriverDashboardFragment newInstance(String param1, String param2) {
        DriverDashboardFragment fragment = new DriverDashboardFragment();
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