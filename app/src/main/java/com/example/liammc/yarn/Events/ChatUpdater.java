package com.example.liammc.yarn.Events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.liammc.yarn.utility.AddressTools;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;


public class ChatUpdater
{
    final String TAG = "Chat Updater";
    final String localUserID;
    DatabaseReference placeDatabaseReference;

    private YarnPlace yarnPlace;


    public ChatUpdater(String localUserID, YarnPlace _yarnPlace)
    {
        this.localUserID = localUserID;
        this.yarnPlace = _yarnPlace;


        this.placeDatabaseReference = AddressTools.getPlaceDatabaseReference(
                yarnPlace.address.getCountryName(),yarnPlace.address.getAdminArea(),
                yarnPlace.placeMap.get("id"));

        //this.getChats();
        this.setChatListener();
    }

    /*
    private void getChats()
    {
        if(placeDatabaseReference.getKey() != null) {

            placeDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChildren())
                    {
                        for (DataSnapshot child: dataSnapshot.getChildren()) {

                            String childKey =  child.getKey();

                            //The child isn't the info node
                            if(!childKey.equals("Yarn_Place_Info"))
                            {
                                if(isActive(child))
                                {
                                    addChat(yarnPlace.placeMap.get("id"),child.getKey());
                                }
                                else{
                                    Log.d(TAG,"Not the correct Data");
                                }
                            }
                            else{
                                Log.d(TAG, "Not the correct Data");
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
    }*/

    public void setChatListener()
    {
        if(placeDatabaseReference.getKey() != null) {

            placeDatabaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                {
                    String key = dataSnapshot.getKey();

                    if(key.equals("Yarn_Place_Info")) {
                        Log.d(TAG,"Not a Chat");
                        return;
                    }

                    if(existingChat(key)){
                        Log.d(TAG,"This chat object is already in the system");
                        return;
                    }

                    if(isAccepted(dataSnapshot))
                    {
                        Log.d(TAG,"This chat has already been accepted");
                        return;
                    }

                    addChat(yarnPlace.placeMap.get("id"), dataSnapshot.getKey());
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


    private void addChat(String chatPlaceID, String chatID)
    {
        for(int i = 0 ; i < yarnPlace.chats.size(); i++)
        {
            if(chatID.equals(yarnPlace.chats.get(i).chatID)) return;
        }

        Chat Chat  = new Chat(chatPlaceID,chatID,yarnPlace.address);

        Notifier.getInstance().addChatSuggestion("Chat suggestion","A new chat was " +
                "created at " + Chat.chatPlaceName + " on " + Chat.chatDate +
                " at " + Chat.chatTime, Chat);

        yarnPlace.chats.add(Chat);
        yarnPlace.addChatToScrollView(Chat);
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

    private boolean isAccepted(DataSnapshot chatSnapshot)
    {
        return Boolean.valueOf(chatSnapshot.child("accepted").getValue().toString());
    }

    private boolean isHost(DataSnapshot snapshot)
    {
        String chatHostID = snapshot.child("host").getValue().toString();

        if(chatHostID.equals(""))
        {
            return false;
        }
        else return localUserID.equals(chatHostID);
    }

    private boolean existingChat(String chatID)
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
