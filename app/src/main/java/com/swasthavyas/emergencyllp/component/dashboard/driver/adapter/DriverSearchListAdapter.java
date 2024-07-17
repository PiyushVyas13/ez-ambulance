package com.swasthavyas.emergencyllp.component.dashboard.driver.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;

import java.util.ArrayList;
import java.util.List;

public class DriverSearchListAdapter extends RecyclerView.Adapter<DriverSearchListAdapter.ViewHolder> {
    private List<EmployeeDriver> employeeDrivers;
    private Context appContext;

    public interface ItemClickListener {
        void onItemClick(int position, EmployeeDriver driver);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setFilteredList(List<EmployeeDriver> employeeDrivers) {
        this.employeeDrivers = employeeDrivers;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView driverName;
        private final ImageView driverProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            driverName = itemView.findViewById(R.id.driver_list_item_name);
            driverProfile = itemView.findViewById(R.id.driver_list_item_profile);
        }


        public TextView getDriverName() {
            return driverName;
        }

        public ImageView getDriverProfile() {
            return driverProfile;
        }

        public void setOnItemClickListener(EmployeeDriver item, int position, ItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(position, item));
        }
    }

    private final ItemClickListener itemClickListener;

    public DriverSearchListAdapter(Context context, ItemClickListener itemClickListener) {
        appContext = context;
        employeeDrivers = new ArrayList<>();
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public DriverSearchListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.driver_search_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverSearchListAdapter.ViewHolder holder, int position) {
        EmployeeDriver driver = employeeDrivers.get(position);

        holder.getDriverName().setText(driver.getName());
        holder.setOnItemClickListener(driver, position, itemClickListener);
//        Glide
//                .with(appContext)
//                .load("some string")
//                .into(holder.getDriverProfile());
    }

    @Override
    public int getItemCount() {
        return employeeDrivers.size();
    }
}
