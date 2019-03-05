package com.example.liammc.yarn.Events;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.FinderCallback;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearbyChatFinder {

    private final String TAG = "NearbyChatFinder";
    private final YarnUser localUser;
    private int searchRadius;
    private FirebaseFunctions firebaseFunctions;

    private FinderCallback listener;

    public NearbyChatFinder(int _searchRadius, FinderCallback _listener)
    {
        this.localUser = LocalUser.getInstance().user;
        this.searchRadius = _searchRadius;
        this.listener =  _listener;

        this.firebaseFunctions = FirebaseFunctions.getInstance();
    }

    //region Public Methods

    public void setSearchRadius(int radius){
        searchRadius = radius;
    }

    public void getNearbyChats(ArrayList<String> types)
    {
        requestNearbyChats(types).addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                //The task is a failure
                if (!task.isSuccessful()) {

                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        Object details = ffe.getDetails();

                        Log.e(TAG, "Error trying to receive chats - " + code + " "
                                + details);
                    }
                    Log.w(TAG, "getNearbyChats:onFailure", e);

                    listener.onNoPlacesFound("Error finding chats");
                    return;
                }
                //The task isn't a failure
                else{

                    try{
                        JSONObject JSONResult = new JSONObject(task.getResult());
                        String status = JSONResult.getString("status");

                        if(!status.equals("success")) listener.onNoPlacesFound(status);
                        else{
                            List<HashMap<String,String>> placeMaps = parse(JSONResult);
                            listener.onFoundPlaces(null,placeMaps);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG,e.getMessage());
                        listener.onNoPlacesFound("Error finding Chats");
                    }
                }
            }
        });
    }

    //endregion

    //region Private Methods

    private List<HashMap<String,String>> parse(JSONObject resultJSON){

        List<HashMap<String,String>> placeMaps = new ArrayList<>();

        try {
            JSONArray JSONArray = resultJSON.getJSONArray("result");

            for(int i = 0 ; i < JSONArray.length(); i++)
            {
                JSONObject JSONObject = JSONArray.getJSONObject(i);

                String id = JSONObject.getString("id");
                String name = JSONObject.getString("name");
                String type = JSONObject.getString("type");
                String lat = JSONObject.getString("lat");
                String lng = JSONObject.getString("lng");

                HashMap<String,String> placeMap = buildPlaceMap(id,name,type,lat,lng);

                placeMaps.add(placeMap);
            }

        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return placeMaps;
    }

    private Task<String> requestNearbyChats(ArrayList<String> types)
    {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("country", localUser.lastAddress.getCountryName());
        parameters.put("state", localUser.lastAddress.getAdminArea());
        parameters.put("types", types);
        parameters.put("search_radius", searchRadius);
        parameters.put("lat", localUser.lastLatLng.latitude);
        parameters.put("lng", localUser.lastLatLng.longitude);

        Log.d(TAG,"Getting nearby chats");
        return firebaseFunctions
                .getHttpsCallable("getNearbyChats")
                .call(parameters)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {

                        HashMap resultMap =  (HashMap) task.getResult().getData();
                        JSONObject JSONResult = new JSONObject(resultMap);

                        return JSONResult.toString();
                    }
                });
    }

    private HashMap<String, String> buildPlaceMap(String id, String name,String type, String lat, String lng){

        HashMap<String, String > placeMap = new HashMap<>();
        placeMap.put("id",id);
        placeMap.put("name", name);
        placeMap.put("type",type);
        placeMap.put("lat", lat);
        placeMap.put("lng", lng);

        return placeMap;
    }

    //endregion
}
