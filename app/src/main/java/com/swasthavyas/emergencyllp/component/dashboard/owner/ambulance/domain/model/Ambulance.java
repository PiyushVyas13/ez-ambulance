package com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.swasthavyas.emergencyllp.util.types.AmbulanceType;

import java.io.Serializable;
import java.util.Map;

public class Ambulance implements Parcelable {


    private String id;
    private String ownerId;
    private AmbulanceType ambulanceType;
    private String vehicleNumber;
    private String vehicleType;
    private StorageReference imageRef;

    private Ambulance() {

    }

    protected Ambulance(Parcel parcel) {
        id = parcel.readString();
        ownerId = parcel.readString();
        ambulanceType = AmbulanceType.valueOf(parcel.readString());
        vehicleNumber = parcel.readString();
        vehicleType = parcel.readString();
        imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(parcel.readString());
    }



    private Ambulance(String id, String ownerId, AmbulanceType ambulanceType, String vehicleNumber, String vehicleType, String imageRef)  {
        this.id = id;
        this.ownerId = ownerId;
        this.ambulanceType = ambulanceType;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageRef);

    }

    public static final Creator<Ambulance> CREATOR = new Creator<Ambulance>() {
        @Override
        public Ambulance createFromParcel(Parcel in) {
            return new Ambulance(in);
        }

        @Override
        public Ambulance[] newArray(int size) {
            return new Ambulance[size];
        }
    };

    public static Ambulance createFromMap(Map<String, Object> map) {
        String id = (String) map.get(ModelColumns.ID);
        String ownerId = (String) map.get(ModelColumns.OWNER_ID);
        AmbulanceType ambulanceType = AmbulanceType.valueOf((String) map.get(ModelColumns.AMBULANCE_TYPE));
        String vehicleNumber = (String) map.get(ModelColumns.VEHICLE_NUMBER);
        String vehicleType = (String) map.get(ModelColumns.AMBULANCE_TYPE);
        String imageRef = (String) map.get(ModelColumns.IMAGE_REF);

        return new Ambulance(id, ownerId, ambulanceType, vehicleNumber, vehicleType, imageRef);
    }

    public static Ambulance getInstance() {
        return new Ambulance();
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public AmbulanceType getAmbulanceType() {
        return ambulanceType;
    }

    public void setAmbulanceType(AmbulanceType ambulanceType) {
        this.ambulanceType = ambulanceType;
    }


    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public StorageReference getImageRef() {
        return imageRef;
    }

    public void setImageRef(StorageReference imageRef) {
        this.imageRef = imageRef;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(getId());
        dest.writeString(getOwnerId());
        dest.writeString(getAmbulanceType().name());
        dest.writeString(getVehicleNumber());
        dest.writeString(getVehicleType());
        dest.writeString(getImageRef().toString());
    }

    public static class ModelColumns {
        public static final String ID = "id";
        public static final String OWNER_ID = "owner_id";
        public static final String AMBULANCE_TYPE = "ambulanceType";
        public static final String VEHICLE_NUMBER = "vehicle_number";
        public static final String VEHICLE_TYPE = "vehicle_type";
        public static final String IMAGE_REF = "image_ref";
    }
}
