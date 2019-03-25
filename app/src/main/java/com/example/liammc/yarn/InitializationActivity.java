package com.example.liammc.yarn;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liammc.yarn.Events.NearbyChatFinder;
import com.example.liammc.yarn.Events.NearbyPlaceFinder;
import com.example.liammc.yarn.Events.Notifier;
import com.example.liammc.yarn.Events.YarnPlace;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.utility.ReadyListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class InitializationActivity extends AppCompatActivity {

    private final int SEARCH_RADIUS = 1000;
    private String TAG =  "InitializationActivity";
    FusedLocationProviderClient mFusedLocationProviderClient;
    Geocoder geocoder;
    FirebaseAuth userAuth;
    YarnUser localUser;
    Notifier notifier;
    Recorder recorder;
    NearbyChatFinder nearbyChatFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initilization);

        final Context context =  this;

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        initializeLocalUser();

        setProgressMessage("Setting up user");
        Log.d(TAG,"Setting up user");

        localUser.setReadyListener(new ReadyListener() {
            @Override
            public void onReady() {

                initializeNotifier();
                initializeRecorder();

                setProgressMessage("Getting needed data");
                Log.d(TAG,"Getting needed data");
                recorder.getJoinedYarnPlaces(context,geocoder);
                recorder.setReadyListener(new ReadyListener() {
                    @Override
                    public void onReady() {
                        setProgressMessage("Getting nearby Chats");

                        ArrayList<String> types = new ArrayList<>();
                        types.add(YarnPlace.PlaceType.CAFE);
                        types.add(YarnPlace.PlaceType.RESTAURANT);

                        setUpNearbyChatFinder();
                        nearbyChatFinder.getNearbyChats(types);
                        nearbyChatFinder.setNearbyChatsListener(types);
                    }
                });
            }
        });
    }

    private void initializeLocalUser(){
        userAuth = FirebaseAuth.getInstance();

        localUser = LocalUser.getInstance().user;
        localUser.setAuth(userAuth);
        localUser.setUpUserLocation(this);
        localUser.getUserLocation(this,mFusedLocationProviderClient,null);
    }

    private void initializeNotifier(){
        notifier = Notifier.getInstance();
        registerReceiver(notifier.timeChangeReceiver,notifier.intentFilter);
    }

    private void initializeRecorder(){
        recorder = Recorder.getInstance();
        recorder.setLocalUser(localUser);
        recorder.initPlaceClient(this);
    }

    private void setUpNearbyChatFinder(){

        final Context context = this;

        nearbyChatFinder = new NearbyChatFinder(SEARCH_RADIUS, new FinderCallback() {
            @Override
            public void onFoundPlaces(String nextPageToken, List<HashMap<String, String>> placeMaps) {

                final List<YarnPlace> addedPlaces = new ArrayList<>();
                int existing = 0;

                for (final HashMap<String, String> placeMap:placeMaps) {

                    if(recorder.getYarnPlace(placeMap.get("id")) != null){
                        existing++;
                        Log.d(TAG,"Exsisting place");
                        continue;
                    }

                    final YarnPlace nearbyPlace = new YarnPlace(placeMap);
                    nearbyPlace.init(context,geocoder);
                    nearbyPlace.initChatUpdater(context);

                    recorder.recordYarnPlace(nearbyPlace);

                    nearbyPlace.setReadyListener(new ReadyListener() {
                        @Override
                        public void onReady() {
                            addedPlaces.add(nearbyPlace);

                            if(addedPlaces.size() == placeMap.size()){
                                Log.d(TAG,"Nearby chat finder is ready");
                                goToMap();
                            }
                        }
                    });
                }

                Log.d(TAG,String.valueOf(existing));
                if(existing == placeMaps.size())goToMap();
            }

            @Override
            public void onFoundPlace(HashMap<String, String> placeMap){
                final YarnPlace nearbyPlace = new YarnPlace(placeMap);
                nearbyPlace.init(context,geocoder);
                nearbyPlace.initChatUpdater(context);

                recorder.recordYarnPlace(nearbyPlace);

                nearbyPlace.setReadyListener(new ReadyListener() {
                    @Override
                    public void onReady() {
                        Log.d(TAG,"Nearby chat finder is ready");
                        goToMap();
                    }
                });
            }

            @Override
            public void onNoPlacesFound(String message) {
                goToMap();
            }
        });
    }

    private void goToMap() {
        Intent myIntent = new Intent(getBaseContext(),   MapsActivity.class);
        startActivity(myIntent);
    }

    private void setProgressMessage(String message){

        TextView progressMessage = findViewById(R.id.progressMessage);

        progressMessage.setText(message);
    }
}
