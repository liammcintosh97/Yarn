package com.example.liammc.yarn.notifications;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.NotificationsActivity;

import java.util.Random;

import static android.app.Activity.RESULT_OK;

public class Notification
{
    /*This is the notification class. Objects of this class are used throughout the application for
    displaying messages to the firebaseUser */

    public String title;
    public String message;
    public boolean seen;
    public int id;
    public Chat chat;

    protected Activity activity;
    protected View view;
    protected Button removeButton;
    protected Button detailsButton;

    public Notification(Chat _chat,String _title,String _message)
    {
        this.chat = _chat;
        this.title = _title;
        this.message = _message;
        this.seen = false;

        Random random = new Random();
        this.id = random.nextInt(100000);
    }

    //region Public Methods

    public View show(Activity _activity){

        activity = _activity;

        //Inflate the notification
        view = inflate(R.layout.element_notification);

        //Set button
        removeButton = view.findViewById(R.id.closeNotification);
        detailsButton = view.findViewById(R.id.detailsButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { onRemovePress();
                    }
                });
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { onDetailsPress();
            }
        });

        //Set Text
        detailsButton.setText(message);

        return view;
    }

    //endregion


    //region Button Methods

    public void onRemovePress() {
        /*Removes the notification from the scroll and the notifier*/

        LinearLayout elements = ((NotificationsActivity)activity).notificationElements;

        if(elements.getChildCount() == 0) {
            ((NotificationsActivity)activity).defaultText.setVisibility(View.VISIBLE);
        }

        elements.removeView(view);
        Notifier.getInstance().removeNotification(this);
    }

    void onDetailsPress(){

        Intent data = new Intent();
        String placeID = chat.yarnPlace.placeMap.get("id");
        String chatID =  chat.chatID;

        data.putExtra("placeID",placeID);
        data.putExtra("chatID",chatID);
        activity.setResult(RESULT_OK, data);

        activity.finish();
    }

    //endregion

    //region Protected Methods

    protected View inflate(int layoutID ) {
        /*Inflates the given layout ID*/

        LayoutInflater inflater = activity.getLayoutInflater();
        return inflater.inflate(layoutID,null);
    }

    //endregion
}