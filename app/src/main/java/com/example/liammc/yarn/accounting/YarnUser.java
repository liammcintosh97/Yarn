package com.example.liammc.yarn.accounting;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.liammc.yarn.Events.ChatFinder;
import com.example.liammc.yarn.Events.Notifier;
import com.example.liammc.yarn.utility.AddressTools;
import com.google.android.gms.maps.GoogleMap;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class YarnUser implements LocationSource, LocationListener
{
    //endregion
    public enum UserType{LOCAL,NETWORK}
    private UserType userType;
    Geocoder geocoder;
    LocationManager locationManager;
    String provider;
    private final StorageReference userStorageReferance;
    private final DatabaseReference userDatabaseReference;
    private final String CALLINGTAG;

    //User info
    public String userID;
    public String userName;
    public Bitmap profilePicture;
    public String email;
    public double rating;
    public boolean termsAcceptance;

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
    public void deactivate(){}

    @Override
    public void activate(OnLocationChangedListener listener){}

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        lastLatLng = new LatLng(lastLocation.getLatitude()
                ,lastLocation.getLongitude());

        lastAddress = AddressTools.getAddressFromLocation(geocoder, lastLatLng);
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

    public void setupUserLocation(Activity callingActivity)
    {
        geocoder = new Geocoder(callingActivity, Locale.getDefault());

        locationManager = (LocationManager) callingActivity
                .getSystemService(callingActivity.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);

        if (ContextCompat.checkSelfPermission(callingActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Location location = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    100, 0, this);

            onLocationChanged(location);
            Notifier.getInstance().onLocationChanged(location);
        }
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
