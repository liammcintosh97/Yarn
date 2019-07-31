package com.example.liammc.yarn.accounting;

import android.app.Activity;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.yarnPlace.PlaceType;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Locale;

public class LocalUser extends YarnUser implements LocationSource, LocationListener {
    /*This class is used to describe a local Yarn User. The local firebaseUser is the firebaseUser that is interacting
    with the application. Local User extends YarnUser and functions in the same way with some
    exceptions and functionality. The LocalUser class allows the application to update the firebaseUser on
    Firebase and also track their location*/

    //region singleton pattern
    private static final LocalUser instance = new LocalUser();

    //private constructor to avoid client applications to use constructor
    private LocalUser(){
        searchRadius =  SEARCH_RADIUS_DEFAULT;

        types.add(PlaceType.BAR);
        types.add(PlaceType.CAFE);
        types.add(PlaceType.NIGHT_CLUB);
        types.add(PlaceType.RESTAURANT);
    }

    public static LocalUser getInstance(Activity _activity){
        instance.activity = _activity;
        return instance;
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
    public ArrayList<String> types = new ArrayList<>();

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

    public void initUserLocation(Activity activity) {
        /*Initialises the firebaseUser location services so that the application can track them*/

        geocoder = new Geocoder(activity, Locale.getDefault());

        locationManager = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);

        // Specify Location Provider criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);

        locationManager = (LocationManager) activity
                .getSystemService(Activity.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, true);
    }

    public void initUserAuth(FirebaseAuth auth){
        /*Initializes the firebaseUser's auth and then gets their email from Firebase*/

        firebaseAuth = auth;
        firebaseUser = auth.getCurrentUser();
        getEmail(firebaseAuth);

        updator = new LocalUserUpdater(this);
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

    public void readyUser(Activity activity){

        //Init the location client
        FusedLocationProviderClient mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(activity);


        //Initializes the Local User's variables and systems
        initDatabaseReferences(firebaseAuth.getUid());
        initUser();

        initUserLocation(activity);
        getUserLocation(activity,mFusedLocationProviderClient,null);
    }

    public void getUserLocation(Activity activity,
                                FusedLocationProviderClient mFusedLocationProviderClient,
                                final locationReceivedListener listener) {
      /*This method is used to manually get the most accurate location at the time*/

        try {
            //Get the location result
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();

            locationResult.addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        LatLng latLng = new LatLng(location.getLatitude(),
                                location.getLongitude());

                        //Pass it to the listeners to handle the results
                        if(listener != null) listener.onLocationReceived(latLng);
                        onLocationChanged(location);
                    }else {
                        //Getting the location was unsuccessful so log the error
                        Log.e(TAG, "Current location is null");
                    }
                }
            });
        } catch(SecurityException e)  {
            //There was an exception when trying to get the location result so log it
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public boolean checkReady(){
        /*Checks if the Local User is ready. The local firebaseUser is considered ready when they have a
        picture, name, location, email, meanRating and terms acceptance
         */

        boolean ready = readyListener != null &&
                profilePicture != null &&
                userName != null &&
                lastLocation != null &&
                lastAddress != null &&
                email != null &&
                ratings != null &&
                birthDate != null &&
                gender != null &&
                termsAcceptance != null;

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
