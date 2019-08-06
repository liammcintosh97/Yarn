package com.example.liammc.yarn.core;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.notifications.Notification;
import com.example.liammc.yarn.R;

public class NotificationsActivity extends YarnActivity {
    /*This Activity is the used for displaying internal notifications to the firebaseUser*/

    //UI
    public TextView defaultText;
    public LinearLayout notificationElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initUI();
        initNotifierListeners();
        initNotifications();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(timeChangeReceiver.receiver);
    }


    //region Init

    private void initUI() {
        /*Initializes the UI for the activity*/

        notificationElements = findViewById(R.id.elements);
        defaultText = findViewById(R.id.defaultText);
    }

    private void initNotifierListeners() {
        /*Initializes the Notifier for the activity*/

        notifier.setNotificationListener(new Notifier.NotificationListener() {
            @Override
            public void onNotificationAdded(Notification notification) {
                addNotification(notification);
            }
        });
    }

    private void initNotifications(){
        for(Notification notification : notifier.notifications){
            addNotification(notification);
        }
    }
    //endregion

    //region Private Methods

    private void addNotification(final Notification notification) {
        /*Adds the notification to the scroll view*/

        View element = notification.show(this);
        defaultText.setVisibility(View.INVISIBLE);
        notificationElements.addView(element);
    }

    //endregion
}
