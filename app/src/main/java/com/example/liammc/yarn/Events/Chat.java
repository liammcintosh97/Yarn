package com.example.liammc.yarn.Events;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.accounting.YarnUser;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.StringTokenizer;
import java.util.UUID;
public class Chat
{
    private final GeoDataClient mGeoDataClient;
    private final DatabaseReference userDatabaseReference;
    private final Activity callingActivity;
    private final String CALLINGTAG;

    public String chatID;
    public YarnUser hostUser;
    public YarnUser guestUser;

    public String chatPlaceID;
    public String chatAddress;
    public String chatDate;
    public String chatLength;

    private boolean accepted = false;
    private boolean active = false;
    private boolean canceled = false;

    //region Constructors

    public Chat(Activity _callingActivity,YarnUser _host, String _chatPlaceID, String _chatAddress,
                String _chatDate,String _chatLength)
    {
        //This is the constructor for creating an instance of a new chat

        this.callingActivity = _callingActivity;
        this.CALLINGTAG = _callingActivity.getLocalClassName();

        //Intialize chat variables
        this.chatID = generateChatID();
        this.hostUser = _host;
        this.chatPlaceID = _chatPlaceID;
        this.chatAddress = _chatAddress;
        this.chatDate = _chatDate;
        this.chatLength = _chatLength;

        this.intializeChatState();

        this.mGeoDataClient = Places.getGeoDataClient(_callingActivity);

        this.userDatabaseReference = this.getDatabaseReference(_chatAddress);
        this.intializeDatabase();
    }

    public Chat(Activity _callingActivity,String _chatId)
    {
        //This is the constructor for creating an instance of an exsisting chat

        this.callingActivity = _callingActivity;
        this.CALLINGTAG = _callingActivity.getLocalClassName();

        this.chatID = _chatId;

        this.mGeoDataClient = Places.getGeoDataClient(_callingActivity);

        this.userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chats");

        this.addDataListner("chatID");
        this.addDataListner("host");
        this.addDataListner("guest");
        this.addDataListner("place");
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

    private void intializeDatabase()
    {
       updateData("chatID",chatID);
       updateData("host",hostUser.userID);
       updateData("guest","");
       updateData("place",chatPlaceID);
       updateData("date",chatDate);
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
    }

    public void startChat()
    {
        active = true;
        updateData("active",active);
    }

    public void cancelChat()
    {
        active = false;
        canceled = true;

        updateData("active",active);
        updateData("canceled",canceled);
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


    //region Utility

    private DatabaseReference getDatabaseReference(String address)
    {
        String country;
        String admin1;
        String admin2;
        String locality;

        StringTokenizer tokenizer = new StringTokenizer(address);

        country = tokenizer.nextToken();
        admin1 = tokenizer.nextToken();
        admin2 = tokenizer.nextToken();
        locality = tokenizer.nextToken();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Chats");

        return ref.child(country).child(admin1).child(admin2).child(locality);
    }

    private String generateChatID()
    {
        return UUID.randomUUID().toString();
    }

    private void translateDatabase(String dataType,DataSnapshot snapshot)
    {
        switch(dataType)
        {
            case("chatID"): chatID = (String) snapshot.getValue();
            case("host"): hostUser = new YarnUser(callingActivity, (String) snapshot.getValue());
            case("guest"): guestUser = new YarnUser(callingActivity,(String) snapshot.getValue());
            case("place"): chatPlaceID = (String) snapshot.getValue();
            case("date"): chatDate =  (String) snapshot.getValue();
            case("length"): chatLength = (String) snapshot.getValue();
            case("accepted"): accepted = (boolean) snapshot.getValue();
            case("active"): active = (boolean) snapshot.getValue();
            case("canceled"): canceled = (boolean) snapshot.getValue();
        }
    }

    //endregion

}
