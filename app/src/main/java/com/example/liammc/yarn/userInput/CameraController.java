package com.example.liammc.yarn.userInput;



import android.view.Gravity;

import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class CameraController {

    private final String TAG = "CameraController";
    public static final int ZOOM_MIN = 11;
    public static final int ZOOM_MAX = 14;

    private GoogleMap mMap;
    private LocalUser localUser;
    private RadiusBar radiusBar;

    public CameraController(GoogleMap _mMap,RadiusBar _radiusBar){
        this.mMap = _mMap;
        this.radiusBar = _radiusBar;
        this.localUser = LocalUser.getInstance();
    }

    //region Public Methods

    public void focusOnUser(MapsActivity mapsActivity){

        FusedLocationProviderClient mFusedLocationProviderClient
                = LocationServices.getFusedLocationProviderClient(mapsActivity);

        localUser.getUserLocation(mapsActivity, mFusedLocationProviderClient, new LocalUser.locationReceivedListener() {
            @Override
            public void onLocationReceived(LatLng latLng) {
                if(radiusBar != null) moveToLatLng(latLng, (int)radiusBar.calculateCameraZoom());
            }
        });
    }

    public void focusOnYarnPlace(YarnPlace yarnPlace){

        if(yarnPlace != null){
            if(yarnPlace.infoWindow.isShowing()) yarnPlace.infoWindow.dismiss();
        }

        moveToLatLng(yarnPlace.marker.getPosition(),15);
        yarnPlace.infoWindow.show(mMap, Gravity.START | Gravity.LEFT | Gravity.TOP);
    }

    public void moveToLatLng(LatLng latLng, int zoom) {
        /*Focuses the camera on a LatLng position*/

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .tilt(0)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void zoomTo(float zoom){
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    //endregion

}
