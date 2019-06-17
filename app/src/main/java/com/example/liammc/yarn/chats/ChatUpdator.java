package com.example.liammc.yarn.chats;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.core.ChatActivity;
import com.example.liammc.yarn.core.ChatPlannerActivity;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.planner.EventWindow;
import com.example.liammc.yarn.yarnPlace.InfoWindow;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ChatUpdator {

    private static final String TAG = "ChatUpdator";
    private Chat chat;

    public ChatUpdator(Chat _chat) {
        this.chat = _chat;
    }

    public void initDataListener(final String dataType) {

        chat.chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Log.d(TAG,"This chat node doesn't exist");
                    chat.getValueChangelistener().onDeletion(chat,chat.chatRef);
                    return;
                }

                snapshot.getValue().toString();

                if(translateDatabase(dataType,snapshot)) {
                    Log.d(TAG,"Updated Chat(" + chat.chatID + ")'s " + dataType);
                    chat.checkReady();
                }
                else Log.d(TAG,dataType + " doesn't exist" + chat.chatRef.toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG,"Unable to get " + dataType + "value from database");
            }
        });
    }

    public void initChangeListener(final Activity activity){

        final Notifier notifier = Notifier.getInstance();
        final Recorder recorder = Recorder.getInstance();

        Chat.ValueChangeListener valueListener = new Chat.ValueChangeListener() {
            @Override
            public void onAccepted(Chat chat, DatabaseReference chatRef) {
                if(chat.hostUser.userID != LocalUser.getInstance().userID) recorder.recordChat(chat);

                if(chat.guestUser != null && chat.guestUser.userID != LocalUser.getInstance().userID){
                    notifier.addNotification(activity,"Chat Accepted"
                            ,"Your chat at " + chat.yarnPlace.placeMap.get("name") + " on "
                                    + chat.chatDate + " at " + chat.chatTime + " was chatAccepted");
                }

                if(activity instanceof ChatPlannerActivity)updateChatPlanner(activity);
                if(activity instanceof ChatActivity)updateChatActivity(activity);
            }

            @Override
            public void onActiveChange(Chat chat, DatabaseReference chatRef) {
                if(chat.chatActive)Log.d(TAG,"Chat - " + chat.chatID + "was activated by " + chat.hostUser.userID);

                if(activity instanceof ChatActivity)updateChatActivity(activity);
            }

            @Override
            public void onDeletion(Chat chat, DatabaseReference chatRef) {

                Log.d(TAG,"Chat - " + chat.chatID + "was deleted by " + chat.hostUser.userID);

                if(chat.guestUser != null && !chat.chatActive) notifier.addNotification(activity,"Chat Canceled"
                        ,"Chat at " + chat.yarnPlace.placeMap.get("name") + " on "
                                + chat.chatDate + " at " + chat.chatTime + " was chatCanceled");

                Recorder.getInstance().removeChat(chat.chatID);
                chat.yarnPlace.yarnPlaceUpdater.removeChat(chat.yarnPlace.placeMap.get("id"));


                if(activity instanceof MapsActivity)updateInfoWindow();
                if(activity instanceof ChatActivity)updateChatActivity(activity);
            }
        };

        chat.setValueChangelistener(valueListener);
    }

    private boolean translateDatabase(String dataType, DataSnapshot snapshot) {
        switch(dataType) {
            //region Host
            case ("host"): {

                String chatHostID =  (String)snapshot.child("host").getValue();
                if(chatHostID == null) return false;

                if (chatHostID.equals(chat.localUserID)) {
                    chat.hostUser = LocalUser.getInstance();
                } else chat.hostUser = new YarnUser(chatHostID);
                return true;
            }
            //endregion
            //region Guest
            case ("guest"): {

                String chatGuestID =  (String)snapshot.child("guest").getValue();
                if(chatGuestID == null) return false;

                if(chat.getValueChangelistener() == null) Log.e(TAG,"Value Change listener = null");

                if (chatGuestID.equals(chat.localUserID)) {
                    chat.guestUser = LocalUser.getInstance();
                    if(chat.getValueChangelistener() != null)
                        chat.getValueChangelistener().onAccepted(chat,chat.chatRef);
                } else if (!chatGuestID.equals("")) {
                    chat.guestUser = new YarnUser( chatGuestID);
                    if(chat.getValueChangelistener() != null)
                        chat.getValueChangelistener().onAccepted(chat,chat.chatRef);
                } else {
                    chat.guestUser = null;
                }
                chat.setGuestReady(true);
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
                    if(chat.getValueChangelistener() != null)
                        chat.getValueChangelistener().onActiveChange(chat,chat.chatRef);
                    return true;
                } else return false;
            }
            //endregion
        }

        return false;
    }

    private void updateChatPlanner(Activity activity){
        ChatPlannerActivity plannerActivity = (ChatPlannerActivity)activity;
        EventWindow eventWindow = plannerActivity.eventWindow;

        if(eventWindow != null && eventWindow.window.isShowing()) eventWindow.update();
    }

    private void updateInfoWindow(){
        InfoWindow infoWindow = chat.yarnPlace.infoWindow;

        if(infoWindow != null && infoWindow.window.isShowing()){
            chat.yarnPlace.infoWindow.update();
        }
    }

    private  void updateChatActivity(Activity activity){
        ChatActivity chatActivity = (ChatActivity) activity;

        chatActivity.update();
    }
}
