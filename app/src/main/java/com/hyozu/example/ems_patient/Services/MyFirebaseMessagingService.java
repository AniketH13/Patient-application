package com.hyozu.example.ems_patient.Services;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hyozu.example.ems_patient.Common.Common;
import com.hyozu.example.ems_patient.Model.EventBus.DeclineRequestAndRemoveTripFromDriver;
import com.hyozu.example.ems_patient.Model.EventBus.DeclineRequestFromDriver;
import com.hyozu.example.ems_patient.Model.EventBus.DriverAcceptTripEvent;
import com.hyozu.example.ems_patient.Model.EventBus.DriverCompleteTripEvent;
import com.hyozu.example.ems_patient.Utils.UserUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (FirebaseAuth.getInstance().getCurrentUser() !=null)
            UserUtils.updateToken(this,s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String,String> dataRecv = remoteMessage.getData();
        if (dataRecv !=null) {
            if (dataRecv.get(Common.NOTI_TITLE) != null) {

                if (dataRecv.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_DECLINE)) {
                    EventBus.getDefault().postSticky(new DeclineRequestFromDriver());
                }

                else if (dataRecv.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_DECLINE_AND_REMOVE_TRIP)) {
                        EventBus.getDefault().postSticky(new DeclineRequestAndRemoveTripFromDriver());
                    }

                else if (dataRecv.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_ACCEPT)) {

                    String tripKey = dataRecv.get(Common.TRIP_KEY);
                    EventBus.getDefault().postSticky(new DriverAcceptTripEvent(tripKey));
                }
                else if (dataRecv.get(Common.NOTI_TITLE).equals(Common.PATIENT_COMPLETE_TRIP)) {

                    String tripKey = dataRecv.get(Common.TRIP_KEY);
                    EventBus.getDefault().postSticky(new DriverCompleteTripEvent(tripKey));
                }
                    else
            Common.showNotification(this, new Random().nextInt(),
                    dataRecv.get(Common.NOTI_TITLE),
                    dataRecv.get(Common.NOTI_CONTENT),
                    null);
             }
        }
    }
}
