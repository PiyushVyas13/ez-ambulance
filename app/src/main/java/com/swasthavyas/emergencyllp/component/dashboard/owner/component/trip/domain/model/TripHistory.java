package com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

import java.util.List;
import java.util.Map;

public class TripHistory {

    private Trip trip ;
    private String terminalState;
    private Timestamp completedAt;
    private List<String> routePolyLines;
    private String ambulanceImageRef;
    private String driverProfileImageRef;


    public TripHistory(){
        // required public empty constructor
    }

    /**
     * @param trip copy of trip object that was recorded as history
     * @param terminalState last state after the trip got removed from RTDB
     * @param completedAt completion time of the trip
     * @param routePolyLines encoded polyline from pickup to drop location
     */
    private TripHistory(Trip trip,String terminalState,Timestamp completedAt,List<String> routePolyLines, String ambulanceImageRef, String driverProfileImageRef){
        this.trip = trip;
        this.terminalState = terminalState;
        this.completedAt = completedAt;
        this.routePolyLines = routePolyLines;
        this.ambulanceImageRef = ambulanceImageRef;
        this.driverProfileImageRef = driverProfileImageRef;
    }

    public Trip getTrip() {
        return trip;
    }

    public Timestamp getCompletionTimestamp() {
        return completedAt;
    }

    public String getTerminalState() {
        return terminalState;
    }

    public List<String> getRoutePolyLines() {
        return routePolyLines;
    }


     public static TripHistory createFromMap(Map<String,Object> map){
        if (!map.containsKey("trip") || !map.containsKey("terminalState") || !map.containsKey("completionTimestamp")){
            throw new IllegalArgumentException("Kuch nhi ho sakta tera");
        }

        Object tripRec = map.get("trip");
        Trip trip = null;

        if(tripRec instanceof Map) {
            trip = Trip.createFromMap( (Map<String, Object>) tripRec);
        } else if(tripRec instanceof Trip) {
            trip = (Trip) map.get("trip");
        }

         assert trip != null;

         Object terminalStateRec = map.get("terminalState");

         String terminalState;

         if(terminalStateRec instanceof String) {
             terminalState = (String) terminalStateRec;
         } else {
             terminalState = ((TripStatus) terminalStateRec).name();
         }
        Timestamp completionTimestamp = (Timestamp) map.get("completionTimestamp");

         List<String> routePolylines = (List<String>) map.get("routePolyLines");

         String ambulanceImageRef = (String) map.get("ambulanceImageRef");
         String driverProfileImageRef = (String) map.get("driverProfileImageRef");

        return new TripHistory(trip,terminalState,completionTimestamp,routePolylines, ambulanceImageRef, driverProfileImageRef);

     }

    @NonNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TripHistory{");
        sb.append("trip=").append(trip);
        sb.append(", terminalState='").append(terminalState).append('\'');
        sb.append(", completedAt=").append(completedAt);
        sb.append(", routePolyLines=").append(routePolyLines);
        sb.append(", ambulanceImageRef='").append(ambulanceImageRef).append('\'');
        sb.append(", driverProfileImageRef='").append(driverProfileImageRef).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getAmbulanceImageRef() {
        return ambulanceImageRef;
    }

    public void setAmbulanceImageRef(String ambulanceImageRef) {
        this.ambulanceImageRef = ambulanceImageRef;
    }

    public String getDriverProfileImageRef() {
        return driverProfileImageRef;
    }

    public void setDriverProfileImageRef(String driverProfileImageRef) {
        this.driverProfileImageRef = driverProfileImageRef;
    }
}
