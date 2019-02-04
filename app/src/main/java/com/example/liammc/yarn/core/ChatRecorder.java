package com.example.liammc.yarn.core;

import com.example.liammc.yarn.Events.Chat;
import com.example.liammc.yarn.utility.DateTools;

import java.util.HashMap;

public class ChatRecorder
{
    public HashMap<Long,Chat> recordedChats;

    public ChatRecorder()
    {
        this.recordedChats = new HashMap<Long,Chat>();
    }

    public void recordChat(Chat chat)
    {
        long dateMilli = DateTools.dateStringToMilli(chat.chatDate);

        if(dateMilli != 0)
        {
            recordedChats.put(dateMilli,chat);
        }
    }
}
