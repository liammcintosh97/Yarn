package com.example.liammc.yarn.core;

import android.content.Context;
import android.util.Log;

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.utility.DateTools;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;


import java.util.ArrayList;
import java.util.HashMap;

public class Recorder {
    /*The Recorder is used for when the application needs to keep track of certain Chats of places.
    Chats and places need to be recorded when the firebaseUser creates a chat or joins and existing one
     */

    //region singleton pattern
    private static final Recorder instance = new Recorder();

    //private constructor to avoid client applications to use constructor
    private Recorder(){ }

    public static Recorder getInstance(){
        return instance;
    }
    //endregion

    private static String TAG = "Recorder";

    public ArrayList<YarnPlace> recordedYarnPlaces = new ArrayList<>();
    public HashMap<Long,ArrayList<Chat>> recordedChats = new HashMap<>();
    public ArrayList<Chat> chatList = new ArrayList<>();

    private PlacesClient placesClient;

    //region Init

    public void initPlaceClient(Context context){

        // Initialize Places.
        Places.initialize(context, context.getResources().getString(R.string.google_place_android_key));

        // Create a new Places client instance.
        placesClient = Places.createClient(context);
    }
    //endregion

    //region Getters and Setters

    public YarnPlace getYarnPlace(String placeID){

        for (YarnPlace place:recordedYarnPlaces) {
            if(place.placeMap.get("id").equals(placeID))return place;
        }
        return null;
    }

    public Chat getRecordedChat(String chatID){
        /*Loops over the chat list and returns the chat with the matching ID*/

        for (Chat chat: chatList) {
            if(chat.chatID.equals(chatID)) return chat;
        }
        return null;
    }

    //endregion

    //region Public Methods

    public void recordYarnPlace(YarnPlace yarnPlace) {
        /*Adds the passed YarnPlace to the list of recorded YarnPlaces if its not already present*/

        if(recordedYarnPlaces.indexOf(yarnPlace) != -1) return;

        recordedYarnPlaces.add(yarnPlace);
    }

    public void recordYarnPlaces(ArrayList<YarnPlace> places) {
        /*Adds all the YarnPlaces in the passed list to the list of recorded YarnPlaces if
        they are not already present*/

        for (YarnPlace yarnPlace:places) {
            if(recordedYarnPlaces.indexOf(yarnPlace) != -1) return;

            recordedYarnPlaces.add(yarnPlace);
        }
    }

    public void recordChat(Chat chat) {
        /*Adds the passed chat to the recorder's chat list if it isn't already present*/

        //Checks if the chat is present
        if(chatList.indexOf(chat) != -1){
            Log.d(TAG,"Didn't record Chat because it's already recorded");
            return;
        }

        //Gets the date milli from the Chat's date string
        long dateMilli = DateTools.dateStringToMilli(chat.chatDate);
        Log.d(TAG,"Date = " + chat.chatDate  + " | Milli = " + dateMilli);

        //Adds the Chat to recorder's chat list
        if(dateMilli == 0) {
            Log.e(TAG,"Unable to format Date String to Date Milli. Chat wasn't recorded!");
        }
        else{
            chatList.add(chat);
            recordedChats.put(dateMilli, chatList);
        }
    }

    public void removeChat(String chatID){
        /*Gets the chat from the recorded chat list with the Chat ID*/

        Chat chat = getRecordedChat(chatID);
        chatList.remove(chat);
    }

    //endregion

}
