package com.example.liammc.yarn.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.chats.ChatCreator;
import com.example.liammc.yarn.core.ChatActivity;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.core.YarnActivity;
import com.example.liammc.yarn.networking.InternetListener;
import com.example.liammc.yarn.utility.DateTools;
import com.example.liammc.yarn.yarnPlace.InfoWindow;
import com.example.liammc.yarn.yarnPlace.YarnPlace;

import java.util.ArrayList;
import java.util.Calendar;

public class TimeChangeReceiver {
    /*This Receiver class is used to check for time and date changes. It is used in conjunction
    with the notifier to tell the firebaseUser if there is an upc0ming chat that they have joined
     */

    private final String TAG = "TimeChangeReceiver";
    private YarnActivity activity;
    InternetListener internetListener;

    long secondsInMilli = 1000;
    long minutesInMilli = secondsInMilli * 60;
    long hoursInMilli = minutesInMilli * 60;
    long daysInMilli = hoursInMilli * 24;
    long yearsInMilli = daysInMilli * 365;

    public static IntentFilter intentFilter;

    static {
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
    }

    public BroadcastReceiver receiver;

    public TimeChangeReceiver(YarnActivity _activity){
        this.activity = _activity;
        this.internetListener =  activity.internetListener;
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

                if (action.equals(Intent.ACTION_DATE_CHANGED))dateChange(context);
                else if(action.equals(Intent.ACTION_TIME_TICK)) timeTick(context);
                else if(action.equals(Intent.ACTION_TIMEZONE_CHANGED)){
                    dateChange(context);
                    timeTick(context);
                }

                if(!internetListener.isConnected()){
                    internetListener.internetDialog.alert(activity.getSupportFragmentManager(),TAG);
                }
            }
        };
    }

    //endregion

    //region Private Methods

    private void dateChange(Context context){

        long currentTime = Calendar.getInstance().getTime().getTime();
        ArrayList<Chat> chatList = Recorder.getInstance().chatList;

        for(int i  = 0 ; i < chatList.size(); i++)
        {
            Chat chat = chatList.get(i);
            long chatDate = DateTools.dateStringToMilli(chat.chatDate);

            if(yearDifference(currentTime,chatDate) == 0
                    && dayDifference(currentTime,chatDate) == 0) {
                Notifier.getInstance().addNotification(chat,context,"Upcoming Chat",
                        "You have a chat today at " +
                                chat.yarnPlace.placeMap.get("name") + " at "
                                + chat.chatTime);
            }
        }
    }

    private void timeTick(Context context){
        //This method runs every minute and is used for updating things based on time

        long currentTime = Calendar.getInstance().getTime().getTime();
        ArrayList<Chat> chatList = Recorder.getInstance().chatList;

        //Update the info window if it's showing
        if(activity instanceof MapsActivity){

            YarnPlace touchedYarnPlace = ((MapsActivity) activity).getTouchedYarnPlace();

            if(touchedYarnPlace != null){
                InfoWindow window = touchedYarnPlace.infoWindow;
                if(window != null && window.isShowing()){
                    window.updateInfoWindow();

                    ChatCreator creator = window.chatCreator;
                    if(creator != null && creator.isShowing()){
                        creator.appointmentPicker.updateAppointmentTimes();
                    }
                }
            }


        }

        for(int i  = 0 ; i < chatList.size(); i++) {
            Chat chat = chatList.get(i);
            long chatDate = DateTools.dateStringToMilli(chat.chatDate);

            if(yearDifference(currentTime,chatDate) == 0
                    && dayDifference(currentTime,chatDate) == 0
                    && minuteDifference(currentTime,chatDate) <= 5) {
                goToChatActivity(context);
            }
        }

        Log.d(TAG,"Time Tick");
    }

    private void goToChatActivity(Context context){

        Intent intent = new Intent(context, ChatActivity.class);
        context.startActivity(intent);

    }

    private long yearDifference(long start, long end){
        long difference = start - end;
        return difference /= yearsInMilli;
    }

    private long dayDifference(long start, long end){
        long difference = start - end;
        return difference /= daysInMilli;
    }

    private long minuteDifference(long start, long end){
        long difference = start - end;
        return difference /= minutesInMilli;
    }

    //endregion

}
