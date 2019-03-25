package com.example.liammc.yarn.accounting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.ReadyListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;


public class YarnUser implements LocationSource, LocationListener
{
    //region Ready Listener

    private ReadyListener readyListener;

    public ReadyListener getReadyListener() {
        return readyListener;
    }

    public void setReadyListener(ReadyListener readyListener) {
        this.readyListener = readyListener;
    }
    //endregion

    //region Location Received Listener

    public interface LocationRecievedListener{
        void onLocationRecieved(LatLng latLng);
    }

    //endregion

    //endregion
    private final String TAG = "YarnUser";
    public enum UserType{LOCAL,NETWORK}

    //Local User Location;
    OnLocationChangedListener listner;
    Geocoder geocoder;
    LocationManager locationManager;
    String provider;
    Criteria criteria;
    private final int minTime = 10000;     // minimum time interval between location updates, in milliseconds
    private final int minDistance = 100;

    //Firebase
    private final StorageReference userStorageReferance;
    private final DatabaseReference userDatabaseReference;
    private FirebaseAuth userAuth;

    //User info
    public String userID;
    public String userName;
    public Bitmap profilePicture;
    public String email;
    public Double rating = null;
    public Boolean termsAcceptance = null;
    private UserType userType;

    //User Location
    public Location lastLocation;
    public LatLng lastLatLng;
    public Address lastAddress;

    public YarnUser(String _userID, UserType type)
    {
        this.userType = type;

        this.userStorageReferance = FirebaseStorage.getInstance().getReference().child("Users");
        this.userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        this.userID = _userID;
        this.getUserName();
        this.getUserProfilePicture();
        this.getUserRating();
        this.getUserTermAcceptance();
    }


    @Override
    public void deactivate(){

        locationManager.removeUpdates(this);

        listner = null;
    }

    @Override
    public void activate(OnLocationChangedListener _listener){

        listner = _listener;
        Log.d(TAG,"The Listener has been activated");

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
    public void onLocationChanged(Location location) {

        if (listner != null) {
            listner.onLocationChanged(location);
        }

        lastLocation = location;
        lastLatLng = new LatLng(lastLocation.getLatitude()
                ,lastLocation.getLongitude());
        lastAddress = AddressTools.getAddressFromLocation(geocoder, lastLatLng);
        //TODO make the notifier work with the new location structure
        //Notifier.getInstance().onLocationChanged(context, location);
        Log.d(TAG, "Got local user's location");


        if(checkReady()){
            readyListener.onReady();
            readyListener = null;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    //region User Setup

    public void setAuth(FirebaseAuth auth){
        userAuth = auth;

        getEmail(userAuth);
    }

    public void setUpUserLocation(Activity callingActivity)
    {
        geocoder = new Geocoder(callingActivity, Locale.getDefault());

        locationManager = (LocationManager) callingActivity.getSystemService(Activity.LOCATION_SERVICE);

        // Specify Location Provider criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);

        locationManager = (LocationManager) callingActivity
                .getSystemService(Activity.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, true);
    }

    //endregion

    //region Get User Data
    private void getUserName()
    {
        userDatabaseReference
                .child(userID)
                .child("userName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                userName = (String) snapshot.getValue();
                Log.d(TAG,"Got username");

                if(checkReady()){
                    readyListener.onReady();
                    readyListener = null;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(TAG,"Unable to get value from database");
            }
        });
    }

    private void getUserProfilePicture()
    {
        final long ONE_MEGABYTE = 1024 * 1024;
        userStorageReferance
                .child(userID)
                .child("profilePicture")
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes)
            {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                if(bitmap != null)
                {
                    profilePicture = bitmap;
                    Log.d(TAG,"Got profile picture");

                    if(checkReady()){
                        readyListener.onReady();
                        readyListener = null;
                    }
                }
                else
                {
                    Log.d(TAG,"Unable to decode byte array");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG,"Failed to download profile picture");
            }
        });
    }

    private void getEmail(FirebaseAuth auth)
    {
        email = auth.getCurrentUser().getEmail();
        Log.d(TAG,"Got email");

        if(checkReady()){
            readyListener.onReady();
            readyListener = null;
        }
        /*
        userDatabaseReference
                .child(userID)
                .child("email").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                email = (String) snapshot.getValue();
                Log.d(TAG,"Got email");

                if(checkReady()){
                    readyListener.onReady();
                    readyListener = null;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(TAG,"Unable to get value from database");
            }
        });*/
    }

    private void getUserRating()
    {
        userDatabaseReference
                .child(userID)
                .child("rating").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        try {
                            rating = (double) snapshot.getValue();
                            Log.d(TAG,"Got user rating");

                            if(checkReady()){
                                readyListener.onReady();
                                readyListener = null;
                            }
                        }
                        catch (ClassCastException e)
                        {
                            Long l = (long)snapshot.getValue();
                            rating = l.doubleValue();

                            Log.d(TAG,"Got user rating");

                            if(checkReady()){
                                readyListener.onReady();
                                readyListener = null;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                        Log.e(TAG,"Unable to get value from database");
                    }
                });
    }

    private void getUserTermAcceptance()
    {
        userDatabaseReference
                .child(userID)
                .child("TermsAcceptance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                 termsAcceptance = (boolean)snapshot.getValue();
                Log.d(TAG,"Got terms acceptance");

                if(checkReady()){
                    readyListener.onReady();
                    readyListener = null;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(TAG,"Unable to get value from database");
            }
        });
    }

    public void getUserLocation(Activity activity,
                                 FusedLocationProviderClient mFusedLocationProviderClient,
                                 final LocationRecievedListener listener) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {

            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(activity,new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastLocation =(Location)task.getResult();
                        LatLng latLng = new LatLng(lastLocation.getLatitude(),
                                lastLocation.getLongitude());

                        lastLatLng = latLng;
                        lastAddress = AddressTools.getAddressFromLocation(geocoder,latLng);
                        Log.d(TAG,"Got the user's current location");

                        if(listener != null) listener.onLocationRecieved(latLng);

                        if(checkReady()){
                            readyListener.onReady();
                            readyListener = null;
                        }
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

    //endregion

    //region Utility

    private boolean checkReady(){

        boolean ready = readyListener != null &&
                        profilePicture != null &&
                        lastLocation != null &&
                        userName != null &&
                        email != null &&
                        rating != null &&
                        termsAcceptance != null;

        return ready;
    }

    //endregion

}
