package com.example.liammc.yarn.Events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.utility.AddressTools;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;


public class ChatFinder
{
    final String CALLINGTAG;
    final String localUserID;
    DatabaseReference chatDatabaseReference;

    private YarnPlace yarnPlace;


    public ChatFinder(String callingTag, String localUserID,YarnPlace _yarnPlace)
    {
        this.CALLINGTAG = callingTag;
        this.localUserID = localUserID;

        this.yarnPlace = _yarnPlace;

        this.UpdateChatFinderAddress();
    }

    public void UpdateChatFinderAddress()
    {
        chatDatabaseReference = AddressTools.getChatDatabaseReference(yarnPlace.placeMap.get("id"));

        if(chatDatabaseReference.getKey() != null)
        {
            chatDatabaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                {
                    if(!exsistingChat(dataSnapshot.getKey()))
                    {
                        try
                        {
                            if(!isActive(dataSnapshot) && !isHost(dataSnapshot))
                            {
                                addNewChat(yarnPlace.placeMap.get("id"),dataSnapshot.getValue().toString());
                            }
                        }
                        catch(Exception e){
                            Log.d(CALLINGTAG,"Not the correct Data");
                        }

                    }
                    else{
                        Log.d(CALLINGTAG,"This chat object is already in the system");
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                {
                    removeChat(dataSnapshot);
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
    }


    private void addNewChat(String chatPlaceID, String chatID)
    {
        for(int i = 0 ; i < yarnPlace.chats.size(); i++)
        {
            if(chatID.equals(yarnPlace.chats.get(i).chatID)) return;
        }

        Chat newChat  = new Chat("ChatFinder",localUserID,chatPlaceID,chatID,yarnPlace.address);

        yarnPlace.chats.add(newChat);
        yarnPlace.addChatToScrollView(newChat);
    }

    private void removeChat(DataSnapshot dataSnapshot)
    {
        String removedChatPlaceID = (String)dataSnapshot.getValue();

        for(int i = 0; i < yarnPlace.chats.size(); i ++)
        {
            if(yarnPlace.chats.get(i).chatPlaceID.equals(removedChatPlaceID)){
                yarnPlace.chats.remove(i);
            }
        }

        yarnPlace.removeChatFromScrollView(removedChatPlaceID);
    }

    private boolean isActive(DataSnapshot snapshot) throws Exception
    {
        if(snapshot.getKey().equals("accepted"))
        {
            return Boolean.valueOf(snapshot.getValue().toString());
        }
        else{
            throw new Exception();
        }

    }

    private boolean isHost(DataSnapshot snapshot) throws Exception
    {
        String chatHostID = snapshot.child("host").getValue().toString();

        if(snapshot.getKey().equals("host"))
        {
            return localUserID.equals(chatHostID);
        }
        else{
            throw new Exception();
        }
    }

    private boolean exsistingChat(String chatID)
    {
        if(yarnPlace.chats != null)
        {
            for(int i = 0 ; i < yarnPlace.chats.size(); i++)
            {
                if(chatID.equals(yarnPlace.chats.get(i).chatID)) return true;
            }
        }

        return false;
    }
}
