package com.example.liammc.yarn.notifications;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.chats.Chat;


import java.util.ArrayList;

public class Notifier {
    /*This class is used when the system needs to notify the firebaseUser to certain events in the
    application. This class handles both internal and Android Notifications
     */

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
        void onSuggestionAdded(Notification notification, Chat chat);
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

    public ArrayList<Notification> notifications = new ArrayList<>();
    public ArrayList<Notification> chatSuggestions = new ArrayList<>();


    //region Public Methods

    public void addNotification(Context context,String title, String message) {
        /*This method adds a new notification to the system*/

        //Creates a new internal notification object
        Notification notification = new Notification(title,message);
        notifications.add(notification);

        //Creates a new Android notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                //.setSmallIcon(R.drawable.)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ccp_down_arrow)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        //Shows the Android notification to the firebaseUser
        notificationManager.notify(notification.id, mBuilder.build());

        //Returns the internal notification to the listener
        if(notificationListener != null) notificationListener.onNotificationAdded(notification);
    }

    public void createNotificationChannel(Activity activity) {
        /*This Method creates a notification Channel but only if the device's API is 26+*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "notifier";
            String description = "notifies the firebaseUser about app activities";

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void addChatSuggestion(String title, String message, Chat chat) {
        /*This method adds a new internal notification to notify the firebaseUser on suggested chats*/

        Notification notification = new Notification(title,message);
        chatSuggestions.add(notification);

        /*Returns the notification to the listener*/
        if(suggestionListener != null) suggestionListener.onSuggestionAdded(notification, chat);
    }

    public void removeNotification(Notification notification) {
        /*Removes the internal notification from the system*/

        notifications.remove(notification);
    }

    public void removeSuggestion(Notification notification) {
        /*Removes the internal chat suggestion notification from the system*/

        chatSuggestions.remove(notification);
    }

    //endregion

}
