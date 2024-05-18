package com.swasthavyas.emergencyllp.component.dashboard.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.swasthavyas.emergencyllp.AuthActivity;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.auth.viewmodel.AuthViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Owner;
import com.swasthavyas.emergencyllp.component.dashboard.owner.ui.HomeFragment;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.DashboardViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.worker.FetchOwnerWorker;
import com.swasthavyas.emergencyllp.databinding.FragmentOwnerDashboardBinding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class OwnerDashboardFragment extends Fragment {

    FragmentOwnerDashboardBinding viewBinding;
    OwnerViewModel ownerViewModel;
    AuthViewModel authViewModel;
    DashboardViewModel dashboardViewModel;
    NavController navController;

    public OwnerDashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewBinding = FragmentOwnerDashboardBinding.inflate(getLayoutInflater());
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        FirebaseUser currentUser = authViewModel.getCurrentUser().getValue();

        if(currentUser == null) {
            Toast.makeText(requireActivity(), "Unauthorized", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }

        //ownerViewModel.fetchOwnerByUserId(requireContext(), authViewModel.getCurrentUser().getValue().getUid());



        OneTimeWorkRequest fetchOwnerRequest = new OneTimeWorkRequest.Builder(FetchOwnerWorker.class)
                .setInputData(new Data.Builder().putString("userId", currentUser.getUid()).build())
                .build();

        WorkManager.getInstance(requireActivity())
                .enqueue(fetchOwnerRequest);


        WorkManager.getInstance(requireActivity())
                .getWorkInfoByIdLiveData(fetchOwnerRequest.getId())
                .observe(getViewLifecycleOwner(), workInfo -> {
                    if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                        Map<String, Object> ownerData = workInfo.getOutputData().getKeyValueMap();
                        Log.d("MYAPP", "onCreateView: " + ownerData);
                        Log.d("MYAPP", "onCreateView: " + workInfo.getOutputData());
                        if(!ownerData.isEmpty()) {
                            Owner owner = new Owner(
                                    (String) ownerData.get("owner_id"),
                                    (String) ownerData.get("user_id"),
                                    (String) ownerData.get("aadhaar_number")
                            );
                            //TODO: Deserialize ambulance string and convert it to List<Ambulance> and pass it to owner.

                            List<Ambulance> ambulances = deserializeAmbulancesString((String) ownerData.get("ambulances"));
                            List<EmployeeDriver> employees = deserializeEmployeesString((String) ownerData.get("employees"));
                            owner.setAmbulances(ambulances);
                            owner.setEmployees(employees);

                            ownerViewModel.setOwner(owner);
                        }
                        else return;
                    } else if (workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                        Toast.makeText(requireActivity(), workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                    }
                });

//        viewBinding.signoutBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseAuth.getInstance().signOut();
//                Intent intent = new Intent(requireActivity(), AuthActivity.class);
//                startActivity(intent);
//                requireActivity().finish();
//            }
//        });

        NavigationBarView bottomNav = viewBinding.bottomNavOwner;

         navController = NavHostFragment.findNavController(getChildFragmentManager().findFragmentById(R.id.owner_bottom_nav_container));

        NavOptions.Builder options = new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                                .setPopEnterAnim(android.R.anim.slide_in_left)
                                        .setPopExitAnim(android.R.anim.slide_out_right);

        MenuItem item = bottomNav.getMenu().findItem(R.id.navAdd_Ambulance);

        dashboardViewModel.getDisplayMode().observe(getViewLifecycleOwner(), displayMode -> {
            if(displayMode.equals(HomeFragment.MODE_AMBULANCE)) {
                item.setTitle("Ambulance");
            }
            else {
                item.setTitle("Driver");

            }
        });

        bottomNav.setOnItemSelectedListener(menuItem -> {

            if(menuItem.getItemId() == R.id.navHome) {
                navigateToDestination(R.id.ownerHomeFragment);
            }
            else if(menuItem.getItemId() == R.id.navAdd_Ambulance) {
                String displayMode = dashboardViewModel.getDisplayMode().getValue();

                if(displayMode.equals(HomeFragment.MODE_AMBULANCE)) {
                    navigateToDestination(R.id.ownerManageAmbulanceFragment);
                }
                else {
                    navigateToDestination(R.id.addDriverFragment);
                }

            }
            else if(menuItem.getItemId() == R.id.navNotification) {
                navigateToDestination(R.id.ownerNotificationFragment);
            }
            else if(menuItem.getItemId() == R.id.navProfile) {
                navigateToDestination(R.id.ownerProfileFragment);
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

    private List<Ambulance> deserializeAmbulancesString(String serializedString) {
        List<Ambulance> ambulances = new ArrayList<>();

        if (serializedString == null || serializedString.isEmpty() || serializedString.equals("[]")) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();

        try {

            JsonArray jsonArray = gson.fromJson(serializedString, JsonArray.class);

            for(JsonElement element : jsonArray) {
                Log.d("MYAPP", "deserializeAmbulancesString: " + element.toString());
                Type type = new TypeToken<Map<String, Object>>() {}.getType();

                Map<String, Object> map = gson.fromJson(element.getAsString(), type);
                Log.d("MYAPP", "deserializeAmbulancesString: " + map.toString());
                ambulances.add(Ambulance.createFromMap(map));
            }


        } catch (JsonSyntaxException e) {
            Log.w("convertJsonArrayToListOfMaps", "Error parsing JSON array string: " + e.getMessage());
        }

        return ambulances;
    }

    private List<EmployeeDriver> deserializeEmployeesString(String serializedString) {
        List<EmployeeDriver> employees = new ArrayList<>();

        if (serializedString == null || serializedString.isEmpty() || serializedString.equals("[]")) {
            return new ArrayList<>(); // Return empty list for null/empty/empty array string
        }

        Gson gson = new Gson(); // Assuming you have Gson dependency added

        try {

            JsonArray jsonArray = gson.fromJson(serializedString, JsonArray.class);

            for(JsonElement element : jsonArray) {
                Log.d("MYAPP", "deserializeEmployeeString: " + element.toString());
                Type type = new TypeToken<Map<String, Object>>() {}.getType();

                Map<String, Object> map = gson.fromJson(element.getAsString(), type);
                Log.d("MYAPP", "deserializeEmployeesString: " + map.toString());
                employees.add(EmployeeDriver.createFromMap(map));
            }


        } catch (JsonSyntaxException e) {
            Log.w("convertJsonArrayToListOfMaps", "Error parsing JSON array string: " + e.getMessage());
        }

        return employees;
    }



}