package com.swasthavyas.emergencyllp.network.model;

import com.google.gson.annotations.SerializedName;

public class GoogleMapsDirectionsResponse {

    @SerializedName("routes")
    public Route[] routes;

    public static class Route {
        @SerializedName("distanceMeters")
        public int distanceMeters;
        @SerializedName("duration")
        public String duration;
        @SerializedName("polyline")
        public PolylineWrapper polyline;

    }

    public static class PolylineWrapper {
        @SerializedName("encodedPolyline")
        public String encodedPolyline;
    }
}
