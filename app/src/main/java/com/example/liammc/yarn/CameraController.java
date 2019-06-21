package com.example.liammc.yarn;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class CameraController {

    private final String TAG = "CameraController";
    public static final int ZOOM_MIN = 11;
    public static final int ZOOM_MAX = 14;

    private GoogleMap mMap;

    public CameraController(GoogleMap _mMap){
        this.mMap = _mMap;
    }

    //region Public Methods

    public void moveToLatLng(LatLng latLng, int zoom) {
        /*Focuses the camera on a LatLng position*/

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .tilt(40)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void zoomTo(float zoom){
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }
    //endregion
}
