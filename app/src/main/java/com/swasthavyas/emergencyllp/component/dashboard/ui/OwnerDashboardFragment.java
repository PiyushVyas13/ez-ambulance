package com.swasthavyas.emergencyllp.component.dashboard.ui;

import android.content.Intent;
import android.os.Bundle;

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

        NavController navController = NavHostFragment.findNavController(getChildFragmentManager().findFragmentById(R.id.owner_bottom_nav_container));

        NavOptions.Builder options = new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                                .setPopEnterAnim(android.R.anim.slide_in_left)
                                        .setPopExitAnim(android.R.anim.slide_out_right);



        bottomNav.setOnItemSelectedListener(menuItem -> {

            int itemId = menuItem.getItemId();
            int currentDestinationTag = Integer.parseInt(navController.getCurrentDestination().getLabel().toString());


            if(itemId == R.id.navHome) {
                if(navController.getCurrentDestination().getId() != navController.getGraph().findNode(R.id.ownerHomeFragment).getId()) {
                    navController.navigate(R.id.ownerHomeFragment, null, options.setEnterAnim(android.R.anim.slide_in_left).setExitAnim(android.R.anim.slide_out_right).build());
                }
            }
            else if (itemId == R.id.navAdd_Ambulance) {
                int ambulanceDestinationTag = Integer.parseInt(navController.getGraph().findNode(R.id.ownerManageAmbulanceFragment).getLabel().toString());


                if(navController.getCurrentDestination().getId() != navController.getGraph().findNode(R.id.ownerManageAmbulanceFragment).getId()) {
                    String displayMode = dashboardViewModel.getDisplayMode().getValue();

                    int destinationId = displayMode.equals(HomeFragment.MODE_AMBULANCE) ? R.id.ownerManageAmbulanceFragment : R.id.addDriverFragment;
                    if(ambulanceDestinationTag > currentDestinationTag) {
                        navController.navigate(destinationId, null, options.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left).build());
                    }else if(ambulanceDestinationTag < currentDestinationTag) {
                        navController.navigate(destinationId, null, options.setEnterAnim(android.R.anim.slide_in_left).setExitAnim(android.R.anim.slide_out_right).build());
                    }

                }

//                navController.navigate(R.id.ownerManageAmbulanceFragment, null, options.setPopUpTo(R.id.ownerManageAmbulanceFragment, true).build());
            } else if (itemId == R.id.navNotification) {

                int notificationDestinationTag = Integer.parseInt(navController.getGraph().findNode(R.id.ownerNotificationFragment).getLabel().toString());


                if(navController.getCurrentDestination().getId() != navController.getGraph().findNode(R.id.ownerNotificationFragment).getId()) {

                    if(notificationDestinationTag > currentDestinationTag) {
                        navController.navigate(R.id.ownerNotificationFragment, null, options.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left).build());
                    }else if(notificationDestinationTag < currentDestinationTag) {
                        navController.navigate(R.id.ownerNotificationFragment, null, options.setEnterAnim(android.R.anim.slide_in_left).setExitAnim(android.R.anim.slide_out_right).build());
                    }

                }


//                navController.navigate(R.id.ownerNotificationFragment, null, options.setPopUpTo(R.id.ownerNotificationFragment, true).build());
            }else{

                int profileDestinationTag = Integer.parseInt(navController.getGraph().findNode(R.id.ownerProfileFragment).getLabel().toString());


                if(navController.getCurrentDestination().getId() != navController.getGraph().findNode(R.id.ownerProfileFragment).getId()) {

                    if(profileDestinationTag > currentDestinationTag) {
                        navController.navigate(R.id.ownerProfileFragment, null, options.setEnterAnim(R.anim.slide_in_right).setExitAnim(R.anim.slide_out_left).build());
                    }else if(profileDestinationTag < currentDestinationTag) {
                        navController.navigate(R.id.ownerProfileFragment, null, options.setEnterAnim(android.R.anim.slide_in_left).setExitAnim(android.R.anim.slide_out_right).build());
                    }

                }

//                navController.navigate(R.id.ownerProfileFragment, null, options.setPopUpTo(R.id.ownerProfileFragment, true).build());
            }


            return true;
        });

        return viewBinding.getRoot();
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