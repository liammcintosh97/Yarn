package com.example.liammc.yarn.planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.ChatActivity;
import com.example.liammc.yarn.dialogs.CancelDialog;

public class EventElement {
    /*This Class is used in conjunction with the Chat Planner Activity. It describes a chat elementView
    within the event window when the user clicks on a date
     */

    private EventWindow eventWindow;
    public final Chat chat;
    private final String TAG = "PlannerActivity";

    //Dialog
    private CancelDialog cancelDialog;

    //UI
    public View parentView;
    private TextView chatDetails;
    private ImageView hostImage;
    private ImageView guestImage;
    private Button cancelButton;

    public EventElement(EventWindow _eventWindow, Chat _chat){
        this.chat = _chat;
        this.eventWindow = _eventWindow;

        init();
        initUI();
    }

    //region Init

    private void init(){
        //Initializes required objects

        cancelDialog = new CancelDialog();
        cancelDialog.init(this);
    }

    private void initUI(){
        //Inflate the elementView and add it to the event window scroll view
        parentView = inflate(R.layout.chat_window_scroll_view_element,false);

        //Set the elementView's button listeners
        parentView.findViewById(R.id.cancelChatButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelChatPress(chat);
            }
        });

        parentView.findViewById(R.id.chatDetails).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPressChat(v);
            }
        });

        //Get the UI references
        hostImage = parentView.findViewById(R.id.hostImage);
        guestImage =  parentView.findViewById(R.id.guestImage);
        chatDetails = parentView.findViewById(R.id.chatDetails);
        cancelButton = parentView.findViewById(R.id.cancelChatButton);

        //Update the elementView
        update();
    }



    //endregion

    //region Public Methods

    private void update(){
        //Set the text and descriptions
        String displayText = chat.yarnPlace.placeMap.get("name") + "\n" + chat.chatDate + "\n"
                + chat.chatTime + "/n" + chat.chatLength;

        parentView.setContentDescription(chat.chatID);
        chatDetails.setText(displayText);

        //Set the images' visibilities
        hostImage.setVisibility(View.VISIBLE);
        if(chat.guestUser == null) guestImage.setVisibility(View.INVISIBLE);
        else guestImage.setVisibility(View.VISIBLE);

        if(chat.chatActive)  cancelButton.setEnabled(false);
        else cancelButton.setEnabled(true);
    }

    //endregion

    //region Buttons

    public void onVerifyCancelPress(Chat chat){
        /*This runs when the firebaseUser verifies that they want to cancel the chat. It removes it from the
         * event scroll view, the database and the application's system*/
        eventWindow.removeChat(chat.chatID);
        chat.yarnPlace.infoWindow.removeChatFromScrollView(chat.chatID);
        chat.removeChat();

        if(eventWindow.chatScrollElements.getChildCount() == 0) eventWindow.dismiss();
    }

    public void onPressChat(View view){
        Intent intent = new Intent(eventWindow.plannerActivity, ChatActivity.class);
        intent.putExtra("chatID",chat.chatID);
        eventWindow.plannerActivity.startActivity(intent);
    }

    private void onCancelChatPress(Chat chat){
        /*Runs when the firebaseUser clicks on the cancel button of the chat*/

        //Show the warning dialog
        cancelDialog.show(eventWindow.plannerActivity.getSupportFragmentManager(),TAG);

        //Pass the chat to the warning dialog so that it can interact with it
        Bundle bundle = new Bundle();
        bundle.putString("chatID",chat.chatID);
        cancelDialog.setArguments(bundle);
    }

    //endregion

    //region Private methods

    private View inflate(int layoutID, boolean attachToRoot){
        /*Inflates the layout that has the passed layoutID*/

        LayoutInflater inflater = (LayoutInflater) eventWindow.plannerActivity
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutID,eventWindow.plannerActivity.parentViewGroup
                ,attachToRoot);

        return view;
    }

    //endregion
}
