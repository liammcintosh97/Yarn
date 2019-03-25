package com.example.liammc.yarn.Events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.liammc.yarn.FinderCallback;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.MathTools;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private DatabaseReference adminRef;

    private FinderCallback listener;

    //TODO get nearby chats as they are added to the database
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

    public void setNearbyChatsListener(final ArrayList<String> types){

        adminRef = AddressTools.getAdminDatabaseReference(localUser.lastAddress.getCountryName()
                ,localUser.lastAddress.getAdminArea());

        adminRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                DataSnapshot placeInfo = dataSnapshot.child("Yarn_Place_Info");

                double lat = (double)placeInfo.child("lat").getValue();
                double lng = (double)placeInfo.child("lng").getValue();

                String placeId = dataSnapshot.getKey();
                String placeName = (String) placeInfo.child("place_name").getValue();
                LatLng placeLatLng =  new LatLng(lat,lng);
                String placeType =  (String) placeInfo.child("place_type").getValue();

                //The distance is greater then
                if(MathTools.latLngDistance(placeLatLng.latitude,placeLatLng.longitude
                        ,localUser.lastLatLng.latitude,localUser.lastLatLng.longitude)
                        > searchRadius) return;

                //The types don't match
                if(!checkTypeEquality(placeType,types)) return;

                HashMap<String, String> placeMap = YarnPlace.buildPlaceMap(placeId,placeName,placeType
                        ,String.valueOf(placeLatLng.latitude),String.valueOf(placeLatLng.longitude));

                listener.onFoundPlace(placeMap);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                HashMap<String,String> placeMap = YarnPlace.buildPlaceMap(id,name,type,lat,lng);

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

    //endregion

    //region Utility

    private boolean checkTypeEquality(String placeType, ArrayList<String> types)
    {
        for(int i = 0; i < types.size(); i++)
        {
            if(types.get(i).equals(placeType)){
                return true;
            }
        }
        return false;
    }

    //endregion
}
