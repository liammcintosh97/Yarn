package com.example.liammc.yarn.Events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.liammc.yarn.Notification;
import com.example.liammc.yarn.core.ChatRecorder;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.DateTools;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class Notifier
{
    //region singleton pattern
    private static final Notifier instance = new Notifier();

    //private constructor to avoid client applications to use constructor
    private Notifier(){}

    public static Notifier getInstance(){
        return instance;
    }

    //endregion

    //region Notification Listener
    private NotificationListener notificationListener;

    public interface NotificationListener{
        void onNotificationAdded(Notification notification);
    }

    public NotificationListener getNotificationListenerListener() {
        return notificationListener;
    }

    public void setNotificationListener(NotificationListener listener) {
        this.notificationListener = listener;
    }
    //endregion

    //region SuggestionListener

    private SuggestionListener suggestionListener;

    public interface SuggestionListener{
        void onSuggestionAdded(Notification notification,Chat chat);
    }

    public SuggestionListener getSuggestionsListenerListener() {
        return suggestionListener;
    }

    public void setSuggestionListener(SuggestionListener listener) {
        this.suggestionListener = listener;
    }
    //endregion

    private static int NOTIFICATION_PROXIMITY = 1000;
    private static String CHANNEL_ID = "notifier";

    public ArrayList<Notification> notifications;
    public ArrayList<Notification> chatSuggestions;
    public static IntentFilter intentFilter;

    static {
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
    }

    //region Public Methods
    public void addNotification(Context context,String title, String message)
    {
        Notification notification = new Notification(title,message);
        notifications.add(notification);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                //.setSmallIcon(R.drawable.)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notification.id, mBuilder.build());

        notificationListener.onNotificationAdded(notification);
    }

    public void addChatSuggestion(String title, String message, Chat chat)
    {
        Notification notification = new Notification(title,message);
        chatSuggestions.add(notification);

        suggestionListener.onSuggestionAdded(notification, chat);
    }

    public void removeNotification(Notification notification)
    {
        for(int i = 0; i < notifications.size(); i++)
        {
            if(notification.equals(notifications.get(i)))
            {
                notifications.remove(i);
            }
        }
    }

    public void removeSuggestion(Notification notification)
    {
        for(int i = 0; i < chatSuggestions.size(); i++)
        {
            if(notification.equals(chatSuggestions.get(i)))
            {
                chatSuggestions.remove(i);
            }
        }
    }

    public void listenToChat(final Context context,final Chat chat)
    {
        chat.setListener(new Chat.ValueChangeListener() {
            @Override
            public void onAcceptedChange() {

                if(chat.accepted) addNotification(context,"Chat Accepted","Your chat at "
                         + chat.chatPlaceName + " on " + chat.chatDate + " at " + chat.chatTime
                        + " was accepted");
            }

            @Override
            public void onActiveChange() {

            }

            @Override
            public void onCanceledChange()
            {
                if(chat.accepted) addNotification(context,"Chat Canceled","Your chat at "
                        + chat.chatPlaceName + " on " + chat.chatDate + " at " + chat.chatTime
                        + " was canceled");
            }
        });
    }

    public void onLocationChanged(Context context, Location location)
    {
        int nearbyChats = 0;
        ArrayList<Chat> chatList = ChatRecorder.getInstance().chatList;

        if(chatList != null){

            for(int i  = 0 ; i < chatList.size(); i++)
            {
                Chat chat = chatList.get(i);

                Location chatLocation = new Location(LocationManager.GPS_PROVIDER);
                chatLocation.setLatitude(chat.chatLatLng.latitude);
                chatLocation.setLatitude(chat.chatLatLng.longitude);

                if(chatLocation.distanceTo(location) < NOTIFICATION_PROXIMITY) nearbyChats++;
            }

            addNotification(context,"Nearby Chats","You have " +
                    nearbyChats + " chats near you!");
        }

    }


    //endregion

    //region BroadcastReceiver
    public final BroadcastReceiver timeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(Intent.ACTION_DATE_CHANGED)){

                Date currentTime = Calendar.getInstance().getTime();
                ArrayList<Chat> chatList = ChatRecorder.getInstance().chatList;

                for(int i  = 0 ; i < chatList.size(); i++)
                {
                    Chat chat = chatList.get(i);
                    Date chatDate = DateTools.dateStringToDateObject(chat.chatDate);

                    if(currentTime.equals(chatDate))
                    {
                        addNotification(context,"Upcoming Chat","You have a chat today at "
                        + chat.chatPlaceName + " at " + chat.chatTime);
                    }
                }
            }
        }
    };
    //endregion
}
