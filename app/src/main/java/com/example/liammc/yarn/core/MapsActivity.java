package com.example.liammc.yarn.core;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.finders.NearbyChatFinder;
import com.example.liammc.yarn.finders.SearchPlaceFinder;
import com.example.liammc.yarn.interfaces.FinderCallback;
import com.example.liammc.yarn.interfaces.ReadyListener;
import com.example.liammc.yarn.userInterface.CameraController;
import com.example.liammc.yarn.userInterface.LoadingSymbol;
import com.example.liammc.yarn.userInterface.RadiusBar;
import com.example.liammc.yarn.userInterface.SearchRadius;
import com.example.liammc.yarn.utility.PermissionTools;
import com.example.liammc.yarn.chats.ChatCreator;
import com.example.liammc.yarn.yarnPlace.InfoWindow;
import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends YarnActivity implements OnMapReadyCallback {
    /*This is main Activity in the whole application and where the firebaseUser will spend most of their
    time. Here the firebaseUser will be searching and interacting with Yarn Places, using the map and
    transitioning to different parts of the application */

    public static int WHERE_TO_CODE =  0;
    public static int PERMISSION_REQUEST_CODE = 1;
    public static int AUTOCOMPLETE_REQUEST_CODE= 2;
    public static int SUGGESTION_RESULT_CODE = 3;
    public static int NOTIFICATION_RESULT_CODE = 4;
    private final String TAG = "MapsActivity";

    //Google Services
    GoogleMap mMap;

    //Finders
    NearbyChatFinder nearbyChatFinder;
    SearchPlaceFinder searchPlaceFinder;

    //User Interaction
    YarnPlace touchedYarnPlace;

    //UI
    public SearchRadius searchRadius;
    CameraController cameraController;
    RadiusBar radiusBar;
    LoadingSymbol loadingSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*This is run when the activity is created*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /*This is run when the google map is ready for firebaseUser interaction*/
        mMap = googleMap;

        //Request permissions from the firebaseUser if needed
        PermissionTools.requestPermissions(this, PERMISSION_REQUEST_CODE);

        //Initialize all the recorded yarn places on the map
        for (YarnPlace place:recorder.recordedYarnPlaces) {
            place.initOnMap(this,mMap);
        }

        //Initialize the firebaseUser
        initLocalUser();

        if(!localUser.checkReady()){
            localUser.initUserLocation(this);
            localUser.setReadyListener(new ReadyListener() {
                @Override
                public void onReady() {

                }
            });
        }

        //Initialize the Map Listeners and services
        initMarkerListener();
        initCameraMoveListener();
        initMapClickListener();

        //Initialize Finders
        initUpNearByChatFinder();
        initUpSearchPlaceFinder();

        //Initialize UI
        searchRadius = new SearchRadius(mMap,nearbyChatFinder);
        radiusBar =  new RadiusBar(this,searchRadius);
        cameraController =  new CameraController(mMap);
        loadingSymbol =  new LoadingSymbol(this);

        radiusBar.init(cameraController);
        cameraController.init(radiusBar);

        //Initialize the Map UI
        initMapUI();

        //Focus on the firebaseUser and show the radius circle on the map
        onFocusOnUserPressed(null);
    }

    @Override
    public void onBackPressed() {
        /*This is run when the firebaseUser presses back on their device. If the chat creator is showing
        * dismiss that, if the Yarn Place Info Window is showing dismiss that but if only the
        * map is showing go to the account activity*/

        if(touchedYarnPlace != null) {
            InfoWindow info = touchedYarnPlace.infoWindow;
            ChatCreator creator = info.chatCreator;

            if(creator.isShowing()) creator.dismiss();

            else info.dismiss();
        }
        else onAccountPressed(null);
    }

    @Override
    public void onDestroy() {
        /*This is run when the Activity is destroyed. Before it is though this method deregisters
        the TimeChangeReceiver
         */

        unregisterReceiver(timeChangeReceiver.receiver);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == WHERE_TO_CODE) whereToResult(resultCode,data);
        else if (requestCode == AUTOCOMPLETE_REQUEST_CODE) searchResult(resultCode,data);
        else if (requestCode == SUGGESTION_RESULT_CODE) suggestionResult(resultCode,data);
        else if (requestCode == NOTIFICATION_RESULT_CODE) notificationResult(resultCode,data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    //region Init

    @Override
    protected void initLocalUser() {
        /*This method initializes the Local firebaseUser*/
        super.initLocalUser();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setLocationSource(localUser);
            mMap.setMyLocationEnabled(true);
        }
    }

    //region Finders

    private void initUpNearByChatFinder(){
        /*Initializes the Near By Chat Finder*/

        final Activity activity =  this;

        nearbyChatFinder = new NearbyChatFinder((int)localUser.searchRadius, new FinderCallback() {
            @Override
            public void onFoundPlaces(String nextPageToken, List<HashMap<String, String>> placeMaps) {
                /*This is called when the Nearby Chat Finder finds some places with chats*/

                Log.d(TAG,"Found Places with Chats - " + placeMaps.toString());
                addYarnPlaces(placeMaps);
            }

            @Override
            public void onFoundPlace(HashMap<String, String> placeMap){
                /*This is called when the Nearby Chat Finder finds a place with chats*/

                Log.d(TAG,"Found Place with Chats - " + placeMap.toString());
                addYarnPlace(placeMap,false);
            }

            @Override
            public void onNoPlacesFound(String message) {
                /*This is called when the Nearby Chat Finder doesn't find any places*/
                Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
            }
        });

        nearbyChatFinder.initNearbyChatsListener(localUser.types);
    }

    private void initUpSearchPlaceFinder(){
        /*Initializes the Search Place Finder*/
        final MapsActivity mapsActivity =  this;

        searchPlaceFinder = new SearchPlaceFinder(this,
                SearchPlaceFinder.FinderType.WIDGET,new FinderCallback() {

            @Override
            public void onFoundPlaces(String nextPageToken, List<HashMap<String, String>> placeMaps) {

            }

            @Override
            public void onFoundPlace(HashMap<String, String> placeMap){
                /*Called when the finder finds a place*/

                //Add it to the map
                final YarnPlace yarnPlace = addYarnPlace(placeMap,true);
                touchedYarnPlace = yarnPlace;
            }

            @Override
            public void onNoPlacesFound(String message) {
                Toast.makeText(mapsActivity,message,Toast.LENGTH_LONG).show();
            }
        });
    }

    //endregion

    //region Map Listeners

    private void initMarkerListener() {
        /*Initializes the Map Marker Listener*/

        mMap.setOnMarkerClickListener( new GoogleMap.OnMarkerClickListener(){

            @Override
            public boolean onMarkerClick(Marker marker) {
                /*Runs when the firebaseUser clicks on a marker*/

                //The firebaseUser has already touched a yarn place so we need to dismiss it
                if(touchedYarnPlace != null) {

                    if(touchedYarnPlace.infoWindow.isShowing()) {
                        touchedYarnPlace.infoWindow.dismiss();
                        touchedYarnPlace = null;
                    }
                }

                //Search for the marker to in the list to find the one the firebaseUser has touched
                touchedYarnPlace = searchMarkerInList(recorder.recordedYarnPlaces,marker);

                //Focus on the Yarn Place and show the info window
                if(touchedYarnPlace != null) {
                    cameraController.focusOnYarnPlace(touchedYarnPlace);
                    Log.d(TAG,"Found the correct Yarn place from marker click");
                }
                else{
                    Log.e(TAG,"Fatal error, couldn't find touched marker in Yarn places lists");
                }
                return true;
            }
        });
    }

    private void initCameraMoveListener() {
        /*Initializes the Camera Move listener*/

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                /*Runs when the camera moves*/

                /*If the firebaseUser has touched a Yarn Place the application must updateInfoWindow it's info window
                position*/
                if(touchedYarnPlace != null) {
                    if(!touchedYarnPlace.infoWindow.updatePosition(mMap)) touchedYarnPlace = null;
                }
            }
        });
    }

    private void initMapClickListener() {
       /*Initializes the Map click listener*/

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng){
                /*Runs when the firebaseUser clicks on the map*/

                //If a Yarn Place Info Window is showing then dismiss it
                if(touchedYarnPlace != null && touchedYarnPlace.infoWindow.isShowing())
                {
                    touchedYarnPlace.infoWindow.dismiss();
                    touchedYarnPlace = null;
                }
            }
        });
    }

    //endregion

    //region UI

    private void initMapUI() {
        /*Initialize the Map UI*/

        UiSettings uiSettings = mMap.getUiSettings();

        uiSettings.setCompassEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setZoomControlsEnabled(false);

        initMapStyle();
    }

    private void initMapStyle() {
        /*Initialize the Map style*/

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    //endregion

    //endregion

    //region Getters and Setters

    public YarnPlace getTouchedYarnPlace(){return touchedYarnPlace;}

    public void setTouchedYarnPlace(YarnPlace _touchedYarnPlace){touchedYarnPlace = _touchedYarnPlace;}

    //endregion

    //region Button Methods

    public void onRefreshButtonPressed(View view) {
        /*This method is run when the firebaseUser presses the refresh button*/

        if(localUser.lastLocation == null){
            localUser.getUserLocation(this, new FusedLocationProviderClient(this)
                    , new LocalUser.locationReceivedListener() {
                @Override
                public void onLocationReceived(LatLng latLng) {
                    refreashButtonResult();
                }
            });
        }
        else refreashButtonResult();
    }

    public void onFocusOnUserPressed(View view) {
        /*Focus the camera on the firebaseUser*/

        final MapsActivity mapsActivity = this;

        if(localUser.lastLatLng == null){
            localUser.getUserLocation(this, new FusedLocationProviderClient(this)
                    , new LocalUser.locationReceivedListener() {
                        @Override
                        public void onLocationReceived(LatLng latLng) {
                            cameraController.focusOnUser(latLng);
                        }
                    });
        }
        else cameraController.focusOnUser(localUser.lastLatLng);

    }

    public void onChatPlannerPressed(View view) {
        /*This method goes to the Chat Planner activity when they press the Chat Planner Button*/

        Intent intent = new Intent(getBaseContext(),ChatPlannerActivity.class);
        startActivityForResult(intent,SUGGESTION_RESULT_CODE);
    }

    public void onNotificationsPressed(View view) {
        /*This method goes to the Notification activity when the firebaseUser presses the notification button*/

        Intent intent = new Intent(getBaseContext(),NotificationsActivity.class);
        startActivityForResult(intent,NOTIFICATION_RESULT_CODE);
    }

    public void onAccountPressed(View view) {
        /*This method goes to the Account Activity when the firebaseUser presses the Account Button*/

        Intent intent = new Intent(getBaseContext(), AccountActivity.class);
        startActivity(intent);
    }

    public void onWhereToPressed(View view){

        Intent intent = new Intent(getBaseContext(), WhereToActivity.class);
        startActivityForResult(intent,WHERE_TO_CODE);
    }

    public void onSearchButtonPressed(View view){
        searchPlaceFinder.search(AUTOCOMPLETE_REQUEST_CODE);
    }
    //endregion

    //region Public Methods

    public YarnPlace addYarnPlace(HashMap<String,String> placeMap,Boolean focus){
        /*Create a Yarn Place Object from the passed placeMap and then adds it to the map and system.
        * This only happens if there isn't a YarnPlace of this description already present. So this
        * method also checks if the passed placeMap's ID isn't present in the list of recorded
        * Yarn Places*/

        //There are no yarn places so add them all
        if(recorder.recordedYarnPlaces.size() == 0) {

            return createYarnPlace(placeMap,focus);
        }
        //There are some yarn places so add the ones we need
        else{

            //Look for equal Yarn Places
            for(YarnPlace place: recorder.recordedYarnPlaces){

                if(place.placeMap.get("id").equals(placeMap.get(("id")))){

                    if(focus){
                        touchedYarnPlace = place;
                        cameraController.focusOnYarnPlace(place);
                    }
                    return place;
                }
            }

            return createYarnPlace(placeMap,focus);
        }
    }

    public void addYarnPlaces(List<HashMap<String,String>> placeMaps){
        /*Creates all the yarn places from the passed List of placeMap and then adds them to the map
        and system. This only happens if there isn't a YarnPlace of it's description already present.
        So this method also checks if the passed placeMap's ID isn't present in the list of recorded
        Yarn Places*/


        //There are no yarn places so add them all
        if(recorder.recordedYarnPlaces.size() == 0) {

            for(HashMap<String,String> placeMap : placeMaps) {
                createYarnPlace(placeMap,false);
            }
        }
        //There are some yarn places so add the ones we need
        else{
            //Loop over our placeMaps
            for(HashMap<String,String> placeMap : placeMaps)
            {
                boolean equal = false;
                //Look for equal Yarn Places
                for(YarnPlace place: recorder.recordedYarnPlaces){

                    if(place.placeMap.get("id").equals(placeMap.get(("id")))){
                        equal = true;
                        break;
                    }
                }
                if(!equal){
                    createYarnPlace(placeMap,false);
                }
            }
        }
    }

    public void goToChat(String chatID){

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatID",chatID);
        startActivity(intent);

    }

    //endregion

    //region Private methods

    private YarnPlace createYarnPlace(HashMap<String,String> placeMap,final boolean focus) {
        /*Creates a new yarn Place Instance*/

        final MapsActivity mapsActivity = this;

        //Create the instance and then initialize it
        final YarnPlace yarnPlace = new YarnPlace(placeMap);
        yarnPlace.init(this,geocoder);

        loadingSymbol.start();

        yarnPlace.setReadyListener(new ReadyListener() {
            @Override
            public void onReady() {
                /*Once the newly created yarn place is created show it on the map, record it and
                set the chat Value Change listener so that the application knows when those chats
                change under that Yarn Place*/

                //Show on the map and record
                yarnPlace.initOnMap(mapsActivity,mMap);
                recorder.recordYarnPlace(yarnPlace);

                //Initialize the value change listeners on all the chats
                ArrayList<Chat> chats = yarnPlace.getChats();
                if(chats != null && chats.size() > 0) {
                    for (Chat c :chats) {
                        c.updator.initChangeListener(mapsActivity);
                    }
                }

                //focus on the Yarn Place
                if(focus){
                    touchedYarnPlace = yarnPlace;
                    cameraController.focusOnYarnPlace(yarnPlace);
                }

                loadingSymbol.stop();
            }
        });


        return yarnPlace;
    }

    private YarnPlace searchMarkerInList(List<YarnPlace> yarnPlaces, Marker toSearch) {
        /*Loops over the passed List of Yarn PLaces and then checks for the equality against the
         * passed marker instance and the marker instance in the Yarn Place. If there is a match
         * that Yarn Place is returned*/

        //Loop through all places to find marker equality
        if( yarnPlaces != null) {
            for (int i = 0; i < yarnPlaces.size(); i++) {
                if(yarnPlaces.get(i).marker.equals(toSearch)) return yarnPlaces.get(i);
            }
        }
        return null;
    }

    private void whereToResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            try{
                HashMap<String, String> placeMap =
                        (HashMap<String, String>) data.getSerializableExtra("placeMap");

                addYarnPlace(placeMap,true);
            }catch(ClassCastException e){
                Log.e(TAG,"Couldn't cast place map from result - " + e.getMessage());
            }
        }
    }

    private void searchResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);

            HashMap<String,String> placeMap = searchPlaceFinder.parsePlaceSearch(place);
            if(placeMap == null) searchPlaceFinder.listener.onNoPlacesFound("Sorry you can't create a chat here");
            else searchPlaceFinder.listener.onFoundPlace(placeMap);

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            searchPlaceFinder.listener.onNoPlacesFound("There was an error selecting this place");
        }
    }

    private void suggestionResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK) {

            String placeId = data.getStringExtra("placeID");
            YarnPlace selectedPlace = recorder.getYarnPlace(placeId);

            if(selectedPlace != null){
                touchedYarnPlace =  selectedPlace;
                cameraController.focusOnYarnPlace(selectedPlace);
            }

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Log.e(TAG,"There was an error trying to focus on the place after " +
                    "suggestion selection");
        }
    }

    private void notificationResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK) {

            String placeID =  data.getStringExtra("placeID");
            String chatID =  data.getStringExtra("chatID");

            YarnPlace selectedPlace = recorder.getYarnPlace(placeID);
            Chat selectedChat = recorder.getRecordedChat(chatID);

            //The Yarn place isn't null
            if(selectedPlace != null){

                //The chat is null so just focus on the Yarn Place
                if(selectedChat == null){
                    touchedYarnPlace =  selectedPlace;
                    cameraController.focusOnYarnPlace(selectedPlace);
                }
                //The chat isn't null so go to the chat
                else goToChat(selectedChat.chatID);
            }

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Log.e(TAG,"There was an error trying to focus on the place after " +
                    "Notification selection");
        }
    }

    private void refreashButtonResult(){
        searchRadius.update(localUser.searchRadius,localUser.lastLatLng);

        //Get the chats with the selected types
        nearbyChatFinder.getNearbyChats(localUser.types);
        nearbyChatFinder.initNearbyChatsListener(localUser.types);
        Log.d(TAG,"Getting near by chats");
    }
    //endregion

}
