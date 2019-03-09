package com.example.liammc.yarn.Events;

import android.content.Context;
import android.location.Address;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;

import com.example.liammc.yarn.core.ChatRecorder;
import com.example.liammc.yarn.utility.AddressTools;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class Chat //implements Parcelable
{
    //region Listener
    private ValueChangeListener listener;

    public interface ValueChangeListener {
        void onAcceptedChange();
        void onActiveChange();
        void onCanceledChange();
    }

    public ValueChangeListener getListener() {
        return listener;
    }

    public void setListener(ValueChangeListener listener) {
        this.listener = listener;
    }
    //endregion

    private final String PLACE_INFO_REF = "Yarn_Place_Info";
    private final DatabaseReference chatRef;
    private final DatabaseReference placeInfoRef;
    private final DatabaseReference placeRef;
    private  String TAG = "Chat" ;
    private final String localUserID;

    //public String chatID;
    public YarnUser hostUser;
    public YarnUser guestUser;

    public String chatID;
    public String chatPlaceID;
    public String chatPlaceName;
    public Address chatAdressObject;
    public String chatFormattedAddress;
    public LatLng chatLatLng;
    public String chatCountry;
    public String chatAdmin1;
    public String chatAdmin2;
    public String chatLocality;
    public String chatStreet;
    public String chatPostcode;

    public String chatPlaceType;
    public String chatDate;
    public String chatTime;
    public String chatLength;

    public boolean accepted = false;
    public boolean active = false;
    public boolean canceled = false;

    //region Constructors

    public Chat(String _hostUserID, String _chatPlaceID,
                String _chatPlaceName, Address address, LatLng _latLng, String _placeType, String _chatDate,
                String _chatTime, String _chatLength)
    {
        //This is the constructor for creating an instance of a new chat
        this.localUserID = LocalUser.getInstance().user.userID;

        //Initialize chat variables
        this.hostUser = new YarnUser("Chat",_hostUserID,YarnUser.UserType.LOCAL);
        this.chatPlaceID = _chatPlaceID;
        this.chatPlaceName = _chatPlaceName;

        this.initializeAddress(address);
        this.chatLatLng = _latLng;

        this.chatID = this.generateChatID();
        this.TAG = TAG + " " + chatID;
        this.chatPlaceType = _placeType;
        this.chatDate = _chatDate;
        this.chatTime = _chatTime;
        this.chatLength = _chatLength;

        this.initializeChatState();

        //Initialize database references
        this.chatRef = AddressTools.getChatDatabaseReference(
                this.chatCountry,this.chatAdmin1,
                chatPlaceID,this.chatID);

        this.placeInfoRef = AddressTools.getPlaceInfoDatabaseReference(
                this.chatCountry,this.chatAdmin1,
                chatPlaceID,this.PLACE_INFO_REF);

        this.placeRef = AddressTools.getPlaceDatabaseReference(
                this.chatCountry,this.chatAdmin1,
                chatPlaceID);

        //Initialize database
        this.initializeYarnPlaceDatabase();
        this.initializeChatDatabase();
    }

    public Chat(String chatPlaceID, String _chatID, Address address)
    {
        //This is the constructor for creating an instance of an exsisting chat
        this.TAG = TAG + " " + _chatID;
        this.localUserID = LocalUser.getInstance().user.userID;
        this.chatID = _chatID;

        this.initializeAddress(address);

        //Initialize database references
        this.chatRef = AddressTools.getChatDatabaseReference(
                this.chatCountry,this.chatAdmin1,
                chatPlaceID,this.chatID);

        this.placeInfoRef = AddressTools.getPlaceInfoDatabaseReference(
            this.chatCountry,this.chatAdmin1,
            chatPlaceID, PLACE_INFO_REF);

        this.placeRef = AddressTools.getPlaceDatabaseReference(
                this.chatCountry,this.chatAdmin1,
                chatPlaceID);

        /*
        //Get Yarn place Info
        this.getData(placeInfoRef,"place_name");
        this.getData(placeInfoRef,"formatted_address");
        this.getData(placeInfoRef,"latLng");
        this.getData(placeInfoRef,"country");
        this.getData(placeInfoRef,"admin1");
        this.getData(placeInfoRef,"admin2");
        this.getData(placeInfoRef,"locality");
        this.getData(placeInfoRef,"street");
        this.getData(placeInfoRef,"postcode");
        this.getData(placeInfoRef,"place_type");
        */

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

    private void initializeAddress(Address address)
    {
        chatAdressObject = address;
        chatFormattedAddress = AddressTools.formatAddress(address);

        String country = address.getCountryName();
        String admin1 = address.getAdminArea();
        String admin2 = address.getSubAdminArea();
        String locality = address.getLocality();
        String street = address.getAddressLine(0);
        String postcode = address.getPostalCode();

        if(country != null)chatCountry = country;
        else chatCountry = "";

        if(admin1 != null)chatAdmin1 = admin1;
        else chatAdmin1 = "";

        if(admin2 != null)chatAdmin2 = admin2;
        else chatAdmin2 = "";

        if(locality != null)chatLocality = locality;
        else chatLocality = "";

        if(street != null)chatStreet = street;
        else chatStreet = "";

        if(postcode != null)chatPostcode = postcode;
        else chatPostcode = "";

    }

    private void initializeChatState()
    {
        //Initialize chat state
        accepted = false;
        active = false;
        canceled = false;
    }

    private void initializeChatDatabase()
    {
       updateData(chatRef,"host",hostUser.userID);
       updateData(chatRef,"guest","");
       updateData(chatRef,"date",chatDate);
       updateData(chatRef,"time",chatTime);
       updateData(chatRef,"length",chatLength);
       updateData(chatRef,"accepted",accepted);
       updateData(chatRef,"active",active);
       updateData(chatRef,"canceled",canceled);
    }

    private void initializeYarnPlaceDatabase()
    {
        //Check if the database has an info node for this place
        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(PLACE_INFO_REF)){
                    setData(placeInfoRef,"place_name",chatPlaceName);
                    setData(placeInfoRef,"formatted_address",chatFormattedAddress);
                    setData(placeInfoRef,"lat",chatLatLng.latitude);
                    setData(placeInfoRef,"lng",chatLatLng.longitude);
                    setData(placeInfoRef,"country",chatCountry);
                    setData(placeInfoRef,"admin1",chatAdmin1);
                    setData(placeInfoRef,"admin2",chatAdmin2);
                    setData(placeInfoRef,"locality",chatLocality);
                    setData(placeInfoRef,"street",chatStreet);
                    setData(placeInfoRef,"postcode",chatPostcode);
                    setData(placeInfoRef,"place_type",chatPlaceType);

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
        accepted = true;
        updateData(chatRef,"guest",guestUser.userID);
        updateData(chatRef,"accepted",true);
        updateData(chatRef,"active",active);

        ChatRecorder.getInstance().recordChat(context,this);
    }

    public void cancelChat()
    {
        active = false;
        canceled = true;

        updateData(chatRef,"active",active);
        updateData(chatRef,"canceled",canceled);
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

    private void updateData(final DatabaseReference ref, final String dataType, final Object dataValue)
    {
        final DatabaseReference dataRef = ref.child(dataType);

        //Write to the User database user name
        dataRef.setValue(dataValue)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Log.d(TAG,dataType +" write to database was a success :"
                                + dataValue.toString());
                        addDataListener(dataRef,dataType);
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

    /*
    private void getData(DatabaseReference ref,final String dataType) {

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (translateDatabase(dataType, snapshot)) {
                    Log.d(CALLINGTAG, "Updated " + dataType + " from database : "
                            + snapshot.getValue().toString());
                } else Log.e(CALLINGTAG, "Fatal error when trying to update "
                        + dataType + " from database");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.e(CALLINGTAG, "Unable to get " + dataType + "value from database");
            }
        });
    }*/

    //endregion

    //region Utility

    private String generateChatID()
    {
        return UUID.randomUUID().toString();
    }

    private boolean translateDatabase(String dataType,DataSnapshot snapshot)
    {
        switch(dataType) {
            //region Place Name
            case ("place_name"): {

                String placeName = snapshot.child("place_name")

                if(snapshot.getValue() != null){
                    chatPlaceName = (String)snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Host
            case ("host"): {
                if(snapshot.getValue() != null){

                    String chatHostID =  (String)snapshot.child("host").getValue();

                    if (chatHostID.equals(localUserID)) {
                        hostUser = LocalUser.getInstance().user;
                    } else {
                        Log.d(TAG, String.valueOf( snapshot.getValue()));
                        hostUser = new YarnUser("Chat", chatHostID,
                                YarnUser.UserType.NETWORK);
                    }
                    return true;
                }else return false;
            }
            //endregion
            //region Guest
            case ("guest"): {
                if(snapshot.getValue() != null){

                    String chatGuestID =  (String)snapshot.child("guest").getValue();

                    if (chatGuestID.equals(localUserID)) {
                        guestUser = LocalUser.getInstance().user;
                    } else if (!chatGuestID.equals("")) {
                        guestUser = new YarnUser("Chat", chatGuestID,
                                YarnUser.UserType.NETWORK);
                    } else {
                        guestUser = null;
                    }
                    return true;
                }else return false;
            }
            //endregion
            //region Formatted Address
            case ("formatted_address"): {
                if(snapshot.getValue() != null){
                    chatFormattedAddress = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Lat Lng
            case("latLng"):{
                if(snapshot.getValue() != null){
                     chatLatLng = new LatLng((double)snapshot.child("latitude").getValue(),
                             (double)snapshot.child("longitude").getValue());
                    return true;
                }else return false;
            }
            //endregion
            //region Country
            case ("country"): {
                if(snapshot.getValue() != null){
                    chatCountry = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Admin 1
            case ("admin1"): {
                if(snapshot.getValue() != null){
                    chatAdmin1 = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Admin 2
            case ("admin2"): {
                if(snapshot.getValue() != null){
                    chatAdmin2 = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion Admin 2
            //region Locality
            case ("locality"): {
                if(snapshot.getValue() != null){
                    chatLocality = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Street
            case ("street"): {
                if(snapshot.getValue() != null){
                    chatStreet = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Postcode
            case ("postcode"): {
                if(snapshot.getValue() != null){
                    chatPostcode = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Place Type
            case ("place_type"): {
                if(snapshot.getValue() != null){
                    chatPlaceType = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Date
            case ("date"): {
                if(snapshot.getValue() != null){
                    chatDate = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Time
            case ("time"): {
                if(snapshot.getValue() != null) {
                    chatTime = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Length
            case ("length"): {
                if(snapshot.getValue() != null){
                    chatLength = (String) snapshot.getValue();
                    return true;
                }else return false;
            }
            //endregion
            //region Accepted
            case ("accepted"): {
                if (snapshot.getValue() != null) {
                    accepted = Boolean.valueOf(snapshot.getValue().toString());
                    listener.onAcceptedChange();
                    return true;
                } else{
                    return false;
                }
            }
            //endregion
            //region Active
            case ("active"): {
                if (snapshot.getValue() != null) {
                    active = Boolean.valueOf(snapshot.getValue().toString());
                    listener.onActiveChange();
                    return true;
                } else return false;
            }
            //endregion
            //region Canceled
            case ("canceled"): {
                if (snapshot.getValue() != null) {
                    canceled = Boolean.valueOf(snapshot.getValue().toString());
                    listener.onCanceledChange();
                    return true;
                } else return false;
            }
            //endregion
        }

        return false;
    }

    //endregion

}
