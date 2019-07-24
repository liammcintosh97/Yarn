package com.example.liammc.yarn.chats;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

public class ChatLogger {

    private final String TAG = "ChatLogger";

    //region singleton pattern
    private static final ChatLogger instance = new ChatLogger();

    //private constructor to avoid client applications to use constructor
    private ChatLogger(){ }

    public static ChatLogger getInstance(){
        return instance;
    }
    //endregion

    //region Public Methods

    public void logChat(YarnUser _otherUser,Chat _chat){

       YarnUser otherUser =  _otherUser;
       Chat chat =  _chat;
       DatabaseReference chatLogRef = getChatLogRef(chat.chatID);

       chatLogRef.child("placeID").setValue(chat.yarnPlace.placeMap.get("id")
               ,buildCompletionListener("placeID"));

       chatLogRef.child("time").setValue(chat.chatTime
               ,buildCompletionListener("time"));

       chatLogRef.child("date").setValue(chat.chatDate
               ,buildCompletionListener("date"));

       chatLogRef.child("length").setValue(chat.chatLength
               ,buildCompletionListener("length"));

       chatLogRef.child("otherUserID").setValue(otherUser.userID
               ,buildCompletionListener("otherUserID"));
    }

    //region Getters and Setters
    public DatabaseReference getChatLogRef(String chatId){
        LocalUser localUser =  LocalUser.getInstance();
        return localUser.userDatabaseReference.child("chatLog").child(chatId);
    }
    //endregion

    //endregion

    //region Private Methods
    private DatabaseReference.CompletionListener buildCompletionListener(final String valueType){

        DatabaseReference.CompletionListener listener =  new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                //Completed without error
                if(databaseError == null){
                    Log.e(TAG,"Update to " + valueType + " was successful");
                }
                //Completed with an error
                else{
                    Log.e(TAG,"There was an error when trying to update " + valueType + " - "
                    + databaseError.getMessage());
                }

            }
        };

        return listener;
    }
    //endregion
}
