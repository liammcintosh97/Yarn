package com.example.liammc.yarn.accounting;

import android.app.Activity;
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

import com.example.liammc.yarn.Events.Notifier;
import com.example.liammc.yarn.utility.AddressTools;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    //endregion
    private final String CALLINGTAG;
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

    //User info
    public String userID;
    public String userName;
    public Bitmap profilePicture;
    public String email;
    public double rating;
    public boolean termsAcceptance;
    private UserType userType;

    //User Location
    public Location lastLocation;
    public LatLng lastLatLng;
    public Address lastAddress;

    public YarnUser(String callingTag, String _userID, UserType type)
    {
        this.userType = type;

        this.CALLINGTAG = callingTag;

        this.userStorageReferance = FirebaseStorage.getInstance().getReference().child("Users");
        this.userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        this.userID = _userID;
        this.getUserName();
        this.getUserProfilePicture();
        this.getEmail();
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
        Log.d(CALLINGTAG,"The Listener has been activated");

        try {
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
            } else {
                Log.d(CALLINGTAG,"No providers at this time");
            }
        }catch (SecurityException e){
            Log.e(CALLINGTAG,e.toString());
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
        Log.d(CALLINGTAG, "Got local user's location");
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


    public void setUpUserLocation(Activity callingActivity)
    {
        geocoder = new Geocoder(callingActivity, Locale.getDefault());

        locationManager = (LocationManager) callingActivity.getSystemService(callingActivity.LOCATION_SERVICE);

        // Specify Location Provider criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);

        locationManager = (LocationManager) callingActivity
                .getSystemService(callingActivity.LOCATION_SERVICE);
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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(CALLINGTAG,"Unable to get value from database");
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
                }
                else
                {
                    Log.d(CALLINGTAG,"Unable to decode byte array");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(CALLINGTAG,"Failed to download profile picture");
            }
        });
    }

    private void getEmail()
    {
        userDatabaseReference
                .child(userID)
                .child("email").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                email = (String) snapshot.getValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(CALLINGTAG,"Unable to get value from database");
            }
        });
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
                        }
                        catch (ClassCastException e)
                        {
                            Long l = (long)snapshot.getValue();
                            rating = l.doubleValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                        Log.e(CALLINGTAG,"Unable to get value from database");
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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(CALLINGTAG,"Unable to get value from database");
            }
        });
    }

    //endregion

}
