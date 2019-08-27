package com.example.liammc.yarn.accounting;

import android.app.Activity;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.PermissionTools;
import com.example.liammc.yarn.yarnPlace.PlaceType;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class LocalUser extends YarnUser implements LocationSource, LocationListener {
    /*This class is used to describe a local Yarn User. The local firebaseUser is the firebaseUser that is interacting
    with the application. Local User extends YarnUser and functions in the same way with some
    exceptions and functionality. The LocalUser class allows the application to updateInfoWindow the firebaseUser on
    Firebase and also track their location*/

    //region singleton pattern
    private static final LocalUser instance = new LocalUser();

    //private constructor to avoid client applications to use constructor
    private LocalUser(){
        searchRadius =  SEARCH_RADIUS_DEFAULT;

        types[0] = PlaceType.BAR;
        types[1] = PlaceType.CAFE;
        types[2] = PlaceType.NIGHT_CLUB;
        types[3] = PlaceType.RESTAURANT;
    }

    public static LocalUser getInstance(){ return instance; }
    //endregion

    //region Location Received Listener
    /*This location listener is used from alert external objects to when the firebaseUser has changed
    location*/

    public interface locationReceivedListener {
        void onLocationReceived(LatLng latLng);
    }

    //endregion

    private final String TAG = "Local User";
    private Activity activity;
    public String[] types = new String[4];

    //Local User Location;
    OnLocationChangedListener locationChangedListener;
    Geocoder geocoder;
    LocationManager locationManager;
    String provider;
    Criteria criteria;
    private final int minTime = 10000;
    private final int minDistance = 100;

    //User Location
    public Location lastLocation;
    public LatLng lastLatLng;
    public Address lastAddress;
    public double searchRadius;
    public static final int SEARCH_RADIUS_DEFAULT = 1000;
    public static final int SEARCH_RADIUS_MAX =  10000;
    public static final int SEARCH_RADIUS_MIN =  100;

    //Firebase
    public FirebaseAuth firebaseAuth;
    public FirebaseUser firebaseUser;

    public LocalUserUpdater updator;

    //region Init

    public void initTypes(){
        types[0] = (PlaceType.CAFE);
        types[1] = (PlaceType.BAR);
        types[2] = (PlaceType.RESTAURANT);
        types[3] = (PlaceType.NIGHT_CLUB);
    }

    public void initUserLocation(Activity activity) {
        /*Initialises the firebaseUser location services so that the application can track them*/

        if(geocoder == null){
            geocoder = new Geocoder(activity, Locale.getDefault());
        }

        if(locationManager == null){
            locationManager = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);
        }

        // Specify Location Provider criteria
        if(criteria == null){
            criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setSpeedRequired(false);
            criteria.setCostAllowed(true);
        }

        if(locationManager == null) {
            locationManager = (LocationManager) activity
                    .getSystemService(Activity.LOCATION_SERVICE);
        }
        if(provider == null) provider = locationManager.getBestProvider(criteria, true);
    }

    public void initUserAuth(FirebaseAuth auth){
        /*Initializes the firebaseUser's auth and then gets their email from Firebase*/

        if(firebaseAuth == null) firebaseAuth = auth;
        if(firebaseUser == null) firebaseUser = auth.getCurrentUser();
        if(email == null) getEmail(firebaseAuth);

        if(updator == null)updator = new LocalUserUpdater(this);
    }
    //endregion

    //region Location Source
    /*Location source is used by Google Maps to describe an object to track*/
    @Override
    public void activate(OnLocationChangedListener _listener){
        /*This method must be run when the application needs to activate the location source and
        there for begin to track it*/

        locationChangedListener = _listener;
        Log.d(TAG,"The Listener has been activated");

        //Request location updates from the device so that the firebaseUser can be tracked
        try {
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
            } else {
                Log.d(TAG,"No providers at this time");
            }
        }catch (SecurityException e){
            Log.e(TAG,e.toString());
        }

    }

    @Override
    public void deactivate(){
        /*Deactivate the location source, stop the location updates and remove the listener*/
        locationManager.removeUpdates(this);
        locationChangedListener = null;
    }

    //endregion

    //region Location Listener
    /*This location listener is used to alert the firebaseUser object internally to when the person using
    the device has moved location*/

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onLocationChanged(Location location) {
        /*Runs when the firebaseUser changes location*/


        if (locationChangedListener != null) {
            locationChangedListener.onLocationChanged(location);
        }

        //Update the firebaseUser location variables
        lastLocation = location;
        lastLatLng = new LatLng(lastLocation.getLatitude()
                ,lastLocation.getLongitude());
        lastAddress = AddressTools.getAddressFromLocation(geocoder, lastLatLng);

        Log.d(TAG, "Got local firebaseUser's location");

        if(activity != null && activity instanceof MapsActivity){
            MapsActivity mapsActivity = ((MapsActivity)activity);
            if(mapsActivity.searchRadius != null)
                mapsActivity.searchRadius.update(searchRadius,lastLatLng);
        }

        //Check if the firebaseUser is ready after getting their location
        if(checkReady()){
            readyListener.onReady();
            readyListener = null;
        }
    }

    //endregion

    //region Public Methods

    public void getUserLocation(Activity activity,
                                FusedLocationProviderClient mFusedLocationProviderClient,
                                final locationReceivedListener listener) {
      /*This method is used to manually get the most accurate location at the time*/

        PermissionTools.requestPermissions(activity, 1);

        try {
            //Get the location result
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();

            locationResult.addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    lastLocation =  location;

                    if (lastLocation != null) {
                        // Logic to handle location object
                        LatLng latLng = new LatLng(lastLocation.getLatitude(),
                                lastLocation.getLongitude());

                        //Pass it to the listeners to handle the results
                        if(listener != null) listener.onLocationReceived(latLng);
                        onLocationChanged(lastLocation);
                    }else {
                        //Getting the location was unsuccessful so log the error
                        if(listener != null) listener.onLocationReceived(null);
                        Log.e(TAG, "Current location is null");
                    }
                }
            });
        } catch(SecurityException e)  {
            //There was an exception when trying to get the location result so log it
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean checkReady(){
        /*Checks if the Local User is ready. The local firebaseUser is considered ready when they have a
        picture, name, location, email, meanRating and terms acceptance
         */

        boolean parentReady = super.checkReady();

        boolean ready = parentReady &&
                lastLocation != null &&
                lastAddress != null &&
                email != null;


        return ready;
    }

    //endregion

    //region Private Methods

    private void getEmail(FirebaseAuth auth) {
        /*Gets the firebaseUser's email from Firebase authentication service*/

        email = auth.getCurrentUser().getEmail();
        Log.d(TAG,"Got email");

        //Check if the firebaseUser is ready after getting the email
        if(checkReady()){
            readyListener.onReady();
            readyListener = null;
        }
    }


    //endregion
}
