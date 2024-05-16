package com.swasthavyas.emergencyllp.component.dashboard.owner.domain.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.domain.model.Ambulance;

import java.util.List;

public class AmbulanceAdapter extends RecyclerView.Adapter<AmbulanceAdapter.ViewHolder> {

    private List<Ambulance> ambulances;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // We can attach some listeners here
            textView = (TextView) itemView.findViewById(R.id.vehicle_number_holder);
        }

        public TextView getTextView() {
            return textView;
        }


    }

    public AmbulanceAdapter(List<Ambulance> ambulances) {
        this.ambulances = ambulances;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ambulance_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getTextView().setText(ambulances.get(position).getVehicleNumber());
    }


    @Override
    public int getItemCount() {
        return ambulances.size();
    }
}
