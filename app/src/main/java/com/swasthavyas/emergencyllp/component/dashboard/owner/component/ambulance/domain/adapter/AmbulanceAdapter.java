package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.model.Ambulance;

import java.util.List;
import java.util.Locale;

public class AmbulanceAdapter extends RecyclerView.Adapter<AmbulanceAdapter.ViewHolder> {


    public interface OnDeleteCallback {
        void onDelete(String ambulanceId, String ambulanceNumber, String imageRef, int position);
    }


    private final List<Ambulance> ambulances;
    private final Context context;
    private List<String> availableAmbulanceIds;

    private final OnDeleteCallback deleteCallback;
    private final OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position, Ambulance item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView vehicleNumber;
        private final TextView serialNumber;
        private final ImageButton deleteButton;
        private final Chip availabilityChip;




        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // We can attach some listeners here
            vehicleNumber = (TextView) itemView.findViewById(R.id.vehicle_number_holder);
            serialNumber = (TextView) itemView.findViewById(R.id.sr_number);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);
            availabilityChip = (Chip) itemView.findViewById(R.id.availability);
        }

        public TextView getVehicleNumber() {
            return vehicleNumber;
        }

        public TextView getSerialNumber() {
            return serialNumber;
        }

        public ImageButton getDeleteButton() {return deleteButton; }

        public void setOnItemClickListener(Ambulance item, int position, OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(position, item));
        }

        public Chip getAvailabilityChip() {return availabilityChip;}

    }

    public void setAvailableAmbulanceIds(List<String> availableAmbulanceIds) {
        this.availableAmbulanceIds = availableAmbulanceIds;
        notifyDataSetChanged();
    }

    public AmbulanceAdapter(Context context, List<Ambulance> ambulances, List<String> availableAmbulanceIds, OnDeleteCallback deleteCallback, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.ambulances = ambulances;
        this.deleteCallback = deleteCallback;
        this.itemClickListener = onItemClickListener;
        this.availableAmbulanceIds = availableAmbulanceIds;
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
        holder.setOnItemClickListener(ambulance, position, itemClickListener);

        holder.getAvailabilityChip().setText(
                availableAmbulanceIds.contains(ambulance.getId()) ?
                        "Not Available" :
                        "Available"
        );

        holder.getAvailabilityChip().setChipBackgroundColor(
                availableAmbulanceIds.contains(ambulance.getId()) ?
                        ColorStateList.valueOf(context.getColor(R.color.error)) :
                        ColorStateList.valueOf(context.getColor(R.color.success))
        );


        holder.getDeleteButton().setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Ambulance")
                    .setMessage(String.format("Are you sure you want to delete Ambulance '%s'?", ambulance.getVehicleNumber()))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Toast.makeText(context, "Starting ambulance delete...", Toast.LENGTH_SHORT).show();
                        deleteCallback.onDelete(ambulance.getId(), ambulance.getVehicleNumber(), ambulance.getImageRef().toString(), position);
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
