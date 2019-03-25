package com.example.liammc.yarn.core;

import android.content.Context;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.Events.Chat;
import com.example.liammc.yarn.Events.Notifier;
import com.example.liammc.yarn.Events.YarnPlace;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.utility.DateTools;
import com.example.liammc.yarn.utility.ReadyListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recorder
{
    //region Ready Listener

    private ReadyListener readyListener;

    public ReadyListener getReadyListener() {
        return readyListener;
    }

    public void setReadyListener(ReadyListener readyListener) {
        this.readyListener = readyListener;
    }
    //endregion

    //region singleton pattern
    private static final Recorder instance = new Recorder();

    //private constructor to avoid client applications to use constructor
    private Recorder(){
        this.notifier = Notifier.getInstance();
        this.firebaseFunctions = FirebaseFunctions.getInstance();
    }

    public static Recorder getInstance(){
        return instance;
    }
    //endregion

    private static String TAG = "Recorder";

    public ArrayList<YarnPlace> recordedYarnPlaces = new ArrayList<>();
    public HashMap<Long,ArrayList<Chat>> recordedChats = new HashMap<>();
    public ArrayList<Chat> chatList = new ArrayList<>();
    private Notifier notifier;
    private YarnUser localUser;
    private FirebaseFunctions firebaseFunctions;
    private PlacesClient placesClient;

    //region Public Methods

    public void setLocalUser(YarnUser _localUser){
        localUser = _localUser;
    }

    public void initPlaceClient(Context context){

        // Initialize Places.
        Places.initialize(context, context.getResources().getString(R.string.google_place_android_key));

        // Create a new Places client instance.
        placesClient = Places.createClient(context);
    }

    public YarnPlace getYarnPlace(String placeID){

        for (YarnPlace place:recordedYarnPlaces) {
            if(place.placeMap.get("id").equals(placeID))return place;
        }
        return null;
    }

    public void recordYarnPlace(YarnPlace yarnPlace)
    {
        if(recordedYarnPlaces.indexOf(yarnPlace) != -1) return;

        recordedYarnPlaces.add(yarnPlace);
    }

    public void recordYarnPlaces(ArrayList<YarnPlace> places)
    {
        for (YarnPlace yarnPlace:places) {
            if(recordedYarnPlaces.indexOf(yarnPlace) != -1) return;

            recordedYarnPlaces.add(yarnPlace);
        }
    }

    public void recordChat(Context context, Chat chat)
    {
        if(chatList.indexOf(chat) != -1) return;

        chatList.add(chat);

        long dateMilli = DateTools.dateStringToMilli(chat.chatDate);
        Log.d(TAG,"Date = " + chat.chatDate  + " | Milli = " + dateMilli);

        if(dateMilli != 0)
        {
            recordedChats.put(dateMilli, chatList);
        }

        notifier.listenToChat(context,chat);
    }

    public void getJoinedYarnPlaces(final Context context, final Geocoder geocoder){

        requestJoinedChats().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                //The task is a failure
                if (!task.isSuccessful()) {

                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        Object details = ffe.getDetails();

                        Log.e(TAG, "Error trying to receive joined Yarn Places - " + code + " "
                                + details);
                    }
                    Log.w(TAG, "Get Joined Yarn Places failed - ", e);

                    return;
                }
                //The task isn't a failure
                else{
                    try{
                        JSONObject JSONResult = new JSONObject(task.getResult());
                        String status = JSONResult.getString("status");

                        if(!status.equals("success")) Log.w(TAG, "Getting joined Yarn Places  failed - "
                                + JSONResult.toString());
                        else{
                            //Build chat objects and record them
                            parse(context,geocoder,JSONResult);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG,e.getMessage());
                    }
                }
            }
        });
    }

    public Chat getRecordedChat(String chatID){

        for (Chat chat: chatList) {
            if(chat.chatID.equals(chatID)) return chat;
        }
        return null;
    }

    //endregion

    //region Private Methods

    private Task<String> requestJoinedChats()
    {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("country", localUser.lastAddress.getCountryName());
        parameters.put("state", localUser.lastAddress.getAdminArea());
        parameters.put("userID", localUser.userID);

        Log.d(TAG,"Getting joined Yarn Places");
        return firebaseFunctions
                .getHttpsCallable("getJoinedPlaces")
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

    private void parse(Context context,Geocoder geocoder,final JSONObject resultJSON){

        try {
            final JSONArray JSONArray = resultJSON.getJSONArray("result");
            final ArrayList<YarnPlace> foundPlaces = new ArrayList<>();

            for(int i = 0 ; i < JSONArray.length(); i++)
            {
                JSONObject JSONObject = JSONArray.getJSONObject(i);

                String id = JSONObject.getString("id");
                String name = JSONObject.getString("name");
                String type = JSONObject.getString("type");
                String lat = JSONObject.getString("lat");
                String lng = JSONObject.getString("lng");

                final HashMap<String,String> placeMap = YarnPlace.buildPlaceMap(id,name,type,lat,lng);

                final YarnPlace yarnPlace = new YarnPlace(placeMap);
                yarnPlace.init(context,geocoder);

                yarnPlace.setReadyListener(new ReadyListener() {
                    @Override
                    public void onReady() {

                        foundPlaces.add(yarnPlace);

                        if(checkReady(foundPlaces, JSONArray.length())){
                            recordYarnPlaces(foundPlaces);
                            Log.d(TAG,"Recorder is ready!");
                            if(readyListener != null) readyListener.onReady();
                        }
                    }
                });
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //endregion

    //region utility

    private boolean checkReady(ArrayList<YarnPlace> list,int amount){

        return amount == list.size();
    }

    //endregion
}
