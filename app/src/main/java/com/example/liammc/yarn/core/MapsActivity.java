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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.finders.NearbyChatFinder;
import com.example.liammc.yarn.finders.NearbyPlaceFinder;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.finders.SearchPlaceFinder;
import com.example.liammc.yarn.notifications.TimeChangeReceiver;
import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.example.liammc.yarn.interfaces.FinderCallback;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.utility.PermissionTools;
import com.example.liammc.yarn.yarnPlace.PlaceType;
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


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{
    /*This is main Activity in the whole application and where the firebaseUser will spend most of their
    time. Here the firebaseUser will be searching and interacting with Yarn Places, using the map and
    transitioning to different parts of the application */

    private  final int PERMISSION_REQUEST_CODE = 1;
    private final int SEARCH_RADIUS = 1000;
    private final String TAG = "MapsActivity";

    //Google Services
    GoogleMap mMap;
    PlacesClient placesClient;
    FusedLocationProviderClient mFusedLocationProviderClient;
    Geocoder geocoder;

    //Finders
    NearbyPlaceFinder nearbyPlaceFinder;
    NearbyChatFinder nearbyChatFinder;
    SearchPlaceFinder searchPlaceFinder;

    //Map Data
    public LocalUser localUser;

    //User Interaction
    YarnPlace touchedYarnPlace;
    Notifier notifier;
    Recorder recorder;

    //UI
    CheckBox barCheckBox;
    CheckBox cafeCheckBox;
    CheckBox restaurantCheckBox;
    CheckBox nightClubCheckBox;
    Circle circle;

    //Receivers
    TimeChangeReceiver timeChangeReceiver;

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
        timeChangeReceiver = new TimeChangeReceiver();
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

        //Initialize Finders
        initUpNearByChatFinder();
        initUpNearByPlaceFinder();
        initUpSearchPlaceFinder();

        //Initialize the firebaseUser and the Map UI
        initLocalUser();
        initMapUI();

        //Initialize the Map Listeners and services
        initMapListeners();
        initMapServices();

        //Focus on the firebaseUser and show the radius circle on the map
        onFocusOnUserPressed(null);
        updateCircle();
    }

    @Override
    public void onBackPressed() {
        /*This is run when the firebaseUser presses back on their device. If the chat creator is showing
        * dismiss that, if the Yarn Place Info Window is showing dismiss that but if only the
        * map is showing go to the account activity*/

        if(touchedYarnPlace != null)
        {
            if(touchedYarnPlace.chatCreator.window.isShowing())
            {
                touchedYarnPlace.chatCreator.dismiss();
            }
            else{
                touchedYarnPlace.dismissInfoWindow();
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

    //region Init

    private void initMapListeners() {
        /*This method initializes all the needed map listeners*/

        initMarkerListener();
        initCameraMoveListener();
        initMapClickListener();
    }

    private void initLocalUser() {
        /*This method initializes the Local firebaseUser*/

        localUser = LocalUser.getInstance();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setLocationSource(localUser);
            mMap.setMyLocationEnabled(true);
        }
    }

    private void initMapServices(){

        //Initialize the Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources()
                    .getString(R.string.google_place_android_key));
        }
    }

    //region Finders

    private void initUpNearByChatFinder(){
        /*Initializes the Near By Chat Finder*/

        final Activity activity =  this;

        nearbyChatFinder = new NearbyChatFinder(SEARCH_RADIUS, new FinderCallback() {
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
                addYarnPlace(placeMap);
            }

            @Override
            public void onNoPlacesFound(String message) {
                /*This is called when the Nearby Chat Finder doesn't find any places*/
                Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initUpNearByPlaceFinder() {
        /*Initializes the Nearby By Place Finder*/

        final Activity activity =  this;

        nearbyPlaceFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.google_place_key), SEARCH_RADIUS, new FinderCallback() {
            @Override
            public void onFoundPlaces(String nextPageToken, List<HashMap<String, String>> placeMaps) {
                /*This is called when the Nearby Place Finder finds some places */

                addYarnPlaces(placeMaps);
                if(nextPageToken!= null) nearbyPlaceFinder.getPlacesNextPage(nextPageToken);
            }

            @Override
            public void onFoundPlace(HashMap<String, String> placeMap){
                /*This is called when the Nearby Place Finder finds a place */
            }

            @Override
            public void onNoPlacesFound(String message) {
                /*This is called when the Nearby Place Finder doesn't find any places */
                Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
            }
        });
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
                final YarnPlace yarnPlace = addYarnPlace(placeMap);

                //Once the Yarn Place is ready focus on it and show the Info Window
                if(yarnPlace.checkReady())
                {
                    focusOnLatLng(yarnPlace.marker.getPosition());
                    touchedYarnPlace = yarnPlace;
                    yarnPlace.showInfoWindow(mapsActivity,mMap);
                }
                else{
                    yarnPlace.setReadyListener(new ReadyListener() {
                        @Override
                        public void onReady() {
                            focusOnLatLng(yarnPlace.marker.getPosition());
                            touchedYarnPlace = yarnPlace;
                            yarnPlace.showInfoWindow(mapsActivity,mMap);
                        }
                    });
                }
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

        final MapsActivity mapsActivity = this;

        mMap.setOnMarkerClickListener( new OnMarkerClickListener(){

            @Override
            public boolean onMarkerClick(Marker marker) {
                /*Runs when the firebaseUser clicks on a marker*/

                //The firebaseUser has already touched a yarn place so we need to dismiss it
                if(touchedYarnPlace != null)
                {
                    if(touchedYarnPlace.window.isShowing())
                    {
                        touchedYarnPlace.dismissInfoWindow();
                        touchedYarnPlace = null;
                    }
                }

                //Search for the marker to in the list to find the one the firebaseUser has touched
                touchedYarnPlace = searchMarkerInList(recorder.recordedYarnPlaces,marker);

                //Focus on the Yarn Place and show the info window
                if(touchedYarnPlace != null)
                {
                    focusOnLatLng(marker.getPosition());
                    touchedYarnPlace.showInfoWindow(mapsActivity,mMap);
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
                if(touchedYarnPlace != null)
                {
                    if(!touchedYarnPlace.updateWindowPosition(mMap)) touchedYarnPlace = null;
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
                if(touchedYarnPlace != null && touchedYarnPlace.window.isShowing())
                {
                    touchedYarnPlace.dismissInfoWindow();
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

        initCheckBoxes();
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

    private void initCheckBoxes() {
        /*Initializes the Check Boxes*/

        //Get button references
        barCheckBox = findViewById(R.id.barCheckBox);
        cafeCheckBox = findViewById(R.id.cafeCheckBox);
        restaurantCheckBox = findViewById(R.id.restaurantCheckBox);
        nightClubCheckBox = findViewById(R.id.nightClubCheckBox);

        //Set button values
        barCheckBox.setChecked(false);
        restaurantCheckBox.setChecked(false);
        nightClubCheckBox.setChecked(false);

        cafeCheckBox.setChecked(true);

        //Set button listeners
        barCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //If the buttons are all unchecked check the cafe one;
                if(!cafeCheckBox.isChecked() && !barCheckBox.isChecked()
                        && !restaurantCheckBox.isChecked() && !nightClubCheckBox.isChecked()){
                    cafeCheckBox.setChecked(true);
                }
            }
        });

        cafeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //If the buttons are all unchecked check the cafe one;
                if(!cafeCheckBox.isChecked() && !barCheckBox.isChecked()
                        && !restaurantCheckBox.isChecked() && !nightClubCheckBox.isChecked()){
                    cafeCheckBox.setChecked(true);
                }
            }
        });

        restaurantCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //If the buttons are all unchecked check the cafe one;
                if(!cafeCheckBox.isChecked() && !barCheckBox.isChecked()
                        && !restaurantCheckBox.isChecked() && !nightClubCheckBox.isChecked()){
                    cafeCheckBox.setChecked(true);
                }
            }
        });

        nightClubCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //If the buttons are all unchecked check the cafe one;
                if(!cafeCheckBox.isChecked() && !barCheckBox.isChecked()
                        && !restaurantCheckBox.isChecked() && !nightClubCheckBox.isChecked()){
                    cafeCheckBox.setChecked(true);
                }
            }
        });
    }

    //endregion

    //endregion

    //region Button Methods

    public void onRefreshButtonPressed(View view) {
        /*This method is run when the firebaseUser presses the refresh button*/

        if(localUser.lastLocation != null)
        {
            updateCircle();

            //Get all the selected types
            ArrayList<String> types = new ArrayList<>();
            if(barCheckBox.isChecked()) types.add(PlaceType.BAR);
            if(cafeCheckBox.isChecked()) types.add(PlaceType.CAFE);
            if(restaurantCheckBox.isChecked()) types.add(PlaceType.RESTAURANT);
            if(nightClubCheckBox.isChecked()) types.add(PlaceType.NIGHT_CLUB);

            //Get the chats with the selected types
            nearbyChatFinder.getNearbyChats(types);
            nearbyChatFinder.initNearbyChatsListener(types);
            Log.d(TAG,"Getting near by chats");
        }
    }

    public void onFocusOnUserPressed(View view) {
        /*Focus the camera on the firebaseUser*/

        localUser.getUserLocation(this, mFusedLocationProviderClient, new LocalUser.locationReceivedListener() {
            @Override
            public void onLocationReceived(LatLng latLng) {
                focusOnLatLng(latLng);
            }
        });
    }

    public void onChatPlannerPressed(View view) {
        /*This method goes to the Chat Planner activity when they press the Chat Planner Button*/

        Intent intent = new Intent(getBaseContext(),ChatPlannerActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_to_left,R.anim.left_to_right);
        /*
        Intent intent = new Intent(getBaseContext(),ChatPlannerActivity.class);
        intent.putExtra("recordedYarnPlaces",recorder.recordedYarnPlaces);
        startActivityForResult(intent,CHAT_PLANNER_CODE);*/
    }

    public void onNotificationsPressed(View view) {
        /*This method goes to the Notification activity when the firebaseUser presses the notification button*/

        Intent intent = new Intent(getBaseContext(),NotificationsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.down_to_up,R.anim.up_to_down);
    }

    public void onAccountPressed(View view) {
        /*This method goes to the Account Activity when the firebaseUser presses the Account Button*/

        Intent intent = new Intent(getBaseContext(), AccountActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right,R.anim.right_to_left);
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

    public YarnPlace addYarnPlace(HashMap<String,String> placeMap){
        /*Create a Yarn Place Object from the passed placeMap and then adds it to the map and system.
        * This only happens if there isn't a YarnPlace of this description already present. So this
        * method also checks if the passed placeMap's ID isn't present in the list of recorded
        * Yarn Places*/

        final MapsActivity mapsActivity = this;

        //There are no yarn places so add them all
        if(recorder.recordedYarnPlaces.size() == 0)
        {
            return createYarnPlace(placeMap);
        }
        //There are some yarn places so add the ones we need
        else{

            //Look for equal Yarn Places
            for(YarnPlace place: recorder.recordedYarnPlaces){

                if(place.placeMap.get("id").equals(placeMap.get(("id")))){
                    return place;
                }
            }

            return createYarnPlace(placeMap);
        }
    }

    public void addYarnPlaces(List<HashMap<String,String>> placeMaps){
        /*Creates all the yarn places from the passed List of placeMap and then adds them to the map
        and system. This only happens if there isn't a YarnPlace of it's description already present.
        So this method also checks if the passed placeMap's ID isn't present in the list of recorded
        Yarn Places*/


        //There are no yarn places so add them all
        if(recorder.recordedYarnPlaces.size() == 0)
        {
            for(HashMap<String,String> placeMap : placeMaps)
            {
                createYarnPlace(placeMap);
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
                    createYarnPlace(placeMap);
                }
            }
        }
    }

    //endregion

    //region Private methods

    private void focusOnLatLng(LatLng latLng) {
        /*Focuses the camera on a LatLng position*/

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .tilt(40)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private YarnPlace createYarnPlace(HashMap<String,String> placeMap) {
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

                yarnPlace.initOnMap(mapsActivity,mMap);
                recorder.recordYarnPlace(yarnPlace);

                ArrayList<Chat> chats = yarnPlace.getChats();

                if(chats != null || chats.size() > 0)
                {
                    for (Chat c :chats) {
                        c.updator.addValueChangeListener(mapsActivity);
                    }
                }
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

    private void updateCircle(){
        /*Updates the circle on the Map*/

        if(circle != null )circle.remove();

        circle = mMap.addCircle(new CircleOptions()
                .center(localUser.lastLatLng)
                .radius(SEARCH_RADIUS)
                .strokeColor(R.color.searchRadiusStroke)
                .fillColor(R.color.searchRadiusFill));
    }
    //endregion
}
