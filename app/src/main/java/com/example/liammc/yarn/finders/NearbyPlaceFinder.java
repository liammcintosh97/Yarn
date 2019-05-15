package com.example.liammc.yarn.finders;
import android.os.AsyncTask;
import android.util.Log;

import com.example.liammc.yarn.interfaces.FinderCallback;
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
    /*This class is used when the firebaseUser wants to find near by places for possible or existing chats*/

    private final String TAG = "NearbyPlaceFinder";
    private final String TYPE_IDENTIFIER = " type = ";

    private String nextPageToken;
    private final LocalUser localUser;
    private final String googlePlaceKey;
    private int searchRadius;
    private FinderCallback listener;


    public NearbyPlaceFinder(String _googlePlaceKey, int _searchRadius
            , FinderCallback _listener) {

        this.localUser = LocalUser.getInstance();
        this.googlePlaceKey = _googlePlaceKey;
        this.searchRadius = _searchRadius;
        this.listener = _listener;
    }

    //region Async Methods
    /*This region holds the methods that come from extending AsyncTask*/

    @Override
    protected String doInBackground(Object... objects){
        /*This is want runs during the async task execution*/

        //Get the url and place type from the results
        String url = (String)objects[0];
        String placeType = (String)objects[1];

        //Download the data from the url and get the next page token
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
        /*This is want runs after the task has been completed*/

        //get the type from the returned string
        String type =  extractType(s);

        //Parse the returned string into a readable format
        PlaceDataParser parser = new PlaceDataParser();
        List<HashMap<String, String>> nearbyPlaceList = parser.parse(s,type);

        //Return the result to the listener
        if(nearbyPlaceList != null) {
            listener.onFoundPlaces(nextPageToken,nearbyPlaceList);
        }
        else listener.onNoPlacesFound("No places were found");
    }
    //endregion

    //region Getters and Setters

    public void setSearchRadius(int radius){ searchRadius = radius; }

    //endregion

    //region Public Methods

    public void getPlacesNextPage(String nextPageToken) {
        /*Gets the nearby places on the next page of the results */
        execute(buildNextPageTransfer(nextPageToken));
    }

    public void getNearbyPlaces(String type) {
        /*Gets the nearby places*/
       execute(buildTransfer(type));
    }

    //endregion

    //region Private methods

    private Object[] buildTransfer(String placeType) {
        //Build the data transfer object
        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = buildRequestUrl(searchRadius, localUser.lastLocation.getLatitude(),
                localUser.lastLocation.getLongitude(),placeType);
        dataTransfer[1] = placeType;


        return dataTransfer;
    }

    private Object[] buildNextPageTransfer(String nextPageToken) {
        //Builds the next page data transfer object

        Object dataTransfer[] = new Object[1];
        dataTransfer[0] = buildPageRequestURL(nextPageToken);

        return dataTransfer;
    }

    private String buildRequestUrl(int radius, double latitude , double longitude , String nearbyPlace) {
        /*Builds the required URL string for the request*/

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+radius);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&fields=name,place_id,geometry,reference");
        googlePlaceUrl.append("&key="+googlePlaceKey);

        return googlePlaceUrl.toString();
    }

    private String buildPageRequestURL(String token) {
        /*Get the next page URL from the returned JSON object*/

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("key="+googlePlaceKey);
        googlePlaceUrl.append("&pagetoken="+token);

        Log.d(TAG,googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    private String getNextPageToken(String jsonData) {
        /*Get the next page token from the returned JSON object*/
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

    private String extractType(String result) {
        //Gets the type from the returned results string

        String type = "";
        String subString =  result.substring(result.indexOf(TYPE_IDENTIFIER));

        type = subString.replace(TYPE_IDENTIFIER,"");

        return type;
    }
    //endregion

}
