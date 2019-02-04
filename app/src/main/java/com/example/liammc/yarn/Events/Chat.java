package com.example.liammc.yarn.Events;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.accounting.YarnUser;

import com.example.liammc.yarn.core.ChatRecorder;
import com.example.liammc.yarn.utility.AddressTools;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class Chat implements Parcelable
{
    private final DatabaseReference userDatabaseReference;
    private final String CALLINGTAG;
    private final String localUserID;

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

    public String chatPlaceType;
    public String chatDate;
    public String chatTime;
    public String chatLength;

    private boolean accepted = false;
    private boolean active = false;
    private boolean canceled = false;

    //region Constructors

    public Chat(String callingTag,String _localUserID, String _hostUserID, String _chatPlaceID, Address address,
                String _placeType, String _chatDate, String _chatTime, String _chatLength)
    {
        //This is the constructor for creating an instance of a new chat
        this.localUserID = _localUserID;
        this.CALLINGTAG = callingTag;

        //Intialize chat variables
        this.hostUser = new YarnUser("Chat",_hostUserID,YarnUser.UserType.LOCAL);
        this.chatPlaceID = _chatPlaceID;

        this.UpdateAddress(address);

        this.chatID = this.generateChatID();
        this.chatPlaceType = _placeType;
        this.chatDate = _chatDate;
        this.chatTime = _chatTime;
        this.chatLength = _chatLength;

        this.initializeChatState();

        this.userDatabaseReference = AddressTools.getChatDatabaseReference(chatPlaceID);
        this.updateDatabase();
    }

    public Chat(String callingTag,String localUserID,String chatPlaceID, String _chatID, Address address)
    {
        //This is the constructor for creating an instance of an exsisting chat
        this.CALLINGTAG = callingTag;
        this.localUserID = localUserID;

        this.UpdateAddress(address);

        this.userDatabaseReference = AddressTools.getChatDatabaseReference(chatPlaceID);
        this.chatID = _chatID;
        //this.addDataListner("chatID");
        this.addDataListener("host");
        this.addDataListener("guest");
        this.addDataListener("placeID");
        this.addDataListener("formatted_address");
        this.addDataListener("country");
        this.addDataListener("admin1");
        this.addDataListener("admin2");
        this.addDataListener("locality");
        this.addDataListener("street");
        this.addDataListener("postcode");
        this.addDataListener("place_type");
        this.addDataListener("time");
        this.addDataListener("date");
        this.addDataListener("length");
        this.addDataListener("accepted");
        this.addDataListener("active");
        this.addDataListener("canceled");
    }

    //endregion Constructors

    //region Parcelable methods

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {

        out.writeString(CALLINGTAG);
        out.writeString(localUserID);

        out.writeString(hostUser.userID);
        if(guestUser != null)out.writeString(guestUser.userID);
        else out.writeValue("");

        out.writeString(chatID);
        out.writeString(chatPlaceID);
        out.writeParcelable(chatAdressObject,flags);
        out.writeString(chatCountry);
        out.writeString(chatAdmin1);
        out.writeString(chatAdmin2);
        out.writeString(chatLocality);
        out.writeString(chatStreet);
        out.writeString(chatPostcode);

        out.writeString(chatPlaceType);
        out.writeString(chatDate);
        out.writeString(chatTime);
        out.writeString(chatLength);

        out.writeByte((byte) (accepted ? 1 : 0));
        out.writeByte((byte) (active ? 1 : 0));
        out.writeByte((byte) (canceled ? 1 : 0));

    }

    public static final Parcelable.Creator<Chat> CREATOR
            = new Parcelable.Creator<Chat>() {

        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    private Chat(Parcel in) {

        this.CALLINGTAG = in.readString();
        this.localUserID = in.readString();

        //Read host from parcel
        String hostUserID = in.readString();
        if(hostUserID.equals(localUserID)){
            this.hostUser = new YarnUser("Chat",hostUserID,YarnUser.UserType.LOCAL);
        }
        else{
            this.hostUser = new YarnUser("Chat",hostUserID,YarnUser.UserType.NETWORK);
        }

        //read guest from parcel
        String guestUserId = in.readString();
        if(guestUserId.equals(localUserID)){
            this.guestUser = new YarnUser("Chat",guestUserId,YarnUser.UserType.LOCAL);
        }
        else if(!guestUserId.equals("")){
            this.guestUser = new YarnUser("Chat",guestUserId,YarnUser.UserType.NETWORK);
        }
        else this.guestUser = null;

        this.chatID = in.readString();
        this.chatPlaceID = in.readString();
        this.chatAdressObject = in.readParcelable(Address.class.getClassLoader());
        this.chatCountry = in.readString();
        this.chatAdmin1 = in.readString();
        this.chatAdmin2 = in.readString();
        this.chatLocality = in.readString();
        this.chatStreet = in.readString();
        this.chatPostcode = in.readString();

        this.chatPlaceType = in.readString();
        this.chatDate = in.readString();
        this.chatTime = in.readString();
        this.chatLength = in.readString();

        this.accepted = in.readByte() != 0;
        this.active = in.readByte() != 0;
        this.canceled = in.readByte() != 0;

        this.userDatabaseReference = AddressTools.getChatDatabaseReference(this.chatPlaceID);

        this.addDataListener("host");
        this.addDataListener("guest");
        this.addDataListener("placeID");
        this.addDataListener("formatted_address");
        this.addDataListener("country");
        this.addDataListener("admin1");
        this.addDataListener("admin2");
        this.addDataListener("locality");
        this.addDataListener("street");
        this.addDataListener("postcode");
        this.addDataListener("place_type");
        this.addDataListener("time");
        this.addDataListener("date");
        this.addDataListener("length");
        this.addDataListener("accepted");
        this.addDataListener("active");
        this.addDataListener("canceled");
    }

    //endregion

    //region Initialization

    private void initializeChatState()
    {
        //Initialize chat state
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

    public void acceptChat(ChatRecorder chatRecorder,YarnUser guestUser)
    {
        accepted = true;
        updateData("guest",guestUser.userID);
        updateData("accepted",true);
        updateData("active",active);

        chatRecorder.recordChat(this);
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
                        addDataListener(dataType);
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

    private void addDataListener(final String dataType)
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
                    if(snapshot.getValue() == localUserID)
                    {
                        hostUser = new YarnUser("Chat", localUserID,
                            YarnUser.UserType.LOCAL);
                    }
                    else{
                        hostUser = new YarnUser("Chat", (String) snapshot.getValue(),
                                YarnUser.UserType.NETWORK);
                    }
                }
            case("guest"):
                {
                    if(snapshot.getValue() == localUserID)
                    {
                        guestUser = new YarnUser("Chat", localUserID,
                            YarnUser.UserType.LOCAL);
                    }
                    else if(!snapshot.getValue().equals("")){
                        guestUser = new YarnUser("Chat", (String) snapshot.getValue(),
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
            case("place_type"): chatPlaceType = (String) snapshot.getValue();

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
