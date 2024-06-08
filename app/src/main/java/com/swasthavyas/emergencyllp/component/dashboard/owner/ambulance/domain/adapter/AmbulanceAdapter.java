package com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.model.Ambulance;

import java.util.List;
import java.util.Locale;

public class AmbulanceAdapter extends RecyclerView.Adapter<AmbulanceAdapter.ViewHolder> {

    private final List<Ambulance> ambulances;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView vehicleNumber;
        private final TextView serialNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // We can attach some listeners here
            vehicleNumber = (TextView) itemView.findViewById(R.id.vehicle_number_holder);
            serialNumber = (TextView) itemView.findViewById(R.id.sr_number);
        }

        public TextView getVehicleNumber() {
            return vehicleNumber;
        }

        public TextView getSerialNumber() {
            return serialNumber;
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
        holder.getVehicleNumber().setText(ambulances.get(position).getVehicleNumber());
        holder.getSerialNumber().setText(String.format(Locale.getDefault(), "%d", position+1));
    }


    @Override
    public int getItemCount() {
        return ambulances.size();
    }
}
