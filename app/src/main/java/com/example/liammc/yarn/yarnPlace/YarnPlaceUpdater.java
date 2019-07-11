package com.example.liammc.yarn.yarnPlace;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

                //Check if the new child is the Place Info Ref object if so return
                if(key.equals(Chat.PLACE_INFO_REF)) {
                    Log.d(TAG,"Not a Chat");
                    return;
                }

                //Check if the chat is exsisting in the system
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
        /*Adds a single chat to the Yarn Place*/

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
        /*Adds multiples chats to the YarnPlace*/

        //creates a list of all the chats that we need to add
        final ArrayList<Chat> readyChats =  new ArrayList<>();

        //Loop over all chat objects in the database
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

            /*Add the chat to the system once it's ready*/
            addedChat.setReadyListener(new Chat.ChatReadyListener() {
                @Override
                public void onReady(Chat chat) {

                    Log.d(TAG,"Chat -" + chat.chatID + " is ready" );
                    /*Record the newly ready chat into the ready chats list, so that the application
                    can record how many of the newly added chats are ready
                     */
                    readyChats.add(chat);

                    addToSystem(activity,chat);

                    /*Check if the Yarn Place Updator is ready. Its considered ready when  size
                    of the ready chat list is equal the the length of the chat objects in the database
                     */
                    if(checkReady(readyChats,length)) {
                        readyListener.onReady();
                        initChildListener(activity);
                        addChildListener();
                        Log.d(TAG,"The Chat Updater is ready");
                    }
                }
            });
        }
    }

    public void getChatsFromDatabase(final Activity activity){
        /*This method gets all the chats form the database thats under this Yarn Place ID*/

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
        /*This methods add a chat to the entire system and updates various listeners and
        * UI elements*/

        if(!existingChat(chat.chatID)){
            yarnPlace.addChat(chat);

            //Initialize listeners
            chat.updator.initChangeListener(activity);
            initChildListener(activity);
            addChildListener();

            //Update the Info Window
            if(yarnPlace.infoWindow != null && yarnPlace.infoWindow.window.isShowing())
                yarnPlace.infoWindow.update();

            //Record the chat if this local user is in it
            if(chat.checkForUserInChat(localUserID)){
                recorder.recordChat(chat);
            }else{
                //Notify the user of a chat suggestion because they are currently not in it
                Notifier.getInstance().addSuggestion("Chat suggestion",
                        "A new chat was created at "
                                + chat.yarnPlace.placeMap.get("name") + " on "
                                + chat.chatDate + " at " + chat.chatTime, chat);
            }
        }
    }

    public void addChildListener() {
        /*Listens to children under to the Yarn Place ID in the database*/
        if(yarnPlace.placeRef.getKey() != null) {

            yarnPlace.placeRef.addChildEventListener(childEventListener);
        }
    }

    public void removeChildListener(){
        /*Removes the child listener*/
        yarnPlace.placeRef.removeEventListener(childEventListener);
    }

    public boolean existingChat(String chatID) {
        /*Checks if the passed chat ID exists in the application*/

        //Check if it exists in the Yarn Place
        if(yarnPlace.getChats() != null) {
            for(int i = 0 ; i < yarnPlace.getChats().size(); i++)
            {
                if(chatID.equals(yarnPlace.getChats().get(i).chatID)) return true;
            }
        }

        //Check if it exists in the recorded chat's list
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
        /*Checks if the updater is ready. Its considered ready when all the found chats are ready
        by comparing a list of chats against the length of children in a database snapshot
         */

        Log.d(TAG,chatList.size() + " " + amount);

        return chatList.size() == amount;
    }

    public void removeChat(String removedChatPlaceID) {
        //Removes the passed chat from the yarn Place

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
