package com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {

    private final List<EmployeeDriver> employeeDrivers;
    private final OnDeleteCallback deleteCallback;
    private final Context context;

    public interface OnDeleteCallback {
        void onDelete(String driverId, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView driverName;
        private final ImageView profilePicture;
        private final ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // We can attach some listeners here
            driverName = (TextView) itemView.findViewById(R.id.driver_name_holder);
            profilePicture = (ImageView) itemView.findViewById(R.id.employee_profile_pic);
            deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);

        }

        public TextView getDriverName() {
            return driverName;
        }
        public ImageView getProfilePicture() {return profilePicture;}


        public ImageButton getDeleteButton() {
            return deleteButton;
        }
    }

    public EmployeeAdapter(Context context, OnDeleteCallback onDeleteCallback, List<EmployeeDriver> employeeDrivers) {
        this.context = context;
        this.employeeDrivers = employeeDrivers;
        this.deleteCallback = onDeleteCallback;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.employee_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeeDriver employee = employeeDrivers.get(position);
        holder.getDriverName().setText(employee.getName());
        holder.getDeleteButton().setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Driver")
                    .setMessage(String.format("Are you sure you want to delete driver '%s'", employee.getName()))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Toast.makeText(context, "Starting driver delete...", Toast.LENGTH_SHORT).show();
                        deleteCallback.onDelete(employee.getDriverId(), position);
                    })
                    .setNegativeButton("Cancel", ((dialog, which) -> {}))
                    .show();
        });
        //TODO: Load Profile pic from employee profile
    }


    @Override
    public int getItemCount() {
        return employeeDrivers.size();
    }
}
