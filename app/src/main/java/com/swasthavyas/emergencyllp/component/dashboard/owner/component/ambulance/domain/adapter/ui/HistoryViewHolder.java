package com.swasthavyas.emergencyllp.component.dashboard.owner.component.ambulance.domain.adapter.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.databinding.HistoryBinding;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryViewHolder extends RecyclerView.ViewHolder {

    private final CircleImageView profileImage;
    private final TextView name;
    private final TextView tripDate;
    private final TextView tripTime;
    private final TextView tripEarning;
    private final View itemView;

    public HistoryViewHolder(@NonNull View itemView) {
        super(itemView);

        HistoryBinding viewBinding = HistoryBinding.bind(itemView);

        profileImage = viewBinding.profileImage;
        name = viewBinding.name;
        tripDate = viewBinding.tripDate;
        tripTime = viewBinding.tripTime;
        tripEarning = viewBinding.tripEarning;
        this.itemView = itemView;
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public void setTripDate(String dateString) {
        this.tripDate.setText(dateString);
    }

    public void setTripTime(String tripTime) {
        this.tripTime.setText(tripTime);
    }

    public void setTripEarning(Context context, String tripEarning) {
        this.tripEarning.setText(context.getString(R.string.trip_earning, tripEarning));
    }

    public void setProfileImage(Context context, StorageReference profileReference) {
        Glide.with(context)
                .load(profileReference)
                .placeholder(R.drawable.sample_profile)
                .dontAnimate()
                .into(this.profileImage);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.itemView.setOnClickListener(onClickListener);
    }
}
