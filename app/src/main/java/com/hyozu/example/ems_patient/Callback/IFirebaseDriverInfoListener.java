package com.hyozu.example.ems_patient.Callback;

import com.hyozu.example.ems_patient.Model.DriverGeoModel;

public interface IFirebaseDriverInfoListener {
    void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel);
}
