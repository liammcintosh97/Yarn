package com.example.liammc.yarn.core;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.notifications.Notification;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.notifications.TimeChangeReceiver;

public class NotificationsActivity extends AppCompatActivity {
    /*This Activity is the used for displaying internal notifications to the firebaseUser*/

    Notifier notifier;
    TimeChangeReceiver timeChangeReceiver;

    //UI
    public TextView defaultText;
    public LinearLayout notificationElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initUI();
        initNotifier();
        initReceivers();
        initChannels();
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

    private void initNotifier() {
        /*Initializes the Notifier for the activity*/

        notifier = Notifier.getInstance();

        notifier.setNotificationListener(new Notifier.NotificationListener() {
            @Override
            public void onNotificationAdded(Notification notification) {
                addNotification(notification);
            }
        });
    }

    private void initReceivers() {
        /*Initializes the Time Change Receiver*/

        timeChangeReceiver = new TimeChangeReceiver(this);
        registerReceiver(timeChangeReceiver.receiver, TimeChangeReceiver.intentFilter);
    }

    private void initChannels(){
        Notifier.getInstance().createNotificationChannel(this);
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
