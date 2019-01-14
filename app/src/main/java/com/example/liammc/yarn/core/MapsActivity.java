package com.example.liammc.yarn.core;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.liammc.yarn.Events.Chat;
import com.example.liammc.yarn.Events.ChatCreator;
import com.example.liammc.yarn.Events.PlaceFinder;
import com.example.liammc.yarn.Events.YarnPlace;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.UserLocator;
import com.example.liammc.yarn.accounting.YarnUser;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        PlaceFinder.PlaceFinderCallback {

    //region Address Result Receiver

    class AddressResultReceiver extends ResultReceiver {

        public String mAddressOutput;


        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.

            mAddressOutput = resultData.getString(UserLocator.GeoCoderConstants.RESULT_DATA_KEY);

            if (mAddressOutput == null) {
                mAddressOutput = "";
            }
        }
    }

    //endregion

    private final int PROXIMITY_RADIUS = 10000;
    private final String TAG = "MapsActivity";

    //Google Services
    GoogleMap mMap;
    LocationManager locationManager;
    PlaceDetectionClient mPlaceDetectionClient;
    GeoDataClient mGeoDataClient;
    ChatCreator chatCreator;
    AddressResultReceiver mResultReceiver;

    //Map Data
    public YarnUser localUser;
    public Chat activeChat;
    List<YarnPlace> bars;
    List<YarnPlace> cafes;
    List<YarnPlace> resturants;

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

        YarnUser localuser = new YarnUser(this,
                FirebaseAuth.getInstance().getCurrentUser().getUid());

        initializeMapServices();
        focusOnUser(mMap);
        applyMapStyle();

        getBars();
        getCafes();
        getRestaurants();
    }

    @Override
    public void onBackPressed()
    {
        if(chatCreator.window.isShowing())
        {
            chatCreator.dissmissChatCreator();
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
        ViewGroup parentViewGroup = findViewById(R.id.map);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mPlaceDetectionClient =  Places.getPlaceDetectionClient(this);
        mGeoDataClient = Places.getGeoDataClient(this);
        chatCreator = new ChatCreator(this,parentViewGroup);

        setMarkerListener();
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
                YarnPlace touchedPlace;

                //Loop through all places to find the equal marker
                touchedPlace = searchMarkerInList(bars,marker);
                if(touchedPlace == null) touchedPlace = searchMarkerInList(cafes,marker);
                if(touchedPlace == null) touchedPlace = searchMarkerInList(resturants,marker);

                if(touchedPlace != null)
                {
                    Log.d(TAG,"Found the correct Yarn place from marker click");

                    chatCreator.showChatCreator(touchedPlace);
                }
                else{
                    Log.e(TAG,"Fatal error, couldn't find touched marker in Yarn places lists");
                }
                    return true;
            }
        });
    }

    protected void startUserLocatorService() {
        Intent intent = new Intent(this, UserLocator.class);
        intent.putExtra(UserLocator.GeoCoderConstants.RECEIVER, mResultReceiver);
        intent.putExtra(UserLocator.GeoCoderConstants.LOCATION_DATA_EXTRA, localUser.lastLocation);
        startService(intent);
    }

    //endregion

    //region local methods

    private void focusOnUser(GoogleMap map)
    {
        getLastKnowLocation(new Criteria());

        if (localUser.lastLocation != null)
        {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(localUser.lastLatLng, 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(localUser.lastLatLng)          // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

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

        PlaceFinder barFinder = new PlaceFinder(new PlaceFinder.PlaceFinderCallback() {
            @Override
            public void onFoundPlaces(List<YarnPlace> yarnPlaces) {
                bars = yarnPlaces;
            }
        });

        Object barDataTransfer[] = new Object[2];
        barDataTransfer[0] = mMap;
        barDataTransfer[1] = getPlaceRequestUrl(localUser.lastLocation.getLatitude(),
                localUser.lastLocation.getLongitude(), "bar");

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
        PlaceFinder cafeFinder = new PlaceFinder(new PlaceFinder.PlaceFinderCallback() {
            @Override
            public void onFoundPlaces(List<YarnPlace> yarnPlaces)
            {
                cafes = yarnPlaces;
            }
        });

        //Build the data transfer object
        Object cafeDataTransfer[] = new Object[2];
        cafeDataTransfer[0] = mMap;
        cafeDataTransfer[1] = getPlaceRequestUrl(localUser.lastLocation.getLatitude(),
                localUser.lastLocation.getLongitude(), "cafe");

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
        PlaceFinder resturantFinder = new PlaceFinder(new PlaceFinder.PlaceFinderCallback() {
            @Override
            public void onFoundPlaces(List<YarnPlace> yarnPlaces)
            {
                resturants = yarnPlaces;
            }
        });

        //Build the data transfer object
        Object restaurantDataTransfer[] = new Object[2];
        restaurantDataTransfer[0] = mMap;
        restaurantDataTransfer[1] = getPlaceRequestUrl(localUser.lastLocation.getLatitude(),
                localUser.lastLocation.getLongitude(), "restaurant");

        //Execute the place finder and get the desired places
        resturantFinder.execute(restaurantDataTransfer);
    }


    //endregion

    //region Utility

    private String getPlaceRequestUrl(double latitude , double longitude , String nearbyPlace)
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
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

    private void getLastKnowLocation(Criteria criteria)
    {
        //Check if we have permission to access to the user's location
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            localUser.lastLocation = locationManager.getLastKnownLocation(
                    locationManager.getBestProvider(criteria, false));

            localUser.lastLatLng = new LatLng(localUser.lastLocation.getLatitude(),
                    localUser.lastLocation.getLongitude());

            if(!mMap.isMyLocationEnabled()) mMap.setMyLocationEnabled(true);
        }
    }

    //endregion
}
