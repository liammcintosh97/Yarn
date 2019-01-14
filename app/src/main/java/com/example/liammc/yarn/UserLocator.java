package com.example.liammc.yarn;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.example.liammc.yarn.accounting.YarnUser;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserLocator extends IntentService
{
    //region Geo Coder Constaints

    public final class GeoCoderConstants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
        public static final String LATLNG_DATA_EXTRA = PACKAGE_NAME +
                ".LATLNG_DATA_EXTRA";
    }

    //endregion

    private final String TAG = "UserLocator";
    protected ResultReceiver mReceiver;

    public Location currentLocation;
    public LatLng curLatLng;
    public LocationManager locationManager;

    GoogleMap mMap;

    public UserLocator(GoogleMap _mMap)
    {
        super("UserLocator");

        this.mMap = _mMap;
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (intent == null) {
            return;
        }

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                GeoCoderConstants.LOCATION_DATA_EXTRA);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            Log.e(TAG, "GeoCoder service not avaliable", ioException);

        } catch (IllegalArgumentException illegalArgumentException) {

            Log.e(TAG, "Invalid lat lng used" + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {

            Log.e(TAG, "No addresses found");
            deliverResultToReceiver(GeoCoderConstants.FAILURE_RESULT, "No addresses found");
        } else {

            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address found");
            deliverResultToReceiver(GeoCoderConstants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(GeoCoderConstants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    private void getlastKnowLocation(Criteria criteria)
    {
        //Check if we have permission to access to the user's location
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            currentLocation = locationManager.getLastKnownLocation(
                    locationManager.getBestProvider(criteria, false));

            curLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

            if(!mMap.isMyLocationEnabled()) mMap.setMyLocationEnabled(true);
        }
    }
}
