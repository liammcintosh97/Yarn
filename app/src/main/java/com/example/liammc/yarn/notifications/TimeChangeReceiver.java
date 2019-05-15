package com.example.liammc.yarn.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.utility.DateTools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TimeChangeReceiver {
    /*This Receiver class is used to check for time and date changes. It is used in conjunction
    with the notifier to tell the firebaseUser if there is an upc0ming chat that they have joined
     */

    public static IntentFilter intentFilter;

    static {
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
    }

    public BroadcastReceiver receiver;

    public TimeChangeReceiver(){
        this.initReceiver();
    }

    //region Init

    private void initReceiver() {
        /*Initializes the receiver to check when the time changes and then notify the firebaseUser when
        * there is an upcoming chat*/

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(Intent.ACTION_DATE_CHANGED)){

                    Date currentTime = Calendar.getInstance().getTime();
                    ArrayList<Chat> chatList = Recorder.getInstance().chatList;

                    for(int i  = 0 ; i < chatList.size(); i++)
                    {
                        Chat chat = chatList.get(i);
                        Date chatDate = DateTools.stringToDate(chat.chatDate);

                        if(currentTime.equals(chatDate))
                        {
                            Notifier.getInstance().addNotification(context,"Upcoming Chat",
                                    "You have a chat today at " +
                                            chat.yarnPlace.placeMap.get("name") + " at "
                                            + chat.chatTime);
                        }
                    }
                }
            }
        };
    }

    //endregion

}
