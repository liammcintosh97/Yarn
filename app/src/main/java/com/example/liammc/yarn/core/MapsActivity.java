package com.example.liammc.yarn.core;


import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import com.example.liammc.yarn.Events.Chat;
import com.example.liammc.yarn.Events.PlaceFinder;
import com.example.liammc.yarn.Events.YarnPlace;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.PermissionTools;
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
import com.google.firebase.auth.FirebaseAuth;

import java.security.Provider;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        PlaceFinder.PlaceFinderCallback {

    private  final int PERMISSION_REQUEST_CODE = 1;
    private final String TAG = "MapsActivity";

    //Google Services
    GoogleMap mMap;
    PlaceDetectionClient mPlaceDetectionClient;
    GeoDataClient mGeoDataClient;

    //Map Data
    public YarnUser localUser;
    public Chat activeChat;
    List<YarnPlace> bars;
    List<YarnPlace> cafes;
    List<YarnPlace> resturants;

    //User Interaction
    YarnPlace touchedYarnPlace;

    //UI
    CheckBox barCheckBox;
    CheckBox cafeCheckBox;
    CheckBox restaurantCheckBox;
    NumberPicker numberPicker;
    Circle circle;
    CircleOptions circleOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        applyMapStyle();
        PermissionTools.requestPermissions(this, PERMISSION_REQUEST_CODE);

        localUser = new YarnUser(this,
                FirebaseAuth.getInstance().getCurrentUser().getUid()
                ,YarnUser.UserType.LOCAL);

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
            super.onBackPressed();
        }
    }

    @Override
    public void onFoundPlaces(List<YarnPlace> yarnPlaces)
    {
        //Called when any instance of a Place Finder returns a list of Yarn places
    }

    //region SetUp

    private void initializeMapServices()
    {
        mPlaceDetectionClient =  Places.getPlaceDetectionClient(this);
        mGeoDataClient = Places.getGeoDataClient(this);

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
                if(touchedYarnPlace == null) touchedYarnPlace = searchMarkerInList(resturants,marker);

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
        removeYarnPlaces(resturants);

        if(localUser.lastLocation != null)
        {
            if(circle != null )circle.remove();

            circle = mMap.addCircle(new CircleOptions()
                    .center(localUser.lastLatLng)
                    .radius(numberPicker.getValue())
                    .strokeColor(R.color.searchRadiusStroke)
                    .fillColor(R.color.searchRadiusFill));

            if(barCheckBox.isChecked())getBars();
            if(cafeCheckBox.isChecked())getCafes();
            if(restaurantCheckBox.isChecked())getRestaurants();
        }
    }

    public void onFocusOnUserPressed(View view)
    {
        if(localUser.lastLocation != null) {
            Log.d(TAG,"Focusing on user");
            focusOnLatLng(localUser.lastLatLng);
        }
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

    private void getBars()
    {
        //Clear the restaurants list and markers
        if(bars != null) {
            for(int i = 0; i < bars.size(); i++) {
                bars.get(i).marker.remove();
            }
            bars.clear();
        }

        PlaceFinder barFinder = new PlaceFinder(this,
                new PlaceFinder.PlaceFinderCallback() {
            @Override
            public void onFoundPlaces(List<YarnPlace> yarnPlaces) {
                bars = yarnPlaces;
            }
        });

        Object barDataTransfer[] = new Object[3];
        barDataTransfer[0] = mMap;
        barDataTransfer[1] = getPlaceRequestUrl(numberPicker.getValue(),
                localUser.lastLocation.getLatitude(),localUser.lastLocation.getLongitude(),
                "bar");
        barDataTransfer[2] = YarnPlace.PlaceType.BAR;

        barFinder.execute(barDataTransfer);
    }

    private void getCafes()
    {
        //Clear the restaurants list and markers
        if(cafes != null)
        {
            for(int i = 0; i < cafes.size(); i++)
            {
                cafes.get(i).marker.remove();
            }
            cafes.clear();
        }

        //Make a new PlaceFinder instance and set the listener
        PlaceFinder cafeFinder = new PlaceFinder(this,
                new PlaceFinder.PlaceFinderCallback() {
            @Override
            public void onFoundPlaces(List<YarnPlace> yarnPlaces)
            {
                cafes = yarnPlaces;
            }
        });

        //Build the data transfer object
        Object cafeDataTransfer[] = new Object[3];
        cafeDataTransfer[0] = mMap;
        cafeDataTransfer[1] = getPlaceRequestUrl(numberPicker.getValue(),
                localUser.lastLocation.getLatitude(), localUser.lastLocation.getLongitude(),
                "cafe");
        cafeDataTransfer[2] = YarnPlace.PlaceType.CAFE;

        //Execute the place finder and get the desired places
        cafeFinder.execute(cafeDataTransfer);
    }

    private void getRestaurants()
    {
        //Clear the restaurants list and markers
        if(resturants != null)
        {
            for(int i = 0; i < resturants.size(); i++)
            {
                resturants.get(i).marker.remove();
            }
            resturants.clear();
        }

        //Make a new PlaceFinder instance and set the listener
        PlaceFinder restaurantFinder = new PlaceFinder(this,
                new PlaceFinder.PlaceFinderCallback() {
            @Override
            public void onFoundPlaces(List<YarnPlace> yarnPlaces)
            {
                resturants = yarnPlaces;
            }
        });

        //Build the data transfer object
        Object restaurantDataTransfer[] = new Object[3];
        restaurantDataTransfer[0] = mMap;
        restaurantDataTransfer[1] = getPlaceRequestUrl(numberPicker.getValue(),
                localUser.lastLocation.getLatitude(), localUser.lastLocation.getLongitude(),
                "restaurant");
        restaurantDataTransfer[2] = YarnPlace.PlaceType.RESTAURANT;

        //Execute the place finder and get the desired places
        restaurantFinder.execute(restaurantDataTransfer);
    }

    //endregion

    //region Utility

    private String getPlaceRequestUrl(int radius, double latitude , double longitude , String nearbyPlace)
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+radius);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+getResources().getString(R.string.google_place_key));

        Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

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
    //endregion
}
