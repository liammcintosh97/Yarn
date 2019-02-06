package com.example.liammc.yarn.core;

import android.util.Log;

import com.example.liammc.yarn.Events.Chat;
import com.example.liammc.yarn.utility.DateTools;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatRecorder
{
    private static String TAG = "ChatRecorder";

    public HashMap<Long,ArrayList<Chat>> recordedChats;
    private ArrayList<Chat> chatList;

    public ChatRecorder()
    {
        this.recordedChats = new HashMap<Long,ArrayList<Chat>>();
        this.chatList = new ArrayList<>();
    }

    public void recordChat(Chat chat)
    {
        chatList.add(chat);

        long dateMilli = DateTools.dateStringToMilli(chat.chatDate);
        Log.d(TAG,"Date = " + chat.chatDate  + " | Milli = " + dateMilli);

        if(dateMilli != 0)
        {
            recordedChats.put(dateMilli,chatList);
        }
    }
}
