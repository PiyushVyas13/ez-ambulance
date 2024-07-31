package com.swasthavyas.emergencyllp.network.retrofit.model;

public class GetDirectionsRouteRequest {
    private final String origin;
    private final String destination;

    public GetDirectionsRouteRequest(String origin, String destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public String getOrigin(){return origin;}
    public String getDestination(){return destination;}
}
