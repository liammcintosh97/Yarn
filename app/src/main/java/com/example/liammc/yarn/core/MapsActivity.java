package com.example.liammc.yarn.core;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import com.example.liammc.yarn.Events.Notifier;
import com.example.liammc.yarn.Events.PlaceFinder;
import com.example.liammc.yarn.Events.YarnPlace;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.utility.PermissionTools;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    //private final int CHAT_PLANNER_CODE = 1;
    //private final int NOTIFICATION_ACTIVITY_CODE = 2;
    private  final int PERMISSION_REQUEST_CODE = 1;
    private final String TAG = "MapsActivity";

    //Google Services
    GoogleMap mMap;
    PlaceDetectionClient mPlaceDetectionClient;
    GeoDataClient mGeoDataClient;

    //Map Data
    public YarnUser localUser;
    List<YarnPlace> bars = new ArrayList<>();
    List<YarnPlace> cafes = new ArrayList<>();
    List<YarnPlace> restaurants = new ArrayList<>();

    //User Interaction
    YarnPlace touchedYarnPlace;
    Notifier notifier;

    //UI
    CheckBox barCheckBox;
    CheckBox cafeCheckBox;
    CheckBox restaurantCheckBox;
    NumberPicker numberPicker;
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

        notifier = Notifier.getInstance();
        registerReceiver(notifier.timeChangeReceiver,notifier.intentFilter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        applyMapStyle();
        PermissionTools.requestPermissions(this, PERMISSION_REQUEST_CODE);

        localUser = LocalUser.getInstance().user;
        localUser.setupUserLocation(this);

        initializeMapUI();
        initializeMapServices();

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

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that it is the SecondActivity with an OK result
        if (requestCode == CHAT_PLANNER_CODE ) {
            if (resultCode == RESULT_OK) {
                chatRecorder.recordedChats = data.getParcelableExtra("recordedChats");
            }
        }
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notifier.timeChangeReceiver);
    }

    //region SetUp

    private void initializeMapServices()
    {
        mPlaceDetectionClient =  Places.getPlaceDetectionClient(this);
        mGeoDataClient = Places.getGeoDataClient(this);
        //chatRecorder = new ChatRecorder();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.setLocationSource(localUser);

        }

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

        barCheckBox = findViewById(R.id.barCheckBox);
        cafeCheckBox = findViewById(R.id.cafeCheckBox);
        restaurantCheckBox = findViewById(R.id.restaurantCheckBox);
        numberPicker = findViewById(R.id.numberPicker);

        setNumberPicker();
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

    private void setNumberPicker()
    {
        String[] values = {
                "1000",
                "2000",
                "3000",
                "4000",
                "5000",
                "6000",
                "7000",
                "8000",
                "9000",
                "10000"};

        numberPicker.setMinValue(1000);
        numberPicker.setMaxValue(10000);
        //numberPicker.setDisplayedValues(values);
        //numberPicker.setValue(Integer.parseInt(values[values.length -1]));

        Log.d(TAG,"The number picker's value has been set to " + numberPicker.getValue());

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1)
            {
                circleOptions = new CircleOptions();

                circleOptions.center(localUser.lastLatLng);
                circleOptions.radius(i1);
                circleOptions.strokeColor(R.color.searchRadiusStroke);
                circleOptions.fillColor(R.color.searchRadiusFill);
            }
        });

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

                //Loop through all places to find the equal marker
                touchedYarnPlace = searchMarkerInList(bars,marker);
                if(touchedYarnPlace == null) touchedYarnPlace = searchMarkerInList(cafes,marker);
                if(touchedYarnPlace == null) touchedYarnPlace = searchMarkerInList(restaurants,marker);

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

    //endregion

    //region Button Methods

    public void onRefreshButtonPressed(View view)
    {
        removeYarnPlaces(bars);
        removeYarnPlaces(cafes);
        removeYarnPlaces(restaurants);

        if(localUser.lastLocation != null)
        {
            if(circle != null )circle.remove();

            circle = mMap.addCircle(new CircleOptions()
                    .center(localUser.lastLatLng)
                    .radius(numberPicker.getValue())
                    .strokeColor(R.color.searchRadiusStroke)
                    .fillColor(R.color.searchRadiusFill));

            if(barCheckBox.isChecked())getBars(null);
            if(cafeCheckBox.isChecked())getCafes(null);
            if(restaurantCheckBox.isChecked())getRestaurants(null);
        }
    }

    public void onFocusOnUserPressed(View view)
    {
        if(localUser.lastLocation != null) {
            Log.d(TAG,"Focusing on user");
            focusOnLatLng(localUser.lastLatLng);
        }
    }

    public void onChatPlannerPressed(View view)
    {
        Intent intent = new Intent(getBaseContext(),ChatPlannerActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_to_left,R.anim.left_to_right);
        /*
        Intent intent = new Intent(getBaseContext(),ChatPlannerActivity.class);
        intent.putExtra("recordedChats",chatRecorder.recordedChats);
        startActivityForResult(intent,CHAT_PLANNER_CODE);*/
    }

    public void onNotificationsPressed(View view)
    {
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

    private void focusOnLatLng(LatLng latLng)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)          // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void getBars(String pageToken)
    {
        PlaceFinder barFinder = new PlaceFinder(this,
                new PlaceFinder.PlaceFinderCallback() {
                    @Override
                    public void onFoundPlaces(String nextPageToken,List<YarnPlace> yarnPlaces) {

                        //Add the found places to the list
                        bars.addAll(yarnPlaces);

                        //If there is an other page call itself
                        if(nextPageToken != null){
                            Log.d(TAG,"More bars were found requesting the next page");
                            getCafes(nextPageToken);
                        }
                    }
                });

        //Get the first page of results
        if(pageToken == null){

            clearYarnPlaceList(bars);
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
                    public void onFoundPlaces(String nextPageToken,List<YarnPlace> yarnPlaces)
                    {
                        //Add the found places to the list
                        cafes.addAll(yarnPlaces);

                        //If there is an other page call itself
                        if(nextPageToken != null){
                            Log.d(TAG,"More cafes were found requesting the next page");
                            getCafes(nextPageToken);
                        }
                    }
                });

        //Get the first page of results
        if(pageToken == null){

            clearYarnPlaceList(cafes);
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
                    public void onFoundPlaces(String nextPageToken,List<YarnPlace> yarnPlaces)
                    {
                       restaurants.addAll(yarnPlaces);

                       if(nextPageToken != null){
                           Log.d(TAG,"More restaurants were found requesting the next page");
                           getRestaurants(nextPageToken);
                       }
                    }
                });

        if(pageToken == null){

            clearYarnPlaceList(restaurants);
            restaurantFinder.execute(buildDataTransferObject(YarnPlace.PlaceType.RESTAURANT));
        }
        else {
            restaurantFinder.execute(buildDataTransferObject(pageToken,
                    YarnPlace.PlaceType.RESTAURANT));
        }

    }

    //endregion

    //region Utility

    private Object[] buildDataTransferObject(String placeType)
    {
        //Build the data transfer object
        Object dataTransfer[] = new Object[3];
        dataTransfer[0] = mMap;
        dataTransfer[1] = getPlaceRequestUrl(numberPicker.getValue(),
                localUser.lastLocation.getLatitude(), localUser.lastLocation.getLongitude(),
                placeType);
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

    private void removeYarnPlaces(List<YarnPlace> places)
    {
        if(places != null)
        {
            for(int i = 0; i < places.size(); i++)
            {
                places.get(i).marker.remove();
                places.get(i).dismissInfoWindow();
                places.remove(i);
            }
        }
    }

    private void clearYarnPlaceList(List<YarnPlace> yarnPlaces){

        //Clear the restaurants list and markers
        if(yarnPlaces != null)
        {
            for(int i = 0; i < yarnPlaces.size(); i++)
            {
                yarnPlaces.get(i).marker.remove();
            }
            yarnPlaces.clear();
        }

    }

    //endregion
}
