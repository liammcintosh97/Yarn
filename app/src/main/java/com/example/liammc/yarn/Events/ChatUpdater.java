package com.example.liammc.yarn.Events;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.ReadyListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ChatUpdater
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

    final String TAG = "Chat Updater";
    final String localUserID;
    DatabaseReference placeDatabaseReference;

    private YarnPlace yarnPlace;
    ChildEventListener childEventListener;
    Recorder recorder;


    public ChatUpdater(String localUserID, YarnPlace _yarnPlace, ReadyListener listener)
    {
        this.readyListener = listener;
        this.localUserID = localUserID;
        this.yarnPlace = _yarnPlace;

        this.placeDatabaseReference = AddressTools.getPlaceDatabaseReference(
                yarnPlace.address.getCountryName(),yarnPlace.address.getAdminArea(),
                yarnPlace.placeMap.get("id"));

        this.recorder = Recorder.getInstance();
    }

    public void getJoinedChats(final Context context){

        if(placeDatabaseReference.getKey() != null) {

            placeDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long childrenCount = dataSnapshot.getChildrenCount() - 1;
                    addChats(context,dataSnapshot.getChildren(),childrenCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    public void initializeChatListener(final Context context){

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                String key = dataSnapshot.getKey();

                if(key.equals(Chat.PLACE_INFO_REF)) {
                    Log.d(TAG,"Not a Chat");
                    return;
                }

                if(existingChat(key)){
                    Log.d(TAG,"This chat object is already in the system");
                    return;
                }

                if(isAccepted(dataSnapshot))
                {
                    Log.d(TAG,"This chat has already been chatAccepted");
                    return;
                }

                addChat(context,dataSnapshot.getKey());
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
        };
    }

    public void addChatListener() {
        if(placeDatabaseReference.getKey() != null) {

            placeDatabaseReference.addChildEventListener(childEventListener);
        }
    }

    public void removeChatListener(){

        placeDatabaseReference.removeEventListener(childEventListener);
    }

    private void addChat(final Context context,String chatID) {

        if(existingChat(chatID)) return;

        final Chat addedChat =  new Chat(yarnPlace, chatID);
        yarnPlace.chats.add(addedChat);

        addedChat.setReadyListener(new Chat.ChatReadyListener() {
            @Override
            public void onReady(Chat chat) {

                yarnPlace.chats.add(chat);
                if(chat.checkForUserInChat(localUserID)){
                    recorder.recordChat(context,chat);
                }
                else{
                    Notifier.getInstance().addChatSuggestion("Chat suggestion","A new chat was " +
                            "created at " + chat.yarnPlace.placeMap.get("name") + " on " + chat.chatDate +
                            " at " + chat.chatTime, chat);
                }
            }
        });
    }

    private void addChats(final Context context,final Iterable<DataSnapshot> children
            , final long length) {

        final ArrayList<Chat> addedChats =  new ArrayList<>();

        for (DataSnapshot child: children) {

            String chatID = child.getKey();

            if(chatID.equals(Chat.PLACE_INFO_REF)) {
                Log.d(TAG,"Not a Chat");
                continue;
            }

            if(existingChat(chatID)) return;

            final Chat addedChat =  new Chat(yarnPlace, chatID);

            addedChat.setReadyListener(new Chat.ChatReadyListener() {
                @Override
                public void onReady(Chat chat) {

                    Log.d(TAG,"Chat -" + chat.chatID + " is ready" );
                    yarnPlace.chats.add(chat);
                    addedChats.add(chat);

                    if(chat.checkForUserInChat(localUserID)){
                        recorder.recordChat(context,chat);
                    }else{
                        Notifier.getInstance().addChatSuggestion("Chat suggestion",
                                "A new chat was created at "
                                        + chat.yarnPlace.placeMap.get("name") + " on "
                                        + chat.chatDate + " at " + chat.chatTime, chat);
                    }

                    if(checkReady(addedChats,length)) {
                        readyListener.onReady();
                        initializeChatListener(context);
                        addChatListener();
                        Log.d(TAG,"The Chat Updater is ready");
                    }
                }
            });
        }
    }

    private boolean checkReady(ArrayList<Chat> chatList,long amount){

        Log.d(TAG,chatList.size() + " " + amount);

        return chatList.size() == amount;
    }

    private void removeChat(DataSnapshot dataSnapshot) {
        String removedChatPlaceID = (String)dataSnapshot.getValue();

        for(int i = 0; i < yarnPlace.chats.size(); i ++)
        {
            Chat chat = yarnPlace.chats.get(i);
            String chatID = chat.yarnPlace.placeMap.get("id");

            if(chatID == null) continue;

            if(chatID.equals(removedChatPlaceID)){
                yarnPlace.chats.remove(i);
            }
        }

        yarnPlace.removeChatFromScrollView(removedChatPlaceID);
    }

    private boolean isAccepted(DataSnapshot chatSnapshot) {
        for (DataSnapshot child: chatSnapshot.getChildren()) {
            Log.d(TAG,String.valueOf(child.getKey()) + " : " + String.valueOf(child.getValue()));
        }

        return (boolean) chatSnapshot.child("accepted")
                .getValue();
    }

    private boolean isHost(DataSnapshot snapshot) {
        String chatHostID = snapshot.child("host").getValue().toString();

        if(chatHostID.equals(""))
        {
            return false;
        }
        else return localUserID.equals(chatHostID);
    }

    private boolean existingChat(String chatID) {
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
