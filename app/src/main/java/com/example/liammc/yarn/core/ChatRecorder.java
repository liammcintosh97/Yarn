package com.example.liammc.yarn.core;

import android.content.Context;
import android.util.Log;

import com.example.liammc.yarn.Events.Chat;
import com.example.liammc.yarn.Events.Notifier;
import com.example.liammc.yarn.utility.DateTools;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatRecorder
{
    //region singleton pattern
    private static final ChatRecorder instance = new ChatRecorder();

    //private constructor to avoid client applications to use constructor
    private ChatRecorder(){
        this.notifier = Notifier.getInstance();
    }

    public static ChatRecorder getInstance(){
        return instance;
    }
    //endregion

    private static String TAG = "ChatRecorder";

    public HashMap<Long,ArrayList<Chat>> recordedChats;
    public ArrayList<Chat> chatList;
    private Notifier notifier;

    /*
    public ChatRecorder()
    {
        this.recordedChats = new HashMap<Long,ArrayList<Chat>>();
        this.chatList = new ArrayList<>();
    }*/

    public void recordChat(Context context, Chat chat)
    {
        chatList.add(chat);

        long dateMilli = DateTools.dateStringToMilli(chat.chatDate);
        Log.d(TAG,"Date = " + chat.chatDate  + " | Milli = " + dateMilli);

        if(dateMilli != 0)
        {
            recordedChats.put(dateMilli,chatList);
        }

        notifier.listenToChat(context,chat);
    }
}
