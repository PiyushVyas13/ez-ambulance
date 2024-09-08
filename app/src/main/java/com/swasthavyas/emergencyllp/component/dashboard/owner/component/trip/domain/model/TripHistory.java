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


    public TripHistory(){
        // required public empty constructor
    }

    /**
     * @param trip copy of trip object that was recorded as history
     * @param terminalState last state after the trip got removed from RTDB
     * @param completedAt completion time of the trip
     * @param routePolyLines encoded polyline from pickup to drop location
     */
    public TripHistory(Trip trip,String terminalState,Timestamp completedAt,List<String> routePolyLines){
        this.trip = trip;
        this.terminalState = terminalState;
        this.completedAt = completedAt;
        this.routePolyLines = routePolyLines;
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

        return new TripHistory(trip,terminalState,completionTimestamp,routePolylines);

     }

    @NonNull
    @Override
    public String toString() {
        return "TripHistory{" + "trip=" + trip +
                ", terminalState='" + terminalState + '\'' +
                ", completedAt=" + completedAt +
                ", routePolyLines='" + routePolyLines + '\'' +
                '}';
    }
}
