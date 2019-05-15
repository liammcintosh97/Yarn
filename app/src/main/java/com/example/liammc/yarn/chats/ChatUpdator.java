package com.example.liammc.yarn.chats;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.core.Recorder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ChatUpdator {

    private static final String TAG = "ChatUpdator";
    private Chat chat;
    private Chat.ValueChangeListener valueChangelistener;

    public ChatUpdator(Chat _chat) {
        this.chat = _chat;
        this.valueChangelistener = _chat.getValueChangelistener();
    }

    public void addChatDataListener(final String dataType) {

        chat.chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!snapshot.exists()){
                    Log.d(TAG,"This chat node doesn't exsist");
                    chat.getValueChangelistener().onDeletion(chat,chat.chatRef);
                }

                if(translateDatabase(dataType,snapshot))
                {
                    Log.d(TAG,"Updated " + dataType + " from database : "
                            + snapshot.getValue().toString());

                    chat.checkReady();
                }
                else Log.e(TAG,"Fatal error when trying to update "
                        + dataType + " from database reference - " + chat.chatRef.toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(TAG,"Unable to get " + dataType + "value from database");
            }
        });
    }

    public void addValueChangeListener(final Activity activity){

        final Notifier notifier = Notifier.getInstance();
        final Recorder recorder = Recorder.getInstance();

        chat.setValueChangelistener(new Chat.ValueChangeListener() {
            @Override
            public void onAccepted(Chat chat, DatabaseReference chatRef) {

                recorder.recordChat(chat);

                if(chat.guestUser != null) notifier.addNotification(activity,"Chat Accepted"
                        ,"Your chat at " + chat.yarnPlace.placeMap.get("name") + " on "
                                + chat.chatDate + " at " + chat.chatTime + " was chatAccepted");
            }

            @Override
            public void onActiveChange(Chat chat, DatabaseReference chatRef) {

            }

            @Override
            public void onDeletion(Chat chat, DatabaseReference chatRef) {

                if(chat.guestUser != null) notifier.addNotification(activity,"Chat Canceled"
                        ,"Your chat at " + chat.yarnPlace.placeMap.get("name") + " on "
                                + chat.chatDate + " at " + chat.chatTime + " was chatCanceled");

                Recorder.getInstance().removeChat(chat.chatID);
                chat.yarnPlace.removeChatFromScrollView(chat.chatID);
                chat.yarnPlace.yarnPlaceUpdater.removeChat(chat.yarnPlace.placeMap.get("id"));
                chat.yarnPlace.updateScrollView(activity);
            }
        });
    }

    private boolean translateDatabase(String dataType, DataSnapshot snapshot) {
        switch(dataType) {
            //region Host
            case ("host"): {

                String chatHostID =  (String)snapshot.child("host").getValue();
                if(chatHostID == null) return false;

                if (chatHostID.equals(chat.localUserID)) {
                    chat.hostUser = LocalUser.getInstance();
                } else {
                    Log.d(TAG, String.valueOf( snapshot.getValue()));
                    chat.hostUser = new YarnUser( chatHostID);
                }
                return true;
            }
            //endregion
            //region Guest
            case ("guest"): {

                chat.setGuestReady(true);
                String chatGuestID =  (String)snapshot.child("guest").getValue();
                if(chatGuestID == null) return false;

                if (chatGuestID.equals(chat.localUserID)) {
                    chat.guestUser = LocalUser.getInstance();
                    if(valueChangelistener != null) valueChangelistener.onAccepted(chat,chat.chatRef);
                } else if (!chatGuestID.equals("")) {
                    chat.guestUser = new YarnUser( chatGuestID);
                    if(valueChangelistener != null) valueChangelistener.onAccepted(chat,chat.chatRef);
                } else {
                    chat.guestUser = null;
                }
                return true;
            }
            //endregion
            //region Date
            case ("date"): {
                String date =  (String)snapshot.child("date").getValue();
                if(date != null){
                    chat.chatDate = date;
                    return true;
                }else return false;
            }
            //endregion
            //region Time
            case ("time"): {
                String time =  (String)snapshot.child("time").getValue();
                if(time != null) {
                    chat.chatTime = time;
                    return true;
                }else return false;
            }
            //endregion
            //region Length
            case ("length"): {
                String length =  (String)snapshot.child("length").getValue();
                if(length != null){
                    chat.chatLength = length;
                    return true;
                }else return false;
            }
            //endregion
            //region Active
            case ("active"): {
                Boolean active =  (Boolean)snapshot.child("active").getValue();
                if (active != null) {
                    chat.chatActive = active;
                    if(valueChangelistener != null) valueChangelistener.onActiveChange(chat,chat.chatRef);
                    return true;
                } else return false;
            }

        }

        return false;
    }

}
