package com.example.liammc.yarn.core;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.liammc.yarn.Events.NearbyChatFinder;
import com.example.liammc.yarn.Events.NearbyPlaceFinder;
import com.example.liammc.yarn.Events.Notifier;
import com.example.liammc.yarn.Events.SearchPlaceFinder;
import com.example.liammc.yarn.Events.YarnPlace;
import com.example.liammc.yarn.FinderCallback;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.utility.PermissionTools;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    //private final int CHAT_PLANNER_CODE = 1;
    //private final int NOTIFICATION_ACTIVITY_CODE = 2;
    private  final int PERMISSION_REQUEST_CODE = 1;
    private final int SEARCH_RADIUS = 1000;
    private final String TAG = "MapsActivity";

    //Google Services
    GoogleMap mMap;
    PlacesClient placesClient;
    FusedLocationProviderClient mFusedLocationProviderClient;
    NearbyPlaceFinder nearbyPlaceFinder;
    NearbyChatFinder nearbyChatFinder;
    SearchPlaceFinder searchPlaceFinder;
    Geocoder geocoder;

    //Map Data
    public YarnUser localUser;
    //List<YarnPlace> yarnPlaces = new ArrayList<YarnPlace>();

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
    CircleOptions circleOptions;

    //Chat Planner
    //public Recorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //mGeoDataClient = Places.getGeoDataClient(this);
        //mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        recorder = Recorder.getInstance();

        geocoder = new Geocoder(this,Locale.getDefault());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        PermissionTools.requestPermissions(this, PERMISSION_REQUEST_CODE);

        for (YarnPlace place:recorder.recordedYarnPlaces) {
            place.initOnMap(this,mMap);
        }

        setUpNearByChatFinder();
        setUpNearByPlaceFinder();
        setUpSearchPlaceFinder();

        initializeLocalUser();
        initializeMapServices();
        initializeMapUI();

        onFocusOnUserPressed(null);
        updateCircle();
    }

    @Override
    public void onBackPressed() {
        if(touchedYarnPlace != null)
        {
            if(touchedYarnPlace.chatCreator.window.isShowing())
            {
                touchedYarnPlace.chatCreator.dissmissChatCreator();
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
        super.onDestroy();
        unregisterReceiver(notifier.timeChangeReceiver);
    }

    //region SetUp

    private void initializeMapServices() {
        setMarkerListener();
        setCameraMoveListener();
        setMapClickListener();
    }

    private void initializeLocalUser() {

        localUser = LocalUser.getInstance().user;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setLocationSource(localUser);
            mMap.setMyLocationEnabled(true);
        }
    }

    //region Finders

    private void setUpNearByChatFinder(){

        final Activity activity =  this;

        nearbyChatFinder = new NearbyChatFinder(SEARCH_RADIUS, new FinderCallback() {
            @Override
            public void onFoundPlaces(String nextPageToken, List<HashMap<String, String>> placeMaps) {

                Log.d(TAG,"Found Places with Chats - " + placeMaps.toString());
                addYarnPlaces(placeMaps);
            }

            @Override
            public void onFoundPlace(HashMap<String, String> placeMap){

                Log.d(TAG,"Found Place with Chats - " + placeMap.toString());
                addYarnPlace(placeMap);
            }

            @Override
            public void onNoPlacesFound(String message) {
                Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setUpNearByPlaceFinder() {

        final Activity activity =  this;

        nearbyPlaceFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.google_place_key), SEARCH_RADIUS, new FinderCallback() {
            @Override
            public void onFoundPlaces(String nextPageToken, List<HashMap<String, String>> placeMaps) {

                addYarnPlaces(placeMaps);
                if(nextPageToken!= null) nearbyPlaceFinder.getPlacesNextPage(nextPageToken);
            }

            @Override
            public void onFoundPlace(HashMap<String, String> placeMap){

            }

            @Override
            public void onNoPlacesFound(String message) {
                Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setUpSearchPlaceFinder(){

        final MapsActivity mapsActivity =  this;

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources()
                    .getString(R.string.google_place_android_key));
        }
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment searchBar = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        String country = getResources().getConfiguration().locale.getCountry();

        searchPlaceFinder = new SearchPlaceFinder(searchBar,country, new FinderCallback() {

            @Override
            public void onFoundPlaces(String nextPageToken, List<HashMap<String, String>> placeMaps) {

            }

            @Override
            public void onFoundPlace(HashMap<String, String> placeMap){

                YarnPlace yarnPlace = addYarnPlace(placeMap);

                focusOnLatLng(yarnPlace.latLng);
                yarnPlace.showInfoWindow(mapsActivity,mMap);
            }

            @Override
            public void onNoPlacesFound(String message) {
                Toast.makeText(mapsActivity,message,Toast.LENGTH_LONG).show();
            }
        });
    }

    //endregion

    //region Map Listeners

    private void setMarkerListener() {

        final MapsActivity mapsActivity = this;

        mMap.setOnMarkerClickListener( new OnMarkerClickListener(){

            @Override
            public boolean onMarkerClick(Marker marker)
            {
                //The user has already touched a yarn place so we need to dismiss it
                if(touchedYarnPlace != null)
                {
                    if(touchedYarnPlace.window.isShowing())
                    {
                        touchedYarnPlace.dismissInfoWindow();
                        touchedYarnPlace = null;
                    }
                }

                touchedYarnPlace = searchMarkerInList(recorder.recordedYarnPlaces,marker);

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

    private void setCameraMoveListener() {

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {

                if(touchedYarnPlace != null)
                {
                    if(!touchedYarnPlace.updatePopup(mMap)) touchedYarnPlace = null;
                }
            }
        });
    }

    private void setMapClickListener() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng){

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

    private void initializeMapUI() {
        UiSettings uiSettings = mMap.getUiSettings();

        uiSettings.setCompassEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setZoomControlsEnabled(false);

        applyMapStyle();

        setCheckBoxes();
    }

    private void applyMapStyle() {
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

    private void setCheckBoxes() {
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

        if(localUser.lastLocation != null)
        {
            updateCircle();

            ArrayList<String> types = new ArrayList<>();

            if(barCheckBox.isChecked()) types.add(YarnPlace.PlaceType.BAR);
            if(cafeCheckBox.isChecked()) types.add(YarnPlace.PlaceType.CAFE);
            if(restaurantCheckBox.isChecked()) types.add(YarnPlace.PlaceType.RESTAURANT);
            if(nightClubCheckBox.isChecked()) types.add(YarnPlace.PlaceType.NIGHT_CLUB);

            nearbyChatFinder.getNearbyChats(types);
            nearbyChatFinder.setNearbyChatsListener(types);
            Log.d(TAG,"Getting near by chats");
        }
    }

    public void onFocusOnUserPressed(View view) {
        localUser.getUserLocation(this, mFusedLocationProviderClient, new YarnUser.LocationRecievedListener() {
            @Override
            public void onLocationRecieved(LatLng latLng) {
                focusOnLatLng(latLng);
            }
        });
    }

    public void onChatPlannerPressed(View view) {
        Intent intent = new Intent(getBaseContext(),ChatPlannerActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_to_left,R.anim.left_to_right);
        /*
        Intent intent = new Intent(getBaseContext(),ChatPlannerActivity.class);
        intent.putExtra("recordedYarnPlaces",recorder.recordedYarnPlaces);
        startActivityForResult(intent,CHAT_PLANNER_CODE);*/
    }

    public void onNotificationsPressed(View view) {
        Intent intent = new Intent(getBaseContext(),NotificationsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.down_to_up,R.anim.up_to_down);
    }

    public void onAccountPressed(View view) {
        Intent intent = new Intent(getBaseContext(), AccountActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right,R.anim.right_to_left);
    }

    //endregion

    //region Private methods

    private void focusOnLatLng(LatLng latLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .tilt(40)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private YarnPlace createYarnPlace(HashMap<String,String> placeMap)
    {
        YarnPlace yarnPlace = new YarnPlace(placeMap);
        yarnPlace.init(this,geocoder);
        yarnPlace.initOnMap(this,mMap);
        recorder.recordYarnPlace(yarnPlace);

        return yarnPlace;
    }

    private void addYarnPlaces(List<HashMap<String,String>> placeMaps){

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

    public YarnPlace addYarnPlace(HashMap<String,String> placeMap){

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

    //endregion

    //region Utility

    private YarnPlace searchMarkerInList(List<YarnPlace> yarnPlaces, Marker toSearch) {

        //Loop through all places to find marker equality
        if( yarnPlaces != null) {
            for (int i = 0; i < yarnPlaces.size(); i++) {
                if(yarnPlaces.get(i).marker.equals(toSearch)) return yarnPlaces.get(i);
            }
        }
        return null;
    }

    private void updateCircle(){

        if(circle != null )circle.remove();

        circle = mMap.addCircle(new CircleOptions()
                .center(localUser.lastLatLng)
                .radius(SEARCH_RADIUS)
                .strokeColor(R.color.searchRadiusStroke)
                .fillColor(R.color.searchRadiusFill));
    }
    //endregion
}
