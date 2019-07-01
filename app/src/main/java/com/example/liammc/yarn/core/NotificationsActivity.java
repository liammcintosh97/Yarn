package com.example.liammc.yarn.core;

import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
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
    private ViewGroup main;
    private TextView defaultText;
    private ScrollView notificationsScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initUI();
        initNotifier();
        initReceivers();
        initChannels();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(timeChangeReceiver.receiver);
    }


    //region Init

    private void initUI() {
        /*Initializes the UI for the activity*/

        notificationsScrollView = findViewById(R.id.notificationsScrollView);
        defaultText = findViewById(R.id.defaultText);
        main = findViewById(R.id.main);
    }

    private void initNotifier() {
        /*Initializes the Notifier for the activity*/

        notifier = Notifier.getInstance();

        notifier.setNotificationListener(new Notifier.NotificationListener() {
            @Override
            public void onNotificationAdded(Notification notification) {
                addNotificationToScrollView(notification);
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
    //endregionc

    //region Button Methods

    public void onRemoveNotificationPressed(View view, Notification notification) {
        /*Removes the notification from the scroll and the notifier*/

        notificationsScrollView.removeView(view);

        if(notificationsScrollView.getChildCount() == 0)
        {
            defaultText.setVisibility(View.VISIBLE);
        }

        notifier.removeNotification(notification);
    }

    //endregion

    //region Private Methods

    private void addNotificationToScrollView(final Notification notification) {
        /*Adds the notification to the scroll view*/

        //Inflate the notification
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View notificationElement = inflater.inflate(R.layout.notfication_element,
                main,false);

        //Set button
        setButtonListener(notificationElement,notification);

        //Set Text
        TextView message = notificationElement.findViewById(R.id.message);
        message.setText(notification.message);

        //Add to scroll view
        notificationsScrollView.addView(notificationElement);
        defaultText.setVisibility(View.INVISIBLE);
    }

    private void setButtonListener(final View notificationElement,final Notification notification){
        /*Sets the onClick listener*/

        notificationElement.findViewById(R.id.closeNotification)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveNotificationPressed(notificationElement,notification);
            }
        });
    }

    //endregion
}
