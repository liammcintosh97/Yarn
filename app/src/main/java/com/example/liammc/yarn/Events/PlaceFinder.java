package com.example.liammc.yarn.Events;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.networking.Downloader;
import com.example.liammc.yarn.networking.PlaceDataParser;
import com.google.android.gms.maps.GoogleMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PlaceFinder extends AsyncTask<Object, String, String> {

    //region Callback Interface
    public interface PlaceFinderCallback
    {
        void onFoundPlaces(List<YarnPlace> yarnPlaces);
    }
    //endregion

    private List<YarnPlace> yarnPlaces = new ArrayList<>();

    private MapsActivity mapsActivity;
    private String googlePlacesData;
    private GoogleMap mMap;
    private String placeType;

    private PlaceFinderCallback listener;

    public PlaceFinder(MapsActivity _mapsActivity, PlaceFinderCallback listener){
        this.listener = listener;
        this.mapsActivity = _mapsActivity;
    }

    @Override
    protected String doInBackground(Object... objects){

        mMap = (GoogleMap)objects[0];
        String url = (String)objects[1];
        placeType = (String)objects[2];

        try {
            googlePlacesData = Downloader.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s){

        PlaceDataParser parser = new PlaceDataParser();

        List<HashMap<String, String>> nearbyPlaceList = parser.parse(s);

        Log.d("nearbyplacesdata","called parse method");

        createYarnPlaces(nearbyPlaceList);
    }

    private void createYarnPlaces(List<HashMap<String, String>> nearbyPlaceList)
    {
        for(int i = 0; i < nearbyPlaceList.size(); i++)
        {
            HashMap<String, String> placeMap = nearbyPlaceList.get(i);
            yarnPlaces.add(new YarnPlace(mapsActivity,mMap,placeMap,placeType));
        }

        listener.onFoundPlaces(yarnPlaces);
    }
}

