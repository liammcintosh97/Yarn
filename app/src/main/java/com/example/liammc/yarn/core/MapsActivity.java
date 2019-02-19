package com.example.liammc.yarn.core;


import android.Manifest;
import android.content.Context;
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

import com.example.liammc.yarn.Events.Notifier;
import com.example.liammc.yarn.Events.PlaceFinder;
import com.example.liammc.yarn.Events.YarnPlace;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.PermissionTools;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
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

import java.util.ArrayList;

import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    //private final int CHAT_PLANNER_CODE = 1;
    //private final int NOTIFICATION_ACTIVITY_CODE = 2;
    private  final int PERMISSION_REQUEST_CODE = 1;
    private final int SEARCH_PROXIMITY = 100;
    private final String TAG = "MapsActivity";

    //Google Services
    GoogleMap mMap;
    Geocoder geocoder;
    PlaceDetectionClient mPlaceDetectionClient;
    GeoDataClient mGeoDataClient;
    FusedLocationProviderClient mFusedLocationProviderClient;

    //Map Data
    public YarnUser localUser;
    List<YarnPlace> nearbyYarnPlaces = new ArrayList<>();
    //List<YarnPlace> bars = new ArrayList<>();
    //List<YarnPlace> cafes = new ArrayList<>();
    //List<YarnPlace> restaurants = new ArrayList<>();
    //List<YarnPlace> nightClubs = new ArrayList<>();

    //User Interaction
    YarnPlace touchedYarnPlace;
    Notifier notifier;

    //UI
    CheckBox barCheckBox;
    CheckBox cafeCheckBox;
    CheckBox restaurantCheckBox;
    CheckBox nightClubCheckBox;

    Circle circle;
    CircleOptions circleOptions;

    //Chat Planner
    //public ChatRecorder chatRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGeoDataClient = Places.getGeoDataClient(this);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        notifier = Notifier.getInstance();
        registerReceiver(notifier.timeChangeReceiver,notifier.intentFilter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        applyMapStyle();
        PermissionTools.requestPermissions(this, PERMISSION_REQUEST_CODE);

        localUser = LocalUser.getInstance().user;

        initializeMapServices();
        initializeMapUI();

        onFocusOnUserPressed(null);
        onRefreshButtonPressed(null);
    }

    @Override
    public void onBackPressed()
    {
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

    private void initializeMapServices()
    {
        initializeUserLocation();
        setMarkerListener();
        setCameraMoveListener();
        setMapClickListener();
    }

    private void initializeMapUI()
    {
        UiSettings uiSettings = mMap.getUiSettings();

        uiSettings.setCompassEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setZoomControlsEnabled(false);

        setCheckBoxes();
    }

    private void applyMapStyle()
    {
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

    private void setMarkerListener()
    {
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

                /*
                //Loop through all places to find the equal marker
                touchedYarnPlace = searchMarkerInList(bars,marker);
                if(touchedYarnPlace == null) touchedYarnPlace = searchMarkerInList(cafes,marker);
                if(touchedYarnPlace == null) touchedYarnPlace = searchMarkerInList(restaurants,marker);*/

                touchedYarnPlace = searchMarkerInList(nearbyYarnPlaces,marker);

                if(touchedYarnPlace != null)
                {
                    focusOnLatLng(marker.getPosition());
                    touchedYarnPlace.showInfoWindow();
                    Log.d(TAG,"Found the correct Yarn place from marker click");
                }
                else{
                    Log.e(TAG,"Fatal error, couldn't find touched marker in Yarn places lists");
                }
                    return true;
            }
        });
    }

    private void setCameraMoveListener()
    {
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {

                if(touchedYarnPlace != null)
                {
                    if(!touchedYarnPlace.updatePopup()) touchedYarnPlace = null;
                }
            }
        });
    }

    private void setMapClickListener()
    {
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

    private void setCheckBoxes()
    {
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

                //Un-check the other buttons if this one is checked
                if(b){
                    cafeCheckBox.setChecked(false);
                    restaurantCheckBox.setChecked(false);
                    nightClubCheckBox.setChecked(false);
                }

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

                //Un-check the other buttons if this one is checked
                if(b){
                    barCheckBox.setChecked(false);
                    restaurantCheckBox.setChecked(false);
                    nightClubCheckBox.setChecked(false);
                }

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

                //Un-check the other buttons if this one is checked
                if(b){
                    cafeCheckBox.setChecked(false);
                    barCheckBox.setChecked(false);
                    nightClubCheckBox.setChecked(false);
                }

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

                //Un-check the other buttons if this one is checked
                if(b){
                    cafeCheckBox.setChecked(false);
                    restaurantCheckBox.setChecked(false);
                    barCheckBox.setChecked(false);
                }

                //If the buttons are all unchecked check the cafe one;
                if(!cafeCheckBox.isChecked() && !barCheckBox.isChecked()
                        && !restaurantCheckBox.isChecked() && !nightClubCheckBox.isChecked()){
                    cafeCheckBox.setChecked(true);
                }
            }
        });
    }

    private void initializeUserLocation()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            //final Context context = this;
            geocoder = new Geocoder(this, Locale.getDefault());

            localUser.setUpUserLocation(this);

            mMap.setLocationSource(localUser);
            mMap.setMyLocationEnabled(true);
        }
    }

    //endregion

    //region Button Methods

    public void onRefreshButtonPressed(View view) {

        /*
        clearYarnPlaceList(bars);
        clearYarnPlaceList(cafes);
        clearYarnPlaceList(restaurants);
        clearYarnPlaceList(nightClubs);
        */

        clearYarnPlaceList(nearbyYarnPlaces);

        if(localUser.lastLocation != null)
        {
            if(circle != null )circle.remove();

            circle = mMap.addCircle(new CircleOptions()
                    .center(localUser.lastLatLng)
                    .radius(SEARCH_PROXIMITY)
                    .strokeColor(R.color.searchRadiusStroke)
                    .fillColor(R.color.searchRadiusFill));

            if(barCheckBox.isChecked())getBars(null);
            if(cafeCheckBox.isChecked())getCafes(null);
            if(restaurantCheckBox.isChecked())getRestaurants(null);
            if(nightClubCheckBox.isChecked())getNightClubs(null);
        }
    }

    public void onFocusOnUserPressed(View view) {
        getUserLocation();
    }

    public void onChatPlannerPressed(View view) {
        Intent intent = new Intent(getBaseContext(),ChatPlannerActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_to_left,R.anim.left_to_right);
        /*
        Intent intent = new Intent(getBaseContext(),ChatPlannerActivity.class);
        intent.putExtra("recordedChats",chatRecorder.recordedChats);
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

    public void onSearchPressed(View view){

    }
    //endregion

    //region local methods

    private void getUserLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {

            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        localUser.lastLocation =(Location)task.getResult();
                        LatLng latLng = new LatLng(localUser.lastLocation.getLatitude(),
                                localUser.lastLocation.getLongitude());

                        localUser.lastLatLng = latLng;
                        focusOnLatLng(latLng);
                        Log.d(TAG,"Got the user's current location");
                    } else {
                        Log.d(TAG, "Current location is null");
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });

        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void focusOnLatLng(LatLng latLng)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .tilt(40)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void getBars(String pageToken)
    {
        PlaceFinder barFinder = new PlaceFinder(this,
                new PlaceFinder.PlaceFinderCallback() {
                    @Override
                    public void onFoundPlaces(String nextPageToken,List<YarnPlace> yarnPlaces) {

                        //Add the found places to the list
                        //bars.addAll(yarnPlaces);
                        nearbyYarnPlaces.addAll(yarnPlaces);

                        //If there is an other page call itself
                        if(nextPageToken != null){
                            Log.d(TAG,"More bars were found requesting the next page");
                            getCafes(nextPageToken);
                        }
                    }

                    @Override
                    public void onNoPlacesFound(){
                        clearYarnPlaceList(nearbyYarnPlaces);
                        Toast.makeText(MapsActivity.this, "No places were found",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        //Get the first page of results
        if(pageToken == null){
            barFinder.execute(buildDataTransferObject(YarnPlace.PlaceType.BAR+"|night_club"));
        }
        //Get the next page of results
        else {
            barFinder.execute(buildDataTransferObject(pageToken,
                    YarnPlace.PlaceType.BAR));
        }
    }

    private void getCafes(String pageToken)
    {
        //Make a new PlaceFinder instance and set the listener
        PlaceFinder cafeFinder = new PlaceFinder(this,
                new PlaceFinder.PlaceFinderCallback() {
                    @Override
                    public void onFoundPlaces(String nextPageToken,List<YarnPlace> yarnPlaces) {
                        //Add the found places to the list
                        //cafes.addAll(yarnPlaces);
                        nearbyYarnPlaces.addAll(yarnPlaces);

                        //If there is an other page call itself
                        if(nextPageToken != null){
                            Log.d(TAG,"More cafes were found requesting the next page");
                            getCafes(nextPageToken);
                        }
                    }

                    @Override
                    public void onNoPlacesFound(){
                        clearYarnPlaceList(nearbyYarnPlaces);
                        Toast.makeText(MapsActivity.this, "No places were found",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        //Get the first page of results
        if(pageToken == null){
            cafeFinder.execute(buildDataTransferObject(YarnPlace.PlaceType.CAFE));
        }
        //Get the next page of results
        else {
            cafeFinder.execute(buildDataTransferObject(pageToken,
                    YarnPlace.PlaceType.CAFE));
        }
    }

    private void getRestaurants(String pageToken)
    {
        //Make a new PlaceFinder instance and set the listener
        PlaceFinder restaurantFinder = new PlaceFinder(this,
                new PlaceFinder.PlaceFinderCallback() {
                    @Override
                    public void onFoundPlaces(String nextPageToken,List<YarnPlace> yarnPlaces) {
                       //restaurants.addAll(yarnPlaces);
                        nearbyYarnPlaces.addAll(yarnPlaces);

                       if(nextPageToken != null){
                           Log.d(TAG,"More restaurants were found requesting the next page");
                           getRestaurants(nextPageToken);
                       }
                    }

                    @Override
                    public void onNoPlacesFound(){
                        clearYarnPlaceList(nearbyYarnPlaces);
                        Toast.makeText(MapsActivity.this, "No places were found",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        if(pageToken == null){
            restaurantFinder.execute(buildDataTransferObject(YarnPlace.PlaceType.RESTAURANT));
        }
        else {
            restaurantFinder.execute(buildDataTransferObject(pageToken,
                    YarnPlace.PlaceType.RESTAURANT));
        }

    }

    private void getNightClubs(String pageToken)
    {
        //Make a new PlaceFinder instance and set the listener
        PlaceFinder nightClubFinder = new PlaceFinder(this,
                new PlaceFinder.PlaceFinderCallback() {
                    @Override
                    public void onFoundPlaces(String nextPageToken,List<YarnPlace> yarnPlaces) {
                        //nightClubs.addAll(yarnPlaces);
                        nearbyYarnPlaces.addAll(yarnPlaces);

                        if(nextPageToken != null){
                            Log.d(TAG,"More restaurants were found requesting the next page");
                            getRestaurants(nextPageToken);
                        }
                    }

                    @Override
                    public void onNoPlacesFound(){
                        clearYarnPlaceList(nearbyYarnPlaces);
                        Toast.makeText(MapsActivity.this, "No places were found",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        if(pageToken == null){
            nightClubFinder.execute(buildDataTransferObject(YarnPlace.PlaceType.NIGHT_CLUB));
        }
        else {
            nightClubFinder.execute(buildDataTransferObject(pageToken,
                    YarnPlace.PlaceType.NIGHT_CLUB));
        }

    }

    //endregion

    //region Utility

    private Object[] buildDataTransferObject(String placeType)
    {
        //Build the data transfer object
        Object dataTransfer[] = new Object[3];
        dataTransfer[0] = mMap;
        dataTransfer[1] = getPlaceRequestUrl(SEARCH_PROXIMITY, localUser.lastLocation.getLatitude(),
                localUser.lastLocation.getLongitude(),placeType);
        dataTransfer[2] = placeType;

        return dataTransfer;
    }

    private Object[] buildDataTransferObject(String nextPageToken,String placeType)
    {
        //Build the data transfer object
        Object dataTransfer[] = new Object[3];
        dataTransfer[0] = mMap;
        dataTransfer[1] = getNextPageRequestURL(nextPageToken);
        dataTransfer[2] = placeType;

        return dataTransfer;
    }

    private String getPlaceRequestUrl(int radius, double latitude , double longitude , String nearbyPlace)
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+100);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&fields=name,place_id,geometry,reference");
        googlePlaceUrl.append("&key="+getResources().getString(R.string.google_place_key));

        Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    private String getNextPageRequestURL(String token)
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("key="+getResources().getString(R.string.google_place_key));
        googlePlaceUrl.append("&pagetoken="+token);

        Log.d(TAG,googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    private YarnPlace searchMarkerInList(List<YarnPlace> yarnPlaces, Marker toSearch) {

        //Loop through all places to find marker equality
        if( yarnPlaces != null) {
            for (int i = 0; i < yarnPlaces.size(); i++) {
                if(yarnPlaces.get(i).marker.equals(toSearch)) return yarnPlaces.get(i);
            }
        }
        return null;
    }

    private void clearYarnPlaceList(List<YarnPlace> yarnPlaces){

        //Clear the restaurants list and markers
        if(yarnPlaces != null)
        {
            for(int i = 0; i < yarnPlaces.size(); i++)
            {
                yarnPlaces.get(i).marker.remove();
                yarnPlaces.get(i).dismissInfoWindow();
            }
            yarnPlaces.clear();
        }

    }

    //endregion
}
