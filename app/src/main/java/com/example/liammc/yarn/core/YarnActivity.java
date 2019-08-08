package com.example.liammc.yarn.core;

import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.notifications.TimeChangeReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class YarnActivity extends AppCompatActivity {

    private final String TAG = "YarnActivity";

    protected LocalUser localUser;
    protected TimeChangeReceiver timeChangeReceiver;
    protected Recorder recorder;
    protected Notifier notifier;
    protected FirebaseAuth userAuth;
    FusedLocationProviderClient locationProviderClient;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initGeocoder();
        initLocationProviderClient();
        initFirebaseAuth();
        if(!(this instanceof InitializationActivity) &&
                !(this instanceof  MapsActivity)) initLocalUser();
        initReceivers();
        initNotifier();
        if(!(this instanceof InitializationActivity)) initRecorder();
    }

    //region Private Methods

    protected void initLocalUser(){

        localUser = LocalUser.getInstance();
        localUser.initUserAuth(userAuth);
    }

    protected void initFirebaseAuth(){
        userAuth = FirebaseAuth.getInstance();
    }

    protected void initReceivers(){

        try {
            //Registers the time change receiver
            timeChangeReceiver = new TimeChangeReceiver(this);
            registerReceiver(timeChangeReceiver.receiver, TimeChangeReceiver.intentFilter);
        }catch(IllegalArgumentException e){
            Log.d(TAG,"The Time Change Receiver is already registered");
        }
    }

    protected void initRecorder(){
        recorder = Recorder.getInstance();
    }

    protected void initNotifier() {
        notifier = Notifier.getInstance();
        notifier.createNotificationChannel(this);
    }

    protected void initLocationProviderClient(){
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    protected void initGeocoder(){
        geocoder = new Geocoder(this, Locale.getDefault());
    }
    //endregion

}

