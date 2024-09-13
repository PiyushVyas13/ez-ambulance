package com.swasthavyas.emergencyllp.component.history.domain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.history.domain.adapter.ui.HistoryViewHolder;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.viewmodel.HistoryViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.Trip;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.TripHistory;
import com.swasthavyas.emergencyllp.component.history.ui.HistoryFragmentDirections;
import com.swasthavyas.emergencyllp.databinding.HistoryBinding;
import com.swasthavyas.emergencyllp.util.TimestampUtility;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    private final Context context;
    private final List<TripHistory> historyList;

    private final String displayName;

    private final OnHistoryItemClickListener onClickListener;

    public interface OnHistoryItemClickListener {
        void onItemClick(View v, TripHistory history);
    }

    public HistoryAdapter(Context context, List<TripHistory> historyList, String displayName, OnHistoryItemClickListener onClickListener) {
        this.context = context;
        this.historyList = historyList;
        this.displayName = displayName;
        this.onClickListener = onClickListener;
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

        holder.setName(displayName);
        holder.setTripDate(dateString);
        holder.setTripTime(timeString);
        holder.setTripEarning(context, String.valueOf(trip.getPrice()));
        holder.setProfileImage(context, null);
        holder.setOnClickListener(v -> onClickListener.onItemClick(v, tripHistory));

    }

    @Override
    public int getItemCount() {
        return historyList == null ? 0 : historyList.size();
    }
}
