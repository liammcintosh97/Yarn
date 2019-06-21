package com.example.liammc.yarn.finders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.liammc.yarn.interfaces.FinderCallback;
import com.example.liammc.yarn.yarnPlace.YarnPlace;
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
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearbyChatFinder {
    /*This class is required when the firebaseUser needs to find nearby chats to their location*/

    private final String TAG = "NearbyChatFinder";
    private final LocalUser localUser;
    private int searchRadius;
    private FirebaseFunctions firebaseFunctions;
    public DatabaseReference adminRef;
    public ChildEventListener adminRefListener;
    public FinderCallback listener;

    public NearbyChatFinder(int _searchRadius, FinderCallback _listener) {
        this.localUser = LocalUser.getInstance();
        this.searchRadius = _searchRadius;
        this.listener =  _listener;

        this.firebaseFunctions = FirebaseFunctions.getInstance();
    }

    //region Init
    /*This region contains all the initializations methods required by the Nearby Chat Finder*/

    public void initNearbyChatsListener(final ArrayList<String> types){
        /*Initializes the Nearby Chat Listener. This method listens to changes in the database on
        * an admin area level eg(Victoria). If there are any children added,changed,removed,moved
        * or canceled the application will adjust to changes*/

        adminRef = AddressTools.getAdminDatabaseReference(localUser.lastAddress.getCountryName()
                ,localUser.lastAddress.getAdminArea());

        adminRefListener = adminRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                parseFoundChat(dataSnapshot,types);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                parseFoundChat(dataSnapshot,types);
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

    //region Getters and Setters

    public void setSearchRadius(int radius){
        searchRadius = radius;
    }

    //endregion

    //region Public Methods

    public void getNearbyChats(ArrayList<String> types) {
        /*Gets the nearby chats to the firebaseUser*/

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
                //The task is successful
                else{
                    try{
                        //Gets the results from the JSON object
                        JSONObject JSONResult = new JSONObject(task.getResult());
                        String status = JSONResult.getString("status");

                        Log.d(TAG,status);

                        //return the result to listener
                        if(!status.equals("success")){
                            listener.onNoPlacesFound(status);
                        }
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
        /*Parses the JSON data result into a List of HashMaps that is readable by the application*/

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

    private void parseFoundChat(DataSnapshot dataSnapshot,final ArrayList<String> types){
        /*Takes the data snapshot and returns a place map to the firebaseUser if it is within their radius*/

        DataSnapshot placeInfo = dataSnapshot.child(YarnPlace.PLACE_INFO_REF);

        if(placeInfo.getValue() == null){
            Log.d(TAG,"Place info isn't there at this point");
            return;
        }
        //The place info node exists and has value
        else{
            Object latSnap = placeInfo.child("lat").getValue();
            Object lngSnap = placeInfo.child("lng").getValue();
            Object nameSnap = placeInfo.child("place_name").getValue();
            Object typeSnap = placeInfo.child("place_type").getValue();

            //Check if all the required data is in the database
            if(latSnap != null && lngSnap != null && nameSnap != null && typeSnap != null){

                String placeId = dataSnapshot.getKey();
                LatLng placeLatLng =  new LatLng((double)latSnap,(double)lngSnap);

                //The distance is greater then
                if(MathTools.latLngDistance(placeLatLng.latitude,placeLatLng.longitude
                        ,localUser.lastLatLng.latitude,localUser.lastLatLng.longitude)
                        > searchRadius) return;

                //The types don't match
                if(!checkTypeEquality((String)typeSnap,types)) return;

                /*If it's within the radius and matches the firebaseUser's chosen types return the
                place map to the listener*/
                HashMap<String, String> placeMap = YarnPlace.buildPlaceMap(placeId,(String)nameSnap,
                        (String)typeSnap,String.valueOf(placeLatLng.latitude)
                        ,String.valueOf(placeLatLng.longitude));

                listener.onFoundPlace(placeMap);
            }
            else{
                Log.d(TAG,"Not all the required data is at this location at this point");
                return;
            }
        }
    }

    private Task<String> requestNearbyChats(ArrayList<String> types) {
        /*Calls the Firebase cloud function for getting nearby chats*/

        Log.d(TAG,"Getting nearby chats");
        return firebaseFunctions
                .getHttpsCallable("getNearbyChats")
                .call(buildParameters(types))
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {

                        HashMap resultMap =  (HashMap) task.getResult().getData();
                        JSONObject JSONResult = new JSONObject(resultMap);

                        return JSONResult.toString();
                    }
                });
    }

    private boolean checkTypeEquality(String placeType, ArrayList<String> types) {
        /*Checks if one of the types in the list matches the passed type*/

        for(int i = 0; i < types.size(); i++)
        {
            if(types.get(i).equals(placeType)){
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> buildParameters(ArrayList<String> types){
        /*Builds and returns parameters used in the Nearby Chats request*/

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("country", localUser.lastAddress.getCountryName());
        parameters.put("state", localUser.lastAddress.getAdminArea());
        parameters.put("types", types);
        parameters.put("search_radius", searchRadius);
        parameters.put("lat", localUser.lastLatLng.latitude);
        parameters.put("lng", localUser.lastLatLng.longitude);

        return parameters;
    }

    //endregion
}
