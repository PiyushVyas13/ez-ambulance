package com.swasthavyas.emergencyllp.component.history.domain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swasthavyas.emergencyllp.component.history.domain.adapter.ui.HistoryHeadlineViewHolder;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.TripHistory;
import com.swasthavyas.emergencyllp.databinding.HistoryHeadlineBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryHeadlineAdapter extends RecyclerView.Adapter<HistoryHeadlineViewHolder> {

    private final Context context;
    private final Map<String, List<TripHistory>> segregatedHistory;
    private final List<String> sectionHeaders;

    public HistoryHeadlineAdapter(Context context, Map<String, List<TripHistory>> segregatedHistory) {
        this.context = context;
        this.segregatedHistory = segregatedHistory;
        sectionHeaders = new ArrayList<>(segregatedHistory.keySet());
    }

    @NonNull
    @Override
    public HistoryHeadlineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        HistoryHeadlineBinding viewBinding = HistoryHeadlineBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryHeadlineViewHolder(viewBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHeadlineViewHolder holder, int position) {
        String monthName = sectionHeaders.get(position);
        List<TripHistory> historyList = segregatedHistory.get(monthName);

        if(historyList != null && !historyList.isEmpty()) {
            holder.setMonthName(monthName);
            HistoryAdapter adapter = new HistoryAdapter(context, historyList);
            holder.setHistoryList(context, adapter);
        }

    }

    @Override
    public int getItemCount() {
        return segregatedHistory == null ? 0 : segregatedHistory.size();
    }
}
