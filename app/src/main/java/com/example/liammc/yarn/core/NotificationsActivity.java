package com.example.liammc.yarn.core;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.liammc.yarn.Events.Notifier;
import com.example.liammc.yarn.Notification;
import com.example.liammc.yarn.R;

public class NotificationsActivity extends AppCompatActivity {

    Notifier notifier;

    //UI
    private ViewGroup main;
    private TextView defaultText;
    private ScrollView notificationsScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        setUpUI();
        setNotifier();

        registerReceiver(notifier.timeChangeReceiver, notifier.intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notifier.timeChangeReceiver);
    }


    //region Set UP

    private void setUpUI()
    {
        notificationsScrollView = findViewById(R.id.notificationsScrollView);
        defaultText = findViewById(R.id.defaultText);
        main = findViewById(R.id.main);
    }

    private void setNotifier()
    {
        notifier = Notifier.getInstance();

        notifier.setNotificationListener(new Notifier.NotificationListener() {
            @Override
            public void onNotificationAdded(Notification notification) {
                addNotificationToScrollView(notification);
            }
        });
    }

    //endregion

    //region Button Methods

    public void onRemoveNotificationPressed(View view, Notification notification)
    {
        notificationsScrollView.removeView(view);

        if(notificationsScrollView.getChildCount() == 0)
        {
            defaultText.setVisibility(View.VISIBLE);
        }

        notifier.removeNotification(notification);
    }

    //endregion

    //region Local Private Methods

    private void addNotificationToScrollView(final Notification notification)
    {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        final View element = inflater.inflate(R.layout.notfication_element,
                main,false);

        element.findViewById(R.id.closeNotification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveNotificationPressed(element,notification);
            }
        });

        TextView message = element.findViewById(R.id.message);
        message.setText(notification.message);

        notificationsScrollView.addView(element);

        defaultText.setVisibility(View.INVISIBLE);
    }

    //endregion
}
