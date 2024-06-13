package com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.model.EmployeeDriver;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {

    private List<EmployeeDriver> employeeDrivers;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView driverName;
         private final ImageView profilePicture;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // We can attach some listeners here
            driverName = (TextView) itemView.findViewById(R.id.driver_name_holder);
             profilePicture = (ImageView) itemView.findViewById(R.id.employee_profile_pic);
        }

        public TextView getDriverName() {
            return driverName;
        }
         public ImageView getProfilePicture() {return profilePicture;}


    }

    public EmployeeAdapter(List<EmployeeDriver> employeeDrivers) {
        this.employeeDrivers = employeeDrivers;
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
        //TODO: Load Profile pic from employee profile
    }


    @Override
    public int getItemCount() {
        return employeeDrivers.size();
    }
}
