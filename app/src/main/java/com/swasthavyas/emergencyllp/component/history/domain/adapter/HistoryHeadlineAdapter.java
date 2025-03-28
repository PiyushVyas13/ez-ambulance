package com.swasthavyas.emergencyllp.component.history.domain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.StorageReference;
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
    private final String displayName;
    private final String historyMode;
    private final HistoryAdapter.OnHistoryItemClickListener onClickListener;

    public HistoryHeadlineAdapter(Context context,
                                  Map<String, List<TripHistory>> segregatedHistory,
                                  String displayName,
                                  String historyMode,
                                  HistoryAdapter.OnHistoryItemClickListener onClickListener) {
        this.context = context;
        this.segregatedHistory = segregatedHistory;
        sectionHeaders = new ArrayList<>(segregatedHistory.keySet());
        this.displayName = displayName;
        this.onClickListener = onClickListener;
        this.historyMode = historyMode;
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
            HistoryAdapter adapter = new HistoryAdapter(context, historyList, displayName, historyMode, onClickListener);
            holder.setHistoryList(context, adapter);
        }

    }

    @Override
    public int getItemCount() {
        return segregatedHistory == null ? 0 : segregatedHistory.size();
    }
}
