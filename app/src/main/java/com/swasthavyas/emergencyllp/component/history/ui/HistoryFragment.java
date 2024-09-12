package com.swasthavyas.emergencyllp.component.history.ui;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
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

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.history.domain.adapter.HistoryHeadlineAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel.AmbulanceViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.TripHistory;
import com.swasthavyas.emergencyllp.databinding.FragmentHistoryBinding;
import com.swasthavyas.emergencyllp.util.TimestampUtility;
import com.swasthavyas.emergencyllp.util.firebase.FirebaseService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class
HistoryFragment extends Fragment {

    FragmentHistoryBinding viewBinding;
    List<TripHistory> ambulanceHistoryList;
    long lifetimeRides = -1;
    long lastWeekRides = -1;

    private String recordableId;
    private String recordableFieldName;

    public HistoryFragment() {
        ambulanceHistoryList = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HistoryFragmentArgs fragmentArgs = HistoryFragmentArgs.fromBundle(getArguments());

        recordableId = fragmentArgs.getRecordableId();
        recordableFieldName = fragmentArgs.getRecordableFieldName();


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

    private void fetchHistory() {
        FirebaseFirestore dbInstance = FirebaseService.getInstance().getFirestoreInstance();

        if (ambulanceHistoryList.isEmpty()) {
            dbInstance
                    .collection("trip_history")
                    .whereEqualTo(recordableFieldName, recordableId)
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
                .whereEqualTo(recordableFieldName, recordableId)
                .whereGreaterThanOrEqualTo("completionTimestamp", startOfDay)
                .whereLessThanOrEqualTo("completionTimestamp", endOfDay)
                .orderBy("completionTimestamp", Query.Direction.DESCENDING)
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
                    .whereEqualTo(recordableFieldName, recordableId)
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
                    .whereEqualTo(recordableFieldName, recordableId)
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentHistoryBinding.inflate(getLayoutInflater());
        if(recordableFieldName.equals("trip.assignedAmbulanceId"))  {
            viewBinding.ridesCountLayout.setVisibility(View.VISIBLE);
            setLifetimeRides();
            setLastWeekRides();
        }

        fetchHistory();

        viewBinding.filterDateLayout.setEndIconOnClickListener(v -> {
            if(!viewBinding.filterDate.getText().toString().isEmpty()) {
                viewBinding.filterDate.setText("");
                fetchHistory();
                viewBinding.filterDateLayout.setEndIconDrawable(R.drawable.calender);
            } else {
                MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Search trips for a date")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

                picker.addOnPositiveButtonClickListener(selection -> {
                    Date date = new Date(selection);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    int year = calendar.get(Calendar.YEAR);
                    Log.d(TAG, "onCreateView: " + year);
                    int month = calendar.get(Calendar.MONTH);
                    Log.d(TAG, "onCreateView: " + month);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    Log.d(TAG, "onCreateView: " + day);

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                    viewBinding.filterDate.setText(formatter.format(date));

                    getTripForDate(day, month, year);
                    viewBinding.filterDateLayout.setEndIconDrawable(R.drawable.baseline_cancel_24);
                });

                picker.show(getChildFragmentManager(), "FILTER_DATE_PICKER");

            }


        });
        return viewBinding.getRoot();
    }

}