package com.example.liammc.yarn.notifications;

import android.app.Activity;
import android.view.View;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.ChatPlannerActivity;


public class Suggestion extends Notification {

    public Suggestion(Chat _chat, String _title, String _message) {
        super(_chat,_title,_message);
    }

    //region Public Methods
    @Override
    public View show(Activity _activity){

        activity = _activity;

        //Inflate the elementView and add it to the suggestion scroll view
        view = inflate(R.layout.chat_suggestion_element);

        //Set the elementView's button listeners
        removeButton = view.findViewById(R.id.removeSuggestionButton);
        detailsButton = view.findViewById(R.id.detailsButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { onRemovePress();
            }
        });
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDetailsPress();
            }
        });

        //Set the text and descriptions

        detailsButton.setContentDescription(chat.chatID);
        detailsButton.setText(message);

        return view;
    }

    //endregion

    //region Button Methods

    @Override
    public void onRemovePress() {
        /*Removes suggestion from the suggestion scroll view*/

        ((ChatPlannerActivity)activity).chatSuggestionElements.removeView(view);
        Notifier.getInstance().removeSuggestion(this);
    }
    //endregion
}
