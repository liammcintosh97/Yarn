package com.example.liammc.yarn.yarnPlace;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.interfaces.ReadyListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class YarnPlaceUpdater
{
    /*This Class is used for updating the values of each chat for the Yarn place
    that it is attached to
     */

    //region Ready Listener

    /*The Ready listener is used for determining when the initialized Yarn place has
    all of it chat data
     */

    private ReadyListener readyListener;

    public ReadyListener getReadyListener() {
        return readyListener;
    }

    public void setReadyListener(ReadyListener readyListener) {
        this.readyListener = readyListener;
    }
    //endregion

    final String TAG = "YarnPlaceUpdater";
    final String localUserID;

    private YarnPlace yarnPlace;
    public ChildEventListener childEventListener;
    Recorder recorder;


    public YarnPlaceUpdater(String localUserID, YarnPlace _yarnPlace, ReadyListener listener)
    {
        this.readyListener = listener;
        this.localUserID = localUserID;
        this.yarnPlace = _yarnPlace;

        this.recorder = Recorder.getInstance();
    }

    //region Init

    public void initChildListener(final Activity activity){

        /*Listens for new chats as they are added to the database under the ID of the Yarn
        Place*/

        childEventListener =  new ChildEventListener() {
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

                addChats(activity,dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {
                //removeChat(dataSnapshot);
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

    //endregion

    //region Public Methods

    public void addChats(final Activity activity, String chatID) {

        final Chat addedChat =  new Chat(yarnPlace, chatID);

        addedChat.setReadyListener(new Chat.ChatReadyListener() {
            @Override
            public void onReady(Chat chat) {
                addToSystem(activity,chat);
            }
        });
    }

    public void addChats(final Activity activity, final Iterable<DataSnapshot> children
            , final long length) {

        final ArrayList<Chat> addedChats =  new ArrayList<>();

        for (DataSnapshot child: children) {

            String chatID = child.getKey();

            //The snapshot child is the place info so we can't make a chat out of it
            if(chatID.equals(Chat.PLACE_INFO_REF)) {
                Log.d(TAG,"Not a Chat");
                continue;
            }

            //The chat already exists in the yarnplace so don't add it
            if(existingChat(chatID)){
                Log.d(TAG,"This chat exists so don't add it");
                continue;
            }

            final Chat addedChat =  new Chat(yarnPlace, chatID);

            addedChat.setReadyListener(new Chat.ChatReadyListener() {
                @Override
                public void onReady(Chat chat) {

                    Log.d(TAG,"Chat -" + chat.chatID + " is ready" );
                    addedChats.add(chat);

                    addToSystem(activity,chat);

                    if(checkReady(addedChats,length)) {
                        readyListener.onReady();
                        initChildListener(activity);
                        addChildListener();
                        Log.d(TAG,"The Chat Updater is ready");
                    }
                }
            });
        }
    }

    public void getChats(final Activity activity){

        if(yarnPlace.placeRef.getKey() != null) {

            yarnPlace.placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long childrenCount = dataSnapshot.getChildrenCount() - 1;

                    //There are chats to add
                    if(childrenCount > 0){
                        addChats(activity,dataSnapshot.getChildren(),childrenCount);
                    }
                    //There are no chats to add
                    else{
                        getReadyListener().onReady();
                        initChildListener(activity);
                        addChildListener();
                        Log.d(TAG,"The Chat Updater is ready");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void addToSystem(Activity activity, Chat chat) {
        yarnPlace.addChat(chat);
        chat.updator.initChangeListener(activity);
        initChildListener(activity);
        addChildListener();

        if(yarnPlace.infoWindow != null && yarnPlace.infoWindow.window.isShowing())
            yarnPlace.infoWindow.updateScrollView();

        if(chat.checkForUserInChat(localUserID)){
            recorder.recordChat(chat);
        }else{
            Notifier.getInstance().addChatSuggestion("Chat suggestion",
                    "A new chat was created at "
                            + chat.yarnPlace.placeMap.get("name") + " on "
                            + chat.chatDate + " at " + chat.chatTime, chat);
        }
    }

    public void addChildListener() {
        if(yarnPlace.placeRef.getKey() != null) {

            yarnPlace.placeRef.addChildEventListener(childEventListener);
        }
    }

    public void removeChildListener(){

        yarnPlace.placeRef.removeEventListener(childEventListener);
    }

    public boolean existingChat(String chatID) {

        if(yarnPlace.getChats() != null) {
            for(int i = 0 ; i < yarnPlace.getChats().size(); i++)
            {
                if(chatID.equals(yarnPlace.getChats().get(i).chatID)) return true;
            }
        }

        if(recorder.chatList != null) {
            for(int i = 0 ; i < recorder.chatList.size(); i++)
            {
                if(chatID.equals(recorder.chatList.get(i).chatID)) return true;
            }
        }

        return false;
    }
    //endregion

    //region Private methods

    private boolean checkReady(ArrayList<Chat> chatList,long amount){

        Log.d(TAG,chatList.size() + " " + amount);

        return chatList.size() == amount;
    }

    public void removeChat(String removedChatPlaceID) {
        //String removedChatPlaceID = (String)dataSnapshot.getValue();

        for(int i = 0; i < yarnPlace.getChats().size(); i ++)
        {
            Chat chat = yarnPlace.getChats().get(i);
            String chatID = chat.yarnPlace.placeMap.get("id");

            if(chatID == null) continue;

            if(chatID.equals(removedChatPlaceID)){
                yarnPlace.getChats().remove(i);
            }
        }
    }

    //endregion

}
