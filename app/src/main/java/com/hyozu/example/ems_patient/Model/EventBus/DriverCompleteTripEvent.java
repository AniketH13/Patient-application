package com.hyozu.example.ems_patient.Model.EventBus;

public class DriverCompleteTripEvent {
    private String tripkey;

    public DriverCompleteTripEvent(String tripkey) {
        this.tripkey = tripkey;
    }

    public String getTripkey() {
        return tripkey;
    }

    public void setTripkey(String tripkey) {
        this.tripkey = tripkey;
    }
}
