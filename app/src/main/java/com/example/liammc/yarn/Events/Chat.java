package com.example.liammc.yarn.Events;

import android.app.Activity;
import android.location.Address;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.accounting.YarnUser;

import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.utility.AddressTools;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;
public class Chat
{
    private final GeoDataClient mGeoDataClient;
    private final DatabaseReference userDatabaseReference;
    private final MapsActivity mapsActivity;
    private final String CALLINGTAG;

    //public String chatID;
    public YarnUser hostUser;
    public YarnUser guestUser;

    public String chatID;
    public String chatPlaceID;
    public Address chatAdressObject;
    public String chatFormattedAddress;
    public String chatCountry;
    public String chatAdmin1;
    public String chatAdmin2;
    public String chatLocality;
    public String chatStreet;
    public String chatPostcode;

    public YarnPlace.PlaceType chatPlaceType;
    public String chatDate;
    public String chatTime;
    public String chatLength;

    private boolean accepted = false;
    private boolean active = false;
    private boolean canceled = false;

    //region Constructors

    public Chat(MapsActivity _mapsActivity, YarnUser _host, String _chatPlaceID, Address address,
                YarnPlace.PlaceType _placeType, String _chatDate, String _chatTime, String _chatLength)
    {
        //This is the constructor for creating an instance of a new chat

        this.mapsActivity = _mapsActivity;
        this.CALLINGTAG = _mapsActivity.getLocalClassName();

        //Intialize chat variables
        //this.chatID = generateChatID();
        this.hostUser = _host;
        this.chatPlaceID = _chatPlaceID;

        this.UpdateAddress(address);

        this.chatID = this.generateChatID();
        this.chatPlaceType = _placeType;
        this.chatDate = _chatDate;
        this.chatTime = _chatTime;
        this.chatLength = _chatLength;

        this.intializeChatState();

        this.mGeoDataClient = Places.getGeoDataClient(mapsActivity);

        this.userDatabaseReference = AddressTools.getChatDatabaseReference(chatPlaceID);
        this.updateDatabase();
    }

    public Chat(MapsActivity _mapsActivity,String chatPlaceID, String _chatID, Address address)
    {
        //This is the constructor for creating an instance of an exsisting chat

        this.mapsActivity = _mapsActivity;
        this.CALLINGTAG = _mapsActivity.getLocalClassName();

        //this.chatID = _chatId;

        this.UpdateAddress(address);

        this.mGeoDataClient = Places.getGeoDataClient(_mapsActivity);

        this.userDatabaseReference = AddressTools.getChatDatabaseReference(chatPlaceID);
        this.chatID = _chatID;
        //this.addDataListner("chatID");
        this.addDataListner("host");
        this.addDataListner("guest");
        this.addDataListner("placeID");
        this.addDataListner("formatted_address");
        this.addDataListner("country");
        this.addDataListner("admin1");
        this.addDataListner("admin2");
        this.addDataListner("locality");
        this.addDataListner("street");
        this.addDataListner("postcode");
        this.addDataListner("place_type");
        this.addDataListner("time");
        this.addDataListner("date");
        this.addDataListner("length");
        this.addDataListner("accepted");
        this.addDataListner("active");
        this.addDataListner("canceled");
    }

    //endregion Constructors

    //region Intialization

    private void intializeChatState()
    {
        //Intialize chat state
        accepted = false;
        active = false;
        canceled = false;
    }

    private void updateDatabase()
    {
       //updateData("chatID",chatID);
       updateData("host",hostUser.userID);
       updateData("guest","");
       //updateData("placeID",chatPlaceID);
       updateData("formatted_address",chatFormattedAddress);
       updateData("country",chatCountry);
       updateData("admin1",chatAdmin1);
       updateData("admin2",chatAdmin2);
       updateData("locality",chatLocality);
       updateData("street",chatStreet);
       updateData("postcode",chatPostcode);
       updateData("place_type",chatPlaceType);
       updateData("date",chatDate);
       updateData("time",chatTime);
       updateData("length",chatLength);
       updateData("accepted",accepted);
       updateData("active",active);
       updateData("canceled",canceled);
    }

    //endregion

    //region Public Local Methods

    public void acceptChat(YarnUser guestUser)
    {
        accepted = true;
        updateData("guest",guestUser.userID);
        updateData("accepted",true);
        updateData("active",active);

        mapsActivity.activeChat = this;
    }

    public void cancelChat()
    {
        active = false;
        canceled = true;

        updateData("active",active);
        updateData("canceled",canceled);
    }

    //endregion

    //region Private Local Methods

    /*
    public void updateChat(String _chatID, String _hostID, String _guestID, String _placeID
            , String _formattted_address, String _country, String _admin1, String _admin2
            , String _locality, String _street, String _postcode, YarnPlace.PlaceType _placeType
            , String _date, String _length, Boolean _accepted, Boolean _active, Boolean _canceled )
    {
        //chatID = _chatID;

        if(_hostID == mapsActivity.localUser.userID)
        {
            hostUser =
        }
        hostUser = _hostID;
        guestUser = _guestID;

        //chatPlaceID = _placeID;
        chatFormattedAddress = _formattted_address;
        chatCountry = _country;
        chatAdmin1 = _admin1;
        chatAdmin2 = _admin2;
        chatLocality = _locality;
        chatStreet = _street;
        chatPostcode = _postcode;

        chatPlaceType = _placeType;
        chatDate = _date;
        chatLength = _length;

        accepted = _accepted;
        active = _active;
        canceled = _canceled;

    }*/

    private void UpdateAddress(Address address)
    {
        chatAdressObject = address;

        chatFormattedAddress = AddressTools.formatAddress(address);
        chatCountry = address.getCountryCode();
        chatAdmin1 = address.getAdminArea();
        chatAdmin2 = address.getSubAdminArea();
        chatLocality = address.getLocality();
        chatStreet = address.getAddressLine(0);
        chatPostcode = address.getPostalCode();
    }

    private void updateData(final String dataType, Object dataValue)
    {
       final DatabaseReference ref = userDatabaseReference.child(chatID).child(dataType);

        //Write to the User database user name
        ref.setValue(dataValue)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Log.d(CALLINGTAG,dataType +" write to database was a success");
                        addDataListner(dataType);
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d(CALLINGTAG,dataType +"write to database was a failure -" + e);
                    }
                });
    }

    private void addDataListner(final String dataType)
    {
        DatabaseReference ref = userDatabaseReference.child(chatID).child(dataType);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                translateDatabase(dataType,snapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(CALLINGTAG,"Unable to get " + dataType + "value from database");
            }
        });
    }

    //endregion

    //region Utility

    private String generateChatID()
    {
        return UUID.randomUUID().toString();
    }

    private void translateDatabase(String dataType,DataSnapshot snapshot)
    {
        switch(dataType)
        {
            //case("chatID"): chatID = (String) snapshot.getValue();
            case("host"):
                {
                    if(snapshot.getValue() == mapsActivity.localUser.userID)
                    {
                        hostUser = mapsActivity.localUser;
                    }
                    else{
                        hostUser = new YarnUser(mapsActivity, (String) snapshot.getValue(),
                                YarnUser.UserType.NETWORK);
                    }
                }
            case("guest"):
                {
                    if(snapshot.getValue() == mapsActivity.localUser.userID)
                    {
                        guestUser = mapsActivity.localUser;
                    }
                    else if(!snapshot.getValue().equals("")){
                        guestUser = new YarnUser(mapsActivity, (String) snapshot.getValue(),
                                YarnUser.UserType.NETWORK);
                    }
                    else{
                        guestUser = null;
                    }
                }
            //case("place"): chatPlaceID = (String) snapshot.getValue();

            case("formatted_address"): chatFormattedAddress = (String) snapshot.getValue();
            case("country"): chatCountry = (String) snapshot.getValue();
            case("admin1"): chatAdmin1 = (String) snapshot.getValue();
            case("admin2"): chatAdmin2 = (String) snapshot.getValue();
            case("locality"): chatLocality = (String) snapshot.getValue();
            case("street"): chatStreet = (String) snapshot.getValue();
            case("postcode"): chatPostcode = (String) snapshot.getValue();

            case("date"): chatDate =  (String) snapshot.getValue();
            case("time"): chatTime = (String) snapshot.getValue();
            case("length"): chatLength = (String) snapshot.getValue();
            case("accepted"): accepted = Boolean.valueOf(snapshot.getValue().toString());
            case("active"): active = Boolean.valueOf(snapshot.getValue().toString());
            case("canceled"): canceled = Boolean.valueOf(snapshot.getValue().toString());
        }
    }

    //endregion

}
