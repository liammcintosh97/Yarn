package com.example.liammc.yarn.notifications;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.InitializationActivity;
import com.example.liammc.yarn.core.MapsActivity;


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
        void onSuggestionAdded(Suggestion suggestion);
    }

    public SuggestionListener getSuggestionsListenerListener() {
        return suggestionListener;
    }

    public void setSuggestionListener(SuggestionListener listener) {
        this.suggestionListener = listener;
    }
    //endregion

    private final String TAG = "Notifier";
    private static String CHANNEL_ID = "notifier";

    public ArrayList<Notification> notifications = new ArrayList<>();
    public ArrayList<Suggestion> chatSuggestions = new ArrayList<>();

    //region Public Methods

    public void addNotification(Chat chat,Context context,String title, String message) {
        /*This method adds a new notification to the system*/

        //Creates a new internal notification object
        Notification notification = new Notification(chat,title,message);

        if(exists(notifications,notification)){
            Log.i(TAG,"This suggestion already exists");
            return;
        }

        //Build the Android notification
        PendingIntent pendingIntent = buildPendingIntent((Activity) context);
        NotificationCompat.Builder mBuilder = buildNotification((Activity) context,title,message
                ,pendingIntent);

        //Shows the Android Notification to the user
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notification.id, mBuilder.build());

        //Add and return the notification to the internal system
        if(notificationListener != null) notificationListener.onNotificationAdded(notification);
        notifications.add(notification);
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

    public void addSuggestion(String title, String message, Chat chat) {
        /*This method adds a new internal notification to notify the firebaseUser on suggested chats*/

        Suggestion suggestion = new Suggestion(chat,title,message);

        if(exists(chatSuggestions,suggestion)){
            Log.i(TAG,"This suggestion already exists");
            return;
        }

        chatSuggestions.add(suggestion);

        /*Returns the notification to the listener*/
        if(suggestionListener != null) suggestionListener.onSuggestionAdded(suggestion);
    }

    public void removeNotification(Notification notification) {
        /*Removes the internal notification from the system*/

        notifications.remove(notification);
    }

    public void removeSuggestion(Suggestion suggestion) {
        /*Removes the internal chat suggestion notification from the system*/

        chatSuggestions.remove(suggestion);
    }

    //endregion

    //region Private Methods

    private PendingIntent buildPendingIntent(Activity activity){

        Intent intent = new Intent(activity, InitializationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity,
                0, intent, 0);

        return pendingIntent;
    }

    private NotificationCompat.Builder buildNotification(Activity activity,String title,
                                                         String message,PendingIntent pendingIntent){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                //.setSmallIcon(R.drawable.)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ccp_down_arrow)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return mBuilder;
    }

    private boolean exists(ArrayList<Suggestion> suggestions, Suggestion toCheck){

        for(Notification n : suggestions){
            if(n.message.equals(toCheck.message) || n.title.equals(toCheck.title)) return true;
        }
        return false;
    }

    private boolean exists(ArrayList<Notification> notifications, Notification toCheck){

        for(Notification n : notifications){
            if(n.message.equals(toCheck.message) || n.title.equals(toCheck.title)) return true;
        }
        return false;
    }

    //endregion
}
