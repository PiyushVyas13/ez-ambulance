package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.adapter.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.adapter.HistoryAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model.TripHistory;
import com.swasthavyas.emergencyllp.databinding.HistoryHeadlineBinding;

import java.util.List;

public class HistoryHeadlineViewHolder extends RecyclerView.ViewHolder {

    private final TextView monthName;
     private final RecyclerView historyList;

    public HistoryHeadlineViewHolder(@NonNull View itemView) {
        super(itemView);

        HistoryHeadlineBinding viewBinding = HistoryHeadlineBinding.bind(itemView);

        monthName = viewBinding.monthName;
          historyList = viewBinding.historyList;
    }

    public void setHistoryList(Context context, HistoryAdapter adapter) {
         this.historyList.setLayoutManager(new LinearLayoutManager(context));
         this.historyList.setAdapter(adapter);
    }

    public void setMonthName(String monthName) {
        this.monthName.setText(monthName);
    }
}
