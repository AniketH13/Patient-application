package com.hyozu.example.ems_patient.Model.EventBus;

import com.google.android.gms.maps.model.LatLng;

public class SelectePlaceEvent {
    private LatLng origin,destination;
    private String originAddress,destinationAddress;
    private int distanceValue,durationValue;
    private double totalFare;

    public SelectePlaceEvent(LatLng origin, LatLng destination, String originAddress, String destinationAddress) {
        this.origin = origin;
        this.destination = destination;
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(String originAddress) {
        this.originAddress = originAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public int getDistanceValue() {
        return distanceValue;
    }

    public void setDistanceValue(int distanceValue) {
        this.distanceValue = distanceValue;
    }

    public int getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(int durationValue) {
        this.durationValue = durationValue;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public String getOriginString() {
        return new StringBuilder()
                .append(origin.latitude)
                .append(",")
                .append(origin.longitude)
                .toString();
    }

    public String getDestinationString() {
        return new StringBuilder()
                .append(destination.latitude)
                .append(",")
                .append(destination.longitude)
                .toString();
    }
}
