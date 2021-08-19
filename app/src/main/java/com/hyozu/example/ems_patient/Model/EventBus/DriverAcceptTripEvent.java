package com.hyozu.example.ems_patient.Model.EventBus;

public class DriverAcceptTripEvent {
    private String tripId;

    public DriverAcceptTripEvent(String tripId) {
        this.tripId = tripId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
