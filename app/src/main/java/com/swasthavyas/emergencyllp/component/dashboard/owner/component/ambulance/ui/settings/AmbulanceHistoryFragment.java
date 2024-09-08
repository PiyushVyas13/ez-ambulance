package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.ui.settings;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.adapter.HistoryHeadlineAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel.AmbulanceViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.TripHistory;
import com.swasthavyas.emergencyllp.databinding.FragmentAmbulanceHistoryBinding;
import com.swasthavyas.emergencyllp.util.TimestampUtility;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class
AmbulanceHistoryFragment extends Fragment {

    FragmentAmbulanceHistoryBinding viewBinding;
    AmbulanceViewModel ambulanceViewModel;
    List<TripHistory> ambulanceHistoryList;
    Ambulance ambulance;
    long lifetimeRides = -1;
    long lastWeekRides = -1;

    public AmbulanceHistoryFragment() {
        ambulanceHistoryList = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ambulanceViewModel = new ViewModelProvider(requireActivity()).get(AmbulanceViewModel.class);

        this.ambulance = ambulanceViewModel.getCurrentAmbulance().getValue();

        if(ambulance == null) {
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(viewBinding.getRoot())
                        .navigate(
                                R.id.ambulanceDetailFragment,
                                null,
                                new NavOptions.Builder().
                                        setEnterAnim(android.R.anim.fade_in).
                                        setExitAnim(android.R.anim.slide_out_right)
                                        .build()
                        );
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this,backPressedCallback);
    }

    private void fetchAmbulanceHistory() {
        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        if (ambulanceHistoryList.isEmpty()) {
            dbInstance
                    .collection("trip_history")
                    .whereEqualTo("trip.assignedAmbulanceId", ambulance.getId())
                    .orderBy("completionTimestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                       if(task.isSuccessful()) {
                           for(DocumentSnapshot snapshot : task.getResult()) {
                               Map<String, Object> documentMap = snapshot.getData();

                               if(documentMap == null) {
                                   continue;
                               }
                               TripHistory history = TripHistory.createFromMap(snapshot.getData());
                               ambulanceHistoryList.add(history);
                           }

                           Log.d(TAG, "fetchAmbulanceHistory: " + ambulanceHistoryList);
                           prepareRecyclerView(ambulanceHistoryList);
                       }
                    });
        } else {
            prepareRecyclerView(ambulanceHistoryList);
        }
    }

    private void getTripForDate(int day, int month, int year) {
        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        Calendar calendar = Calendar.getInstance();

        calendar.set(year, month, day, 0, 0, 0);
        Timestamp startOfDay = new Timestamp(calendar.getTime());

        calendar.set(year, month, day, 23, 59, 59);
        Timestamp endOfDay = new Timestamp(calendar.getTime());


        dbInstance
                .collection("trip_history")
                .whereEqualTo("trip.assignedAmbulanceId", ambulance.getId())
                .whereGreaterThanOrEqualTo("completionTimestamp", startOfDay)
                .whereLessThanOrEqualTo("completionTimestamp", endOfDay)
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       List<TripHistory> historyList = new ArrayList<>();

                       for(DocumentSnapshot snapshot : task.getResult().getDocuments()) {
                           Map<String, Object> data = snapshot.getData();

                           assert data != null;
                           TripHistory tripHistory = TripHistory.createFromMap(data);
                           historyList.add(tripHistory);
                       }

                       prepareRecyclerView(historyList);
                   }
                });
    }

    private void prepareRecyclerView(List<TripHistory> historyList) {
        Map<String, List<TripHistory>> segregatedHistoryMap = segregateTripHistory(historyList);

        HistoryHeadlineAdapter historyHeadlineAdapter = new HistoryHeadlineAdapter(requireContext(), segregatedHistoryMap);
        viewBinding.historyList.setLayoutManager(new LinearLayoutManager(requireContext()));
        viewBinding.historyList.setAdapter(historyHeadlineAdapter);

    }

    private void setLifetimeRides() {
        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        if(lifetimeRides < 0) {
            dbInstance
                    .collection("trip_history")
                    .whereEqualTo("trip.assignedAmbulanceId", ambulance.getId())
                    .count()
                    .get(AggregateSource.SERVER)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            lifetimeRides = task.getResult().getCount();
                            viewBinding.lifetimeRides.setText(String.valueOf(task.getResult().getCount()));
                        }
                    });
        } else {
            viewBinding.lifetimeRides.setText(String.valueOf(lifetimeRides));
        }
    }

    private void setLastWeekRides() {
        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        Timestamp now = Timestamp.now();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);

        Timestamp sevenDaysAgo = new Timestamp(calendar.getTime());

        if(lastWeekRides < 0) {
            dbInstance
                    .collection("trip_history")
                    .whereEqualTo("trip.assignedAmbulanceId", ambulance.getId())
                    .whereLessThanOrEqualTo("completionTimestamp", now)
                    .whereGreaterThanOrEqualTo("completionTimestamp", sevenDaysAgo)
                    .orderBy("completionTimestamp", Query.Direction.DESCENDING)
                    .count()
                    .get(AggregateSource.SERVER)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "setLastWeekRides: " + task.getResult().getCount());
                            lastWeekRides = task.getResult().getCount();
                            viewBinding.lastWeekRides.setText(String.valueOf(lastWeekRides));
                        } else {
                            Log.d(TAG, "setLastWeekRides: " + task.getException());
                        }
                    });
        } else {
            viewBinding.lastWeekRides.setText(String.valueOf(lastWeekRides));
        }
    }

    private Map<String, List<TripHistory>> segregateTripHistory(List<TripHistory> tripHistoryList){
        Map<String, List<TripHistory>> resultMap = new LinkedHashMap<>();

        TimestampUtility timestampUtility = new TimestampUtility();

        for(TripHistory tripHistory : tripHistoryList) {
            timestampUtility.setTimestamp(tripHistory.getCompletionTimestamp());
            String monthYear = timestampUtility.getFormattedDate("MMMM yyyy");

            if(!resultMap.containsKey(monthYear)) {
                resultMap.put(monthYear, new ArrayList<>());
            }

            resultMap.get(monthYear).add(tripHistory);
        }

        Log.d(TAG, "segregateTripHistory: " + resultMap);
        return resultMap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentAmbulanceHistoryBinding.inflate(getLayoutInflater());
        setLifetimeRides();
        setLastWeekRides();
        fetchAmbulanceHistory();
        return viewBinding.getRoot();
    }

}