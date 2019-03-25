package com.example.liammc.yarn.Events;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;

import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.ReadyListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.UUID;

public class Chat //implements Parcelable
{
    //region Value Change Listener
    private ValueChangeListener valueChangelistener;

    public interface ValueChangeListener {
        void onAcceptedChange();
        void onActiveChange();
        void onCanceledChange();
    }

    public ValueChangeListener getValueChangelistener() {
        return valueChangelistener;
    }

    public void setValueChangelistener(ValueChangeListener valueChangelistener) {
        this.valueChangelistener = valueChangelistener;
    }
    //endregion

    //region Ready Listener

    private ChatReadyListener readyListener;

    public interface ChatReadyListener {
        void onReady(Chat chat);
    }

    public ChatReadyListener getReadyListener() {
        return readyListener;
    }

    public void setReadyListener(ChatReadyListener readyListener) {
        this.readyListener = readyListener;
    }

    //endregion

    public static final String PLACE_INFO_REF = "Yarn_Place_Info";
    private final DatabaseReference chatRef;
    private final DatabaseReference placeInfoRef;
    private final DatabaseReference placeRef;
    private  String TAG = "Chat" ;
    private final String localUserID;

    //public String chatID;
    public YarnUser hostUser;
    public YarnUser guestUser;
    private boolean guestReady  = false;

    public YarnPlace yarnPlace;
    public String chatID;
    public String chatDate;
    public String chatTime;
    public String chatLength;

    public Boolean chatAccepted = null;
    public Boolean chatActive = null;
    public Boolean chatCanceled = null;

    //region Constructors

    public Chat(YarnPlace _place,HashMap<String, String> chatMap, ChatReadyListener _readyListener)
    {
        //This is the constructor for creating an instance of a new chat
        this.localUserID = LocalUser.getInstance().user.userID;

        //Initialize chat variables
        this.hostUser = new YarnUser(chatMap.get("host"),YarnUser.UserType.LOCAL);

        this.yarnPlace = _place;
        this.chatID = this.generateChatID();
        this.TAG = TAG + " " + chatID;
        this.chatDate = chatMap.get("date");
        this.chatTime = chatMap.get("time");
        this.chatLength = chatMap.get("length");

        this.initializeChatState();

        //Initialize database references
        this.chatRef = AddressTools.getChatDatabaseReference(
                _place.address.getCountryName(),_place.address.getAdminArea(),
                _place.placeMap.get("id"),this.chatID);

        this.placeInfoRef = AddressTools.getPlaceInfoDatabaseReference(
                _place.address.getCountryName(),_place.address.getAdminArea(),
                _place.placeMap.get("id"),this.PLACE_INFO_REF);

        this.placeRef = AddressTools.getPlaceDatabaseReference(
                _place.address.getCountryName(),_place.address.getAdminArea(),
                _place.placeMap.get("id"));

        //Initialize database
        this.initializeYarnPlaceDatabase();
        this.initializeChatDatabase();

        //Set ready listener;
        this.readyListener = _readyListener;
    }

    public Chat(YarnPlace _place, String _chatID)
    {
        //This is the constructor for creating an instance of an exsisting chat
        this.TAG = TAG + " " + _chatID;
        this.yarnPlace = _place;
        this.localUserID = LocalUser.getInstance().user.userID;
        this.chatID = _chatID;

        //Initialize database references
        this.chatRef = AddressTools.getChatDatabaseReference(
                _place.address.getCountryName(),_place.address.getAdminArea(),
                _place.placeMap.get("id"),this.chatID);

        this.placeInfoRef = AddressTools.getPlaceInfoDatabaseReference(
                _place.address.getCountryName(),_place.address.getAdminArea(),
                _place.placeMap.get("id"),this.PLACE_INFO_REF);

        this.placeRef = AddressTools.getPlaceDatabaseReference(
                _place.address.getCountryName(),_place.address.getAdminArea(),
                _place.placeMap.get("id"));


        //Get Chat info
        this.addDataListener(chatRef,"host");
        this.addDataListener(chatRef,"guest");
        this.addDataListener(chatRef,"time");
        this.addDataListener(chatRef,"date");
        this.addDataListener(chatRef,"length");
        this.addDataListener(chatRef,"accepted");
        this.addDataListener(chatRef,"active");
        this.addDataListener(chatRef,"canceled");

    }

    //endregion Constructors

    //region Initialization
    private void initializeChatState()
    {
        //Initialize chat state
        chatAccepted = false;
        chatActive = false;
        chatCanceled = false;
    }

    private void initializeChatDatabase()
    {
        setData(chatRef,"host",hostUser.userID);
        setData(chatRef,"guest","");
        setData(chatRef,"date",chatDate);
        setData(chatRef,"time",chatTime);
        setData(chatRef,"length",chatLength);
        setData(chatRef,"accepted", chatAccepted);
        setData(chatRef,"active", chatActive);
        setData(chatRef,"canceled", chatCanceled);

        addDataListener(chatRef,"host");
        addDataListener(chatRef,"guest");
        addDataListener(chatRef,"date");
        addDataListener(chatRef,"time");
        addDataListener(chatRef,"length");
        addDataListener(chatRef,"accepted");
        addDataListener(chatRef,"active");
        addDataListener(chatRef,"canceled");
    }

    private void initializeYarnPlaceDatabase()
    {
        final String placeName = yarnPlace.placeMap.get("name");
        final Double lat = yarnPlace.latLng.latitude;
        final Double lng = yarnPlace.latLng.longitude;
        final String country = yarnPlace.address.getCountryName();
        final String admin1 = yarnPlace.address.getAdminArea();
        final String admin2 = yarnPlace.address.getSubAdminArea();
        final String locality = yarnPlace.address.getLocality();
        final String street = yarnPlace.address.getAddressLine(0);
        final String postCode = yarnPlace.address.getPostalCode();
        final String type = yarnPlace.placeType;

        //Check if the database has an info node for this place
        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(PLACE_INFO_REF)){
                    setData(placeInfoRef,"place_name",placeName);
                    setData(placeInfoRef,"lat",lat);
                    setData(placeInfoRef,"lng",lng);
                    setData(placeInfoRef,"country",country);
                    setData(placeInfoRef,"admin1",admin1);
                    setData(placeInfoRef,"admin2",admin2);
                    setData(placeInfoRef,"locality",locality);
                    setData(placeInfoRef,"street",street);
                    setData(placeInfoRef,"postcode",postCode);
                    setData(placeInfoRef,"place_type",type);

                    Log.d(TAG,"Created the place info node");
                }
                else {
                    Log.d(TAG,"The place info node already exists");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //endregion

    //region Public Local Methods

    public void acceptChat(Context context, YarnUser guestUser)
    {
        chatAccepted = true;
        setData(chatRef,"guest",guestUser.userID);
        setData(chatRef,"chatAccepted",true);
        setData(chatRef,"chatActive", chatActive);

        Recorder.getInstance().recordChat(context,this);
    }

    public void cancelChat()
    {
        chatActive = false;
        chatCanceled = true;

        setData(chatRef,"chatActive", chatActive);
        setData(chatRef,"chatCanceled", chatCanceled);
    }

    //endregion

    //region Private Local Methods

    private void setData(DatabaseReference ref,final String dataType, final Object dataValue)
    {
        DatabaseReference dataRef = ref.child(dataType);

        //Write to the User database user name
        dataRef.setValue(dataValue)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Log.d(TAG,dataType +" write to database was a success :"
                                + dataValue.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d(TAG,dataType +"write to database was a failure -" + e);
                    }
                });
    }

    private void addDataListener(final DatabaseReference ref,final String dataType)
    {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(translateDatabase(dataType,snapshot))
                {
                    Log.d(TAG,"Updated " + dataType + " from database : "
                            + snapshot.getValue().toString());

                    checkReady();
                }
                else Log.e(TAG,"Fatal error when trying to update "
                        + dataType + " from database reference - " + ref.toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(TAG,"Unable to get " + dataType + "value from database");
            }
        });
    }

    //endregion

    //region Utility

    public static HashMap<String, String> buildChatMap(String _hostUserID, String _chatDate, String _chatTime
            , String _chatLength){

        HashMap<String,String> chatMap = new HashMap<>();

        chatMap.put("host",_hostUserID);
        chatMap.put("date",_chatDate);
        chatMap.put("time",_chatTime);
        chatMap.put("length",_chatLength);

        return chatMap;
    }

    public boolean checkForUserInChat(String userID){

        if(guestUser == null){
            return hostUser.userID.equals(userID);
        }

        else return guestUser.userID.equals(userID) || hostUser.userID.equals(userID);
    }

    private void checkReady(){

        boolean ready = (hostUser != null &&
                guestReady &&
                chatID != null &&
                chatDate != null &&
                chatTime != null &&
                chatLength != null &&
                chatAccepted != null &&
                chatActive != null &&
                chatCanceled != null);

        if(readyListener != null && ready){
            readyListener.onReady(this);
        }
    }

    private String generateChatID()
    {
        return UUID.randomUUID().toString();
    }

    private boolean translateDatabase(String dataType,DataSnapshot snapshot)
    {
        switch(dataType) {
            //region Host
            case ("host"): {

                String chatHostID =  (String)snapshot.child("host").getValue();
                if(chatHostID == null) return false;

                if (chatHostID.equals(localUserID)) {
                    hostUser = LocalUser.getInstance().user;
                } else {
                    Log.d(TAG, String.valueOf( snapshot.getValue()));
                    hostUser = new YarnUser( chatHostID,
                            YarnUser.UserType.NETWORK);
                }
                return true;
            }
            //endregion
            //region Guest
            case ("guest"): {

                guestReady = true;
                String chatGuestID =  (String)snapshot.child("guest").getValue();
                if(chatGuestID == null) return false;

                if (chatGuestID.equals(localUserID)) {
                    guestUser = LocalUser.getInstance().user;
                } else if (!chatGuestID.equals("")) {
                    guestUser = new YarnUser( chatGuestID,
                            YarnUser.UserType.NETWORK);
                } else {
                    guestUser = null;
                }
                return true;
            }
            //endregion
            //region Date
            case ("date"): {
                String date =  (String)snapshot.child("date").getValue();
                if(date != null){
                    chatDate = date;
                    return true;
                }else return false;
            }
            //endregion
            //region Time
            case ("time"): {
                String time =  (String)snapshot.child("time").getValue();
                if(time != null) {
                    chatTime = time;
                    return true;
                }else return false;
            }
            //endregion
            //region Length
            case ("length"): {
                String length =  (String)snapshot.child("length").getValue();
                if(length != null){
                    chatLength = length;
                    return true;
                }else return false;
            }
            //endregion
            //region Accepted
            case ("accepted"): {
                Boolean accepted =  (Boolean) snapshot.child("accepted").getValue();
                if (accepted != null) {
                    chatAccepted = accepted;
                    if(valueChangelistener != null) valueChangelistener.onAcceptedChange();
                    return true;
                } else{
                    return false;
                }
            }
            //endregion
            //region Active
            case ("active"): {
                Boolean active =  (Boolean)snapshot.child("active").getValue();
                if (active != null) {
                    chatActive = active;
                    if(valueChangelistener != null) valueChangelistener.onActiveChange();
                    return true;
                } else return false;
            }
            //endregion
            //region Canceled
            case ("canceled"): {
                Boolean canceled =  (Boolean)snapshot.child("canceled").getValue();
                if (canceled != null) {
                    chatCanceled = canceled;
                    if(valueChangelistener != null) valueChangelistener.onCanceledChange();
                    return true;
                } else return false;
            }
            //endregion
        }

        return false;
    }

    //endregion

}
