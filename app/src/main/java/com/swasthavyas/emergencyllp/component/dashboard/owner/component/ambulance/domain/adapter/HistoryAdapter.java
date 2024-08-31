package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.adapter.ui.HistoryViewHolder;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.TripHistory;
import com.swasthavyas.emergencyllp.databinding.HistoryBinding;
import com.swasthavyas.emergencyllp.util.TimestampUtility;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    private final Context context;
    private final List<TripHistory> historyList;

    public HistoryAdapter(Context context, List<TripHistory> historyList) {
        this.context = context;
        this.historyList = historyList;
    }


    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HistoryBinding viewBinding = HistoryBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HistoryViewHolder(viewBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        TripHistory tripHistory = historyList.get(position);

        Trip trip = tripHistory.getTrip();

        TimestampUtility timestampUtility = new TimestampUtility(tripHistory.getCompletionTimestamp());


        String dateString = timestampUtility.getFormattedDate("MMM d, YYYY");
        String timeString = timestampUtility.getFormattedDate("hh:mm a");

        holder.setName(trip.getAssignedDriverId());
        holder.setTripDate(dateString);
        holder.setTripTime(timeString);
        holder.setTripEarning(context, String.valueOf(trip.getPrice()));
        holder.setProfileImage(context, null);

    }

    @Override
    public int getItemCount() {
        return historyList == null ? 0 : historyList.size();
    }
}
