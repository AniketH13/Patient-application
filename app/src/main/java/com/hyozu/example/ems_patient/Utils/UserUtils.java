package com.hyozu.example.ems_patient.Utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hyozu.example.ems_patient.Common.Common;
import com.hyozu.example.ems_patient.Model.DriverGeoModel;
import com.hyozu.example.ems_patient.Model.EventBus.SelectePlaceEvent;
import com.hyozu.example.ems_patient.Model.FCMResponse;
import com.hyozu.example.ems_patient.Model.FCMSendData;
import com.hyozu.example.ems_patient.Model.TokenModel;
import com.hyozu.example.ems_patient.R;
import com.hyozu.example.ems_patient.Remote.IFCMService;
import com.hyozu.example.ems_patient.Remote.RetrofitFCMClient;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class UserUtils {
    public static void updateUser (View view, Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.Patient_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Snackbar.make(view,e.getMessage(),Snackbar.LENGTH_SHORT).show())
                .addOnSuccessListener(aVoid -> Snackbar.make(view,"Update information successfully!",Snackbar.LENGTH_SHORT).show());
    }

    public static void updateToken(Context context, String token) {
        TokenModel tokenModel = new TokenModel(token);

        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(e -> {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
    }

    public static void sendRequestToDriver(Context context, RelativeLayout main_layout, DriverGeoModel foundDriver, SelectePlaceEvent selectePlaceEvent) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        Log.d("TOKEN", FirebaseDatabase.getInstance().getReference(Common.TOKEN_REFERENCE).child(foundDriver.getKey()).toString());

        //Get token
        FirebaseDatabase
                .getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(foundDriver.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot databaseSnapshot) {
                        if (databaseSnapshot.exists())
                        {
                            TokenModel tokenModel = databaseSnapshot.getValue(TokenModel.class);

                            Map<String,String> notificationData = new HashMap<>();
                            notificationData.put(Common.NOTI_TITLE,Common.REQUEST_DRIVER_TITLE);
                            notificationData.put(Common.NOTI_CONTENT, "This message represent for request driver action");
                            notificationData.put(Common.PATIENT_KEY,FirebaseAuth.getInstance().getCurrentUser().getUid());

                            notificationData.put(Common.PATIENT_PICKUP_LOCATION_STRING,selectePlaceEvent.getOriginAddress());
                            notificationData.put(Common.PATIENT_PICKUP_LOCATION,new StringBuilder("")
                            .append(selectePlaceEvent.getOrigin().latitude)
                            .append(",")
                            .append(selectePlaceEvent.getOrigin().longitude)
                            .toString());

                            notificationData.put(Common.PATIENT_DESTINATION_STRING,selectePlaceEvent.getDestinationAddress());
                            notificationData.put(Common.PATIENT_DESTINATION,new StringBuilder("")
                                    .append(selectePlaceEvent.getDestination().latitude)
                                    .append(",")
                                    .append(selectePlaceEvent.getDestination().longitude)
                                    .toString());

                            //information
                            notificationData.put(Common.PATIENT_DISTANCE_VALUE,String.valueOf(selectePlaceEvent.getDistanceValue()));
                            notificationData.put(Common.PATIENT_TIME_VALUE,String.valueOf(selectePlaceEvent.getDurationValue()));
                            notificationData.put(Common.PATIENT_TOTAL_FARE,String.valueOf(selectePlaceEvent.getTotalFare()));

                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(),notificationData);

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<FCMResponse>() {
                                @Override
                                public void accept(FCMResponse fcmResponse) throws Exception {
                                    if (fcmResponse.getSuccess() == 0) {
                                        compositeDisposable.clear();
                                        Snackbar.make(main_layout, context.getString(R.string.request_driver_failed), Snackbar.LENGTH_LONG).show();
                                    }

                                }
                            }, throwable -> {
                                compositeDisposable.clear();
                                Snackbar.make(main_layout,throwable.getMessage(),Snackbar.LENGTH_LONG).show();
                            }));

                        }
                        else
                        {
                            Snackbar.make(main_layout,context.getString(R.string.token_not_found),Snackbar.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Snackbar.make(main_layout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}
