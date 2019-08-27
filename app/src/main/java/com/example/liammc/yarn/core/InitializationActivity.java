package com.example.liammc.yarn.core;

import android.app.Activity;
import android.content.Intent;
import android.location.Geocoder;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.liammc.yarn.finders.NearbyChatFinder;
import com.example.liammc.yarn.networking.JoinedDownloader;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.notifications.TimeChangeReceiver;
import com.example.liammc.yarn.yarnPlace.ChatCreator;
import com.example.liammc.yarn.yarnPlace.InfoWindow;
import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.example.liammc.yarn.interfaces.FinderCallback;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.interfaces.ReadyListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class InitializationActivity extends YarnActivity {
    /*This Activity is used for initializes application systems and downloading needed data. This
    * activity must be run first before the firebaseUser is taken to the MapsActivity otherwise the
    * application may not function as expected. The Initialization Activity runs through like this.
    * Each step must wait for the previous one to finish first as they are dependent on each other.
    * This is achieved through ready listeners.
    *
    *   1 - Initialize the Local User
    *   2 - Initialize the Recorder and Notifier
    *   3 - Downloaded all joined Chats
    *   4 - Initialize the Nearby Chat Finder and download any near by chats
    *   5 - Initialize all found Yarn Places with nearby Chats
    *   7 - Go to the map*/

    private final int SEARCH_RADIUS = 1000;
    private String TAG =  "InitializationActivity";
    JoinedDownloader joinedDownloader;
    NearbyChatFinder nearbyChatFinder;

    //UI
    TextView progressMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initilization);

        //Initialize the required objects and the Local firebaseUser
        init();
        initLocalUser();
    }

    @Override
    public void onBackPressed() {
        //Stops the user from going back
    }

    //region Init

    public void init(){
        joinedDownloader = new JoinedDownloader();
        progressMessage =  findViewById(R.id.progressMessage);
    }

    @Override
    public void initLocalUser(){
        /*Initializes the Local User and it's ready listener*/

        //Initializes the Local User's variables and systems
        //localUser.initDatabaseReferences(userAuth.getUid());
        super.initLocalUser();

        localUser.initUser();
        localUser.initTypes();

        localUser.initUserLocation(this);
        localUser.getUserLocation(this, locationProviderClient, null);

        //Initialize the Local User's Ready Listener
        initLocalUserReadyListener();
    }

    private void initLocalUserReadyListener(){

        updateProgressMessage("Setting up User");

        //Set the ready listener
        localUser.setReadyListener(new ReadyListener() {
            @Override
            public void onReady() {
                /*This runs once the Local firebaseUser is ready*/

                initNotifier();
                initRecorder();

                downloadJoinedYarnPlaces();
            }
        });
    }

    @Override
    protected void initRecorder(){
        super.initRecorder();

        recorder.initPlaceClient(this);
        recorder.chatList.clear();
        recorder.recordedYarnPlaces.clear();
        recorder.recordedChats.clear();
    }

    private void initNearbyChatFinder(){
        /*This method initializes the Nearby Chat Finder*/

        final Activity activity = this;

        FinderCallback listener = new FinderCallback() {
            @Override
            public void onFoundPlaces(String nextPageToken, final List<HashMap<String, String>> placeMaps) {
                /*Runs when the Nearby Chat Finder has found multiple chats that are close to the
                firebaseUser*/

                /*Set local variables that are used to track when all the required Yarn Places have
                been initialized and are ready*/
                final List<YarnPlace> addedPlaces = new ArrayList<>();

                //Loos over the place maps and begin to initialize each Yarn Place
                for (final HashMap<String, String> placeMap:placeMaps) {

                    /*Checks if this Yarn place already exists in the system. If it does then we
                    skip over it*/
                    if(recorder.getYarnPlace(placeMap.get("id")) != null){
                        Log.d(TAG,"Existing place");
                        continue;
                    }

                    //Create a new a new Yarn Place Instance and initialize it internally
                    final YarnPlace nearbyPlace = new YarnPlace(placeMap);
                    nearbyPlace.init(activity,geocoder);
                    nearbyPlace.initYarnPlaceUpdater(activity);

                    //Record the Yarn Place
                    recorder.recordYarnPlace(nearbyPlace);

                    //Sets the newly created Yarn Place's ready listener
                    nearbyPlace.setReadyListener(new ReadyListener() {
                        @Override
                        public void onReady() {
                            /*Runs when the Yarn Place is ready for interaction. When its ready we
                             * add it to the local added List and then compare its size to the
                             * passed List of placeMaps from onFoundPlaces. So only ready Yarn places
                             * are put into the added list. The application knows when all the Yarn
                             * Places are ready when both the added List and the placeMaps List sizes
                             * are the same*/

                            //add the yarn Place
                            addedPlaces.add(nearbyPlace);

                            //Compare the size of the added yarnPlaces and the size of the placeMaps
                            if(addedPlaces.size() == placeMaps.size()){
                                /*When all the found Yarn Places the application is ready for firebaseUser
                                interaction and we can go to the Map*/

                                Log.d(TAG,"Nearby chat finder is ready");
                                goToMap();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFoundPlace(HashMap<String, String> placeMap){
                /*Runs when the Nearby Chat Finder has found chats that are close to the
                firebaseUser*/

                //Create a new Yarn Place instance and initialize it internally
                final YarnPlace nearbyPlace = new YarnPlace(placeMap);
                nearbyPlace.init(activity,geocoder);

                //Record the Yarn Place
                recorder.recordYarnPlace(nearbyPlace);

                //Set the Yarn Place ready Listener
                nearbyPlace.setReadyListener(new ReadyListener() {
                    @Override
                    public void onReady() {
                        /*Runs when the Yarn Place is ready. Once its ready the application takes
                         * the firebaseUser to the Map*/

                        Log.d(TAG,"Yarn Place is ready");
                        goToMap();
                    }
                });
            }

            @Override
            public void onNoPlacesFound(String message) {
                /*Runs when no places were found so go to the map because there are no YarnPlaces
                 * and therefor not Chats to initialize*/
                goToMap();
            }
        };

        nearbyChatFinder = new NearbyChatFinder(SEARCH_RADIUS, listener);
    }

    //endregion

    //region Public Methods

    public void reinitialize(){
        //Reset the initialization process if the internet listener
        // has detected a recent connection

        try{

            Log.d(TAG,"Reinitializing");
            if(!isFinishing()){
                init();
                initLocalUser();
            }
        }catch(Exception e){
            Log.e(TAG,e.getMessage());
        }
    }

    //endregion

    //region Private Methods

    private void updateProgressMessage(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    progressMessage.setText(message);
                } catch (Exception ex) {
                    // Here we are logging the exception to see why it happened.
                    Log.e(TAG, ex.toString());
                }
            }
        });
    }

    private void downloadJoinedYarnPlaces(){
        //Downloads all the Yarn Places with joined chats
        final Activity activity =  this;
        updateProgressMessage("Getting needed data");

        joinedDownloader.getJoinedYarnPlaces(activity,geocoder,new ReadyListener() {
            @Override
            public void onReady() {
                /*This runs once the joined Downloader is ready and has downloaded all of it's data.
                * Then the application must get all the chat data with the nearby Chat Finder*/
                updateProgressMessage("Getting nearby Chats");


                initNearbyChatFinder();
                nearbyChatFinder.getNearbyChats(localUser.types);
                nearbyChatFinder.initNearbyChatsListener(localUser.types);
            }
        });
    }

    private void goToMap() {
        nearbyChatFinder.adminRef.removeEventListener(nearbyChatFinder.adminRefListener);
        nearbyChatFinder.listener = null;

        Intent myIntent = new Intent(getBaseContext(),   MapsActivity.class);
        startActivity(myIntent);
    }

    //endregion
}
