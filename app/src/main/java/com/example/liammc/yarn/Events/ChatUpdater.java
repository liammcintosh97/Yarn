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
import com.google.firebase.database.ValueEventListener;


public class ChatUpdater
{
    final String TAG = "Chat Updater";
    final String localUserID;
    DatabaseReference chatDatabaseReference;

    private YarnPlace yarnPlace;


    public ChatUpdater(String localUserID, YarnPlace _yarnPlace)
    {
        this.localUserID = localUserID;
        this.yarnPlace = _yarnPlace;

        this.chatDatabaseReference = AddressTools.getPlaceDatabaseReference(
                yarnPlace.address.getCountryName(),yarnPlace.address.getLocality(),
                yarnPlace.placeMap.get("id"));

        this.getChats();
        this.UpdateChatFinderAddress();
    }

    private void getChats()
    {

        if(chatDatabaseReference.getKey() != null) {

            chatDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChildren())
                    {
                        for (DataSnapshot child: dataSnapshot.getChildren()) {

                            Log.d(TAG,child.toString());

                            //The child isn't the info node
                            if(!child.getKey().equals("Yarn_Place_Info"))
                            {
                                try
                                {
                                    if(!isActive(child) && !isHost(child))
                                    {
                                        addNewChat(yarnPlace.placeMap.get("id"),child.getKey());
                                    }
                                }
                                catch(Exception e){
                                    Log.d(TAG,"Not the correct Data");
                                }
                            }
                        }
                    }
                    else{
                        Log.e(TAG,"Data Snapshot \"" + dataSnapshot.getKey() + "\" has no children");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    public void UpdateChatFinderAddress()
    {
        if(chatDatabaseReference.getKey() != null) {

            chatDatabaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                {
                    String key = dataSnapshot.getKey();

                    if(!exsistingChat(key) || !key.equals("Yarn_Place_Info"))
                    {
                        try {
                            if (!isActive(dataSnapshot) && !isHost(dataSnapshot)) {
                                addNewChat(yarnPlace.placeMap.get("id"), dataSnapshot.getKey());
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "Not the correct Data");
                        }
                    }
                    else{
                        Log.d(TAG,"This chat object is already in the system");
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

        Chat newChat  = new Chat("ChatUpdater",localUserID,chatPlaceID,chatID,yarnPlace.address);

        Notifier.getInstance().addChatSuggestion("Chat suggestion","A new chat was " +
                "created at " + newChat.chatPlaceName + " on " + newChat.chatDate +
                " at " + newChat.chatTime, newChat);

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
