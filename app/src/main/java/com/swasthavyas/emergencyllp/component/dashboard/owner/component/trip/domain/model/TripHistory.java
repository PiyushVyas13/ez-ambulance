package com.swasthavyas.emergencyllp.component.dashboard.owner.component.trip.domain.model;

import com.google.firebase.Timestamp;
import com.swasthavyas.emergencyllp.util.types.TripStatus;

import java.util.Map;

public class TripHistory {

    private Trip trip ;
    private String terminalState;
    private Timestamp completedAt;
    private String routePolyLine;

    public TripHistory(){

    }

    public TripHistory(Trip trip,String terminalState,Timestamp completedAt,String routePolyLine){
        this.trip = trip;
        this.terminalState = terminalState;
        this.completedAt = completedAt;
        this.routePolyLine = routePolyLine;
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

    public String getRoutePolyLine() {
        return routePolyLine;
    }

     public static TripHistory createFromMap(Map<String,Object> map){
        if (!map.containsKey("trip") || !map.containsKey("terminalState") || !map.containsKey("completionTimestamp")){
            throw new IllegalArgumentException("Kuch nhi ho sakta tera");
        }

        Trip trip = (Trip) map.get("trip");
        String terminalState = ((TripStatus) map.get("terminalState")).name();
        Timestamp completionTimestamp = (Timestamp) map.get("completionTimestamp");

        return new TripHistory(trip,terminalState,completionTimestamp,"");

     }

}
