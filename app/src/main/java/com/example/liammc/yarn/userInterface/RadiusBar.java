package com.example.liammc.yarn.userInterface;

import android.widget.SeekBar;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.core.MapsActivity;

public class RadiusBar {

    private final String TAG = "RadiusBar";

    SeekBar radiusBar;
    CameraController cameraController;
    SearchRadius searchRadius;
    MapsActivity mapsActivity;
    LocalUser localUser;

    public RadiusBar(MapsActivity _mapsActivity,SearchRadius _search){
        this.mapsActivity = _mapsActivity;
        this.searchRadius = _search;
        this.localUser = LocalUser.getInstance();

    }

    //region Init

    public void init(CameraController _cam){
        radiusBar = mapsActivity.findViewById(R.id.radiusBar);

        radiusBar.setMax(LocalUser.SEARCH_RADIUS_MAX);
        radiusBar.setProgress(LocalUser.SEARCH_RADIUS_DEFAULT);

        cameraController = _cam;
        cameraController.moveToLatLng(localUser.lastLatLng,(
                int)calculateCameraZoom());

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //Clamp the progress bar to a minimum
                if(progress < LocalUser.SEARCH_RADIUS_MIN){
                    progress = LocalUser.SEARCH_RADIUS_MIN;
                    seekBar.setProgress(progress);
                }

                localUser.searchRadius = progress;

                searchRadius.update(progress,localUser.lastLatLng);

                double zoom = calculateCameraZoom();
                cameraController.zoomTo((float)zoom);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    //endregion

    //region Public Methods

    public double calculateCameraZoom(){

        double radiusPer = ((double) radiusBar.getProgress()/(double) LocalUser.SEARCH_RADIUS_MAX)
                * (double) 100;
        double zoomPercantage =  100 - radiusPer;
        int zoomRange = CameraController.ZOOM_MAX - CameraController.ZOOM_MIN;

        return ((zoomPercantage * zoomRange)/100) + CameraController.ZOOM_MIN;
    }

    //endregion
}
