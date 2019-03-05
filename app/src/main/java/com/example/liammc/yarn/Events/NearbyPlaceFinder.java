package com.example.liammc.yarn.Events;
import android.os.AsyncTask;
import android.util.Log;

import com.example.liammc.yarn.FinderCallback;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;

import com.example.liammc.yarn.networking.Downloader;
import com.example.liammc.yarn.networking.PlaceDataParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public  class NearbyPlaceFinder extends AsyncTask<Object, String, String> {

    private final String TAG = "NearbyPlaceFinder";
    private final String TYPE_IDENTIFIER = " type = ";

    private String nextPageToken;
    private final YarnUser localUser;
    private final String googlePlaceKey;
    private int searchRadius;
    private FinderCallback listener;


    public NearbyPlaceFinder(String _googlePlaceKey, int _searchRadius
            , FinderCallback _listener)
    {
        this.localUser = LocalUser.getInstance().user;
        this.googlePlaceKey = _googlePlaceKey;
        this.searchRadius = _searchRadius;
        this.listener = _listener;
    }

    @Override
    protected String doInBackground(Object... objects){

        String url = (String)objects[0];
        String placeType = (String)objects[1];

        try {
            StringBuilder data = new StringBuilder(Downloader.readUrl(url));
            nextPageToken = getNextPageToken(data.toString());

            return data.toString() + TYPE_IDENTIFIER + placeType;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s){

        String type =  extractType(s);

        PlaceDataParser parser = new PlaceDataParser();

        List<HashMap<String, String>> nearbyPlaceList = parser.parse(s,type);

        Log.d("nearbyplacesdata","called parse method");

        if(nearbyPlaceList != null) {
            listener.onFoundPlaces(nextPageToken,nearbyPlaceList);
        }
        else listener.onNoPlacesFound("No places were found");
    }

    //region Public Methods

    public void setSearchRadius(int radius){
        searchRadius = radius;
    }

    public void getPlacesNextPage(String nextPageToken) {
        this.execute(buildDataTransferObjectNextPage(nextPageToken));
    }

    public void getNearbyPlaces(String type) {
        this.execute(buildDataTransferObject(type));
    }


    //endregion

    //region Utility

    private Object[] buildDataTransferObject(String placeType) {
        //Build the data transfer object
        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = getPlaceRequestUrl(searchRadius, localUser.lastLocation.getLatitude(),
                localUser.lastLocation.getLongitude(),placeType);
        dataTransfer[1] = placeType;


        return dataTransfer;
    }

    private Object[] buildDataTransferObjectNextPage(String nextPageToken) {
        //Build the data transfer object
        Object dataTransfer[] = new Object[1];
        dataTransfer[0] = getNextPageRequestURL(nextPageToken);

        return dataTransfer;
    }

    private String getPlaceRequestUrl(int radius, double latitude , double longitude , String nearbyPlace) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+radius);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&fields=name,place_id,geometry,reference");
        googlePlaceUrl.append("&key="+googlePlaceKey);

        Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    private String getNextPageRequestURL(String token) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("key="+googlePlaceKey);
        googlePlaceUrl.append("&pagetoken="+token);

        Log.d(TAG,googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    private String getNextPageToken(String jsonData) {
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);

            if(jsonObject.isNull("next_page_token")) return null;

            return jsonObject.getString("next_page_token");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractType(String result)
    {
        String type = "";
        String subString =  result.substring(result.indexOf(TYPE_IDENTIFIER));

        type = subString.replace(TYPE_IDENTIFIER,"");

        return type;
    }
    //endregion

}
