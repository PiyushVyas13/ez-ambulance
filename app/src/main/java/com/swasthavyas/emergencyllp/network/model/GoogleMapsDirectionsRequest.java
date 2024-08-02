package com.swasthavyas.emergencyllp.network.model;

import com.google.gson.annotations.SerializedName;

public class GoogleMapsDirectionsRequest {

    @SerializedName("origin")
    public LocationWrapper origin;
    @SerializedName("destination")
    public LocationWrapper destination;

    @SerializedName("travelMode")
    public String travelMode;
    @SerializedName("routingPreference")
    public String routingPreference;
    @SerializedName("computeAlternativeRoutes")
    public boolean computeAlternativeRoutes;

    @SerializedName("routeModifiers")
    public RouteModifier routeModifiers;

    @SerializedName("languageCode")
    public String languageCode;

    @SerializedName("units")
    public String units;

    public static class LocationWrapper {
        @SerializedName("location")
        public CoordinateWrapper location;
    }

    public static class CoordinateWrapper {
        @SerializedName("latLng")
        public LatLngWrapper latLng;
    }

    public static class LatLngWrapper {
        @SerializedName("latitude")
        public double latitude;
        @SerializedName("longitude")
        public double longitude;
    }

    public static class RouteModifier {
        @SerializedName("avoidTolls")
        public boolean avoidTolls;
        @SerializedName("avoidHighways")
        public boolean avoidHighways;
        @SerializedName("avoidFerries")
        public boolean avoidFerries;
    }
}
