package com.example.liammc.yarn.core;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.liammc.yarn.CameraController;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.finders.NearbyChatFinder;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.finders.SearchPlaceFinder;
import com.example.liammc.yarn.notifications.TimeChangeReceiver;
import com.example.liammc.yarn.yarnPlace.ChatCreator;
import com.example.liammc.yarn.yarnPlace.InfoWindow;
import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.example.liammc.yarn.interfaces.FinderCallback;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.utility.PermissionTools;
import com.example.liammc.yarn.interfaces.ReadyListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    /*This is main Activity in the whole application and where the firebaseUser will spend most of their
    time. Here the firebaseUser will be searching and interacting with Yarn Places, using the map and
    transitioning to different parts of the application */

    public static int WHERE_TO_CODE =  0;
    private  final int PERMISSION_REQUEST_CODE = 1;
    private final String TAG = "MapsActivity";

    //Google Services
    GoogleMap mMap;
    PlacesClient placesClient;
    FusedLocationProviderClient mFusedLocationProviderClient;
    Geocoder geocoder;

    //Finders
    NearbyChatFinder nearbyChatFinder;
    SearchPlaceFinder searchPlaceFinder;

    //Map Data
    public LocalUser localUser;

    //User Interaction
    YarnPlace touchedYarnPlace;
    Notifier notifier;
    Recorder recorder;

    //UI
    Circle circle;
    SeekBar radiusBar;

    //Receivers
    TimeChangeReceiver timeChangeReceiver;

    //User Input
    CameraController cameraController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*This is run when the activity is created*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initialize some internal variables needed for certain processes
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        recorder = Recorder.getInstance();
        geocoder = new Geocoder(this,Locale.getDefault());

        initReceivers();
        initChannels();
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

        //Initialize Finders
        initUpNearByChatFinder();
        initUpSearchPlaceFinder();

        //Initialize User Input
        InitCameraController();

        //Initialize the Map UI
        initLocalUser();
        initMapUI();
        InitSeekBar();

        //Initialize the Map Listeners and services
        initMapListeners();
        initPlacesClient();

        //Focus on the firebaseUser and show the radius circle on the map
        InitSearchCircle();
        onFocusOnUserPressed(null);
    }

    @Override
    public void onBackPressed() {
        /*This is run when the firebaseUser presses back on their device. If the chat creator is showing
        * dismiss that, if the Yarn Place Info Window is showing dismiss that but if only the
        * map is showing go to the account activity*/

        if(touchedYarnPlace != null)
        {
            InfoWindow info = touchedYarnPlace.infoWindow;
            ChatCreator creator = info.chatCreator;

            if(creator.window.isShowing())
            {
                creator.dismiss();
            }
            else{
                info.dismiss();
            }
        }
        else{
            onAccountPressed(null);
        }
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

        if (requestCode == WHERE_TO_CODE) {
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
    }

    //region Init

    private void initMapListeners() {
        /*This method initializes all the needed map listeners*/

        initMarkerListener();
        initCameraMoveListener();
        initMapClickListener();
    }

    private void initLocalUser() {
        /*This method initializes the Local firebaseUser*/

        localUser = LocalUser.getInstance(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setLocationSource(localUser);
            mMap.setMyLocationEnabled(true);
        }
    }

    private void initPlacesClient(){

        //Initialize the Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources()
                    .getString(R.string.google_place_android_key));
        }
    }

    private void initReceivers(){

        timeChangeReceiver = new TimeChangeReceiver(this);
        registerReceiver(timeChangeReceiver.receiver,TimeChangeReceiver.intentFilter);
    }

    private void initChannels(){
        Notifier.getInstance().createNotificationChannel(this);
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

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment searchBar = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Get the country
        String country = getResources().getConfiguration().locale.getCountry();

        searchPlaceFinder = new SearchPlaceFinder(searchBar,country, new FinderCallback() {

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

        mMap.setOnMarkerClickListener( new OnMarkerClickListener(){

            @Override
            public boolean onMarkerClick(Marker marker) {
                /*Runs when the firebaseUser clicks on a marker*/

                //The firebaseUser has already touched a yarn place so we need to dismiss it
                if(touchedYarnPlace != null) {

                    if(touchedYarnPlace.infoWindow.window.isShowing()) {
                        touchedYarnPlace.infoWindow.dismiss();
                        touchedYarnPlace = null;
                    }
                }

                //Search for the marker to in the list to find the one the firebaseUser has touched
                touchedYarnPlace = searchMarkerInList(recorder.recordedYarnPlaces,marker);

                //Focus on the Yarn Place and show the info window
                if(touchedYarnPlace != null) {
                    focusOnYarnPlace(touchedYarnPlace);
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

                /*If the firebaseUser has touched a Yarn Place the application must update it's info window
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
                if(touchedYarnPlace != null && touchedYarnPlace.infoWindow.window.isShowing())
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

    private void InitSearchCircle(){
        /*Updates the circle on the Map*/

        if(circle != null )circle.remove();
        if(mMap == null){
            Log.e(TAG,"Couldn't draw circle because the Map is null");
            return;
        }
        if(localUser == null || localUser.lastLatLng == null){
            Log.e(TAG,"Couldn't draw circle because the Local user or their last LatLng is null");
            return;
        }

        circle = mMap.addCircle(new CircleOptions()
                .center(localUser.lastLatLng)
                .radius(localUser.SEARCH_RADIUS_DEFAULT)
                .strokeColor(R.color.searchRadiusStroke)
                .fillColor(R.color.searchRadiusFill));

    }

    private void InitSeekBar(){
        radiusBar = findViewById(R.id.radiusBar);

        radiusBar.setMax(LocalUser.SEARCH_RADIUS_MAX);
        radiusBar.setProgress(LocalUser.SEARCH_RADIUS_DEFAULT);

        cameraController.moveToLatLng(localUser.lastLatLng,(
                int)calculateCameraZoom(radiusBar.getProgress()));

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                Log.d(TAG,"The zoom of the camera is " + mMap.getCameraPosition().zoom);
                //Clamp the progress bar to a minimum
                if(progress < LocalUser.SEARCH_RADIUS_MIN){
                    progress = LocalUser.SEARCH_RADIUS_MIN;
                    seekBar.setProgress(progress);
                }

                localUser.searchRadius = progress;

                updateCircle(progress,localUser.lastLatLng);

                double zoom = calculateCameraZoom(seekBar.getProgress());
                cameraController.zoomTo((float)zoom);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    //endregion

    //region User Input

    private void InitCameraController(){
        cameraController =  new CameraController(mMap);
    }

    //endregion

    //endregion

    //region Getters and Setters

    public YarnPlace getTouchedYarnPlace(){return touchedYarnPlace;}

    //endregion

    //region Button Methods

    public void onRefreshButtonPressed(View view) {
        /*This method is run when the firebaseUser presses the refresh button*/

        if(localUser.lastLocation != null)
        {
            updateCircle(localUser.searchRadius,localUser.lastLatLng);

            //Get the chats with the selected types
            nearbyChatFinder.getNearbyChats(localUser.types);
            nearbyChatFinder.initNearbyChatsListener(localUser.types);
            Log.d(TAG,"Getting near by chats");
        }
    }

    public void onFocusOnUserPressed(View view) {
        /*Focus the camera on the firebaseUser*/

        localUser.getUserLocation(this, mFusedLocationProviderClient, new LocalUser.locationReceivedListener() {
            @Override
            public void onLocationReceived(LatLng latLng) {
                cameraController.moveToLatLng(latLng,
                        (int)calculateCameraZoom(radiusBar.getProgress()));
            }
        });
    }

    public void onChatPlannerPressed(View view) {
        /*This method goes to the Chat Planner activity when they press the Chat Planner Button*/

        Intent intent = new Intent(getBaseContext(),ChatPlannerActivity.class);
        startActivity(intent);
    }

    public void onNotificationsPressed(View view) {
        /*This method goes to the Notification activity when the firebaseUser presses the notification button*/

        Intent intent = new Intent(getBaseContext(),NotificationsActivity.class);
        startActivity(intent);
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
    //endregion

    //region Public Methods

    public Marker createMarker(Double lat, Double lng) {
        /*This method creates a marker at the given location*/

        //Set the marker options
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = new LatLng( lat, lng);
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        //Add the marker to the map and return it
        return mMap.addMarker(markerOptions);
    }

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

                    if(focus)focusOnYarnPlace(place);
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

    public void updateCircle(double radius, LatLng center){
        if(mMap == null){
            Log.e(TAG,"Couldn't draw circle because the Map is null");
            return;
        }
        if(localUser == null || localUser.lastLatLng == null){
            Log.e(TAG,"Couldn't draw circle because the Local user or their last LatLng is null");
            return;
        }
        if(circle == null ){
            Log.e(TAG,"Couldn't draw circle because it's null");
            return;
        }

        circle.setRadius(radius);
        circle.setCenter(center);
    }
    //endregion

    //region Private methods

    private YarnPlace createYarnPlace(HashMap<String,String> placeMap,final boolean focus) {
        /*Creates a new yarn Place Instance*/

        final MapsActivity mapsActivity = this;

        //Create the instance and then initialize it
        final YarnPlace yarnPlace = new YarnPlace(placeMap);
        yarnPlace.init(this,geocoder);

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
                if(focus)focusOnYarnPlace(yarnPlace);
            }
        });


        return yarnPlace;
    }

    private void focusOnYarnPlace(YarnPlace _touchedYarnPlace){

        if(touchedYarnPlace != null){
            if(touchedYarnPlace.infoWindow.window.isShowing()) touchedYarnPlace.infoWindow.dismiss();
        }

        touchedYarnPlace = _touchedYarnPlace;

        cameraController.moveToLatLng(touchedYarnPlace.marker.getPosition(),15);
        touchedYarnPlace.infoWindow.show(mMap);
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

    private double calculateCameraZoom(int progress ){

        double radiusPer = ((double) progress/(double) LocalUser.SEARCH_RADIUS_MAX) * (double) 100;
        double zoomPercantage =  100 - radiusPer;
        int zoomRange = CameraController.ZOOM_MAX - CameraController.ZOOM_MIN;

        return ((zoomPercantage * zoomRange)/100) + CameraController.ZOOM_MIN;
    }
    //endregion
}
