package com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.model.Ambulance;

import java.util.List;
import java.util.Locale;

public class AmbulanceAdapter extends RecyclerView.Adapter<AmbulanceAdapter.ViewHolder> {


    public interface OnDeleteCallback {
        void onDelete(String ambulanceId, String imageRef, int position);
    }


    private final List<Ambulance> ambulances;
    private Context context;

    private OnDeleteCallback deleteCallback;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView vehicleNumber;
        private final TextView serialNumber;
        private final ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // We can attach some listeners here
            vehicleNumber = (TextView) itemView.findViewById(R.id.vehicle_number_holder);
            serialNumber = (TextView) itemView.findViewById(R.id.sr_number);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
        }

        public TextView getVehicleNumber() {
            return vehicleNumber;
        }

        public TextView getSerialNumber() {
            return serialNumber;
        }

        public ImageButton getDeleteButton() {return deleteButton; }
    }

    public AmbulanceAdapter(Context context, List<Ambulance> ambulances, OnDeleteCallback deleteCallback) {
        this.context = context;
        this.ambulances = ambulances;
        this.deleteCallback = deleteCallback;
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
        Ambulance ambulance = ambulances.get(position);
        holder.getVehicleNumber().setText(ambulance.getVehicleNumber());
        holder.getSerialNumber().setText(String.format(Locale.getDefault(), "%d", position+1));


        holder.getDeleteButton().setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Ambulance")
                    .setMessage(String.format("Are you sure you want to delete Ambulance '%s'?", ambulance.getVehicleNumber()))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Toast.makeText(context, "Starting ambulance delete...", Toast.LENGTH_SHORT).show();
                        deleteCallback.onDelete(ambulance.getId(), ambulance.getImageRef().toString(), position);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {})
                    .show();
        });
    }


    @Override
    public int getItemCount() {
        return ambulances.size();
    }

}
