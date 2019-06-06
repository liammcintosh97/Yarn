package com.example.liammc.yarn.core;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.notifications.TimeChangeReceiver;
import com.example.liammc.yarn.yarnPlace.YarnPlace;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private final String TAG = "ChatActivity";
    private Chat currentChat;
    private LocalUser localUser;
    private YarnUser otherUser;
    private YarnPlace currentYarnPlace;
    private TimeChangeReceiver timeChangeReceiver;

    //UI
    private View personInfo;
    private TextView personNameTextView;
    private ImageView personImageView;
    private ViewGroup stars;

    private TextView placeTitleTextView;
    private ImageView placeImageView;
    private TextView placeAddressTextView;

    private TextView dateTextView;
    private TextView timeTextView;
    private TextView lengthTextView;

    private TextView messageTextView;
    private Button startButton;
    private ViewGroup buttonBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initReceivers();
        initChannels();
        initCurrentChat();

        if(currentChat == null){
            Log.e(TAG,"Current chat is null!");
        }else{
            initOtherUser();
            initYarnPlace();
            initUI();
        }
    }

    //region Init

    private void initUI(){
        personInfo =  findViewById(R.id.personInfo);
        personNameTextView =  personInfo.findViewById(R.id.name);
        personImageView =  personInfo.findViewById(R.id.profilePicture);
        stars = personInfo.findViewById(R.id.stars);

        placeTitleTextView = findViewById(R.id.placeTitle);
        placeImageView =  findViewById(R.id.placeImage);
        placeAddressTextView =  findViewById(R.id.placeAddress);

        dateTextView =  findViewById(R.id.date);
        timeTextView =  findViewById(R.id.time);
        lengthTextView = findViewById(R.id.length);

        messageTextView =  findViewById(R.id.message);
        startButton =  findViewById(R.id.startButton);
        buttonBar =  findViewById(R.id.buttonBar);

        update();
    }

    private void initReceivers(){

        timeChangeReceiver = new TimeChangeReceiver(this);
        registerReceiver(timeChangeReceiver.receiver,TimeChangeReceiver.intentFilter);
    }

    private void initChannels(){
        Notifier.getInstance().createNotificationChannel(this);
    }

    private void initCurrentChat(){
        String currentChatID = getIntent().getStringExtra("chatID");

        if(currentChatID == null || currentChatID.equals("")){
            Log.e(TAG,"The current Chat ID is null!");
            return;
        }

        currentChat = Recorder.getInstance().getRecordedChat(currentChatID);
        currentChat.updator.initChangeListener(this);

    }

    private void initOtherUser(){

        localUser = LocalUser.getInstance();

        if(!currentChat.hostUser.userID.equals(localUser.userID)) otherUser = currentChat.hostUser;
        else otherUser = currentChat.guestUser;
    }

    private void initYarnPlace(){

        currentYarnPlace =  currentChat.yarnPlace;
    }

    //endregion

    //region Public Methods

    public void update(){
        updatePlaceInfo();
        updateChatInfo();
        updateUserInfo();
        updateStartButton();
        updateButtonBar();
    }

    public boolean updatePlaceInfo(){

        if(currentYarnPlace == null){
            Log.e(TAG,"Yarn place is null");
            return false;
        }

        placeTitleTextView.setText(currentYarnPlace.placeMap.get("name"));
        placeImageView.setImageBitmap(currentYarnPlace.placePhoto);
        placeAddressTextView.setText(currentYarnPlace.address.getAddressLine(0));

        return true;
    }

    public boolean updateChatInfo(){

        if(currentChat == null){
            return false;
        }

        dateTextView.setText(currentChat.chatDate);
        timeTextView.setText(currentChat.chatTime);
        lengthTextView.setText(currentChat.chatLength);

        return true;
    }

    public void updateUserInfo(){

        if(otherUser == null){
            personInfo.setVisibility(View.INVISIBLE);
            messageTextView.setVisibility(View.VISIBLE);
        }
        else{
            personInfo.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.GONE);

            personNameTextView.setText(otherUser.userName);
            personImageView.setImageBitmap(otherUser.profilePicture);
            setStars();

        }

    }

    public boolean updateStartButton(){

        if(currentChat == null){
            Log.e(TAG,"Current Chat is null");
            return false;
        }

        if(otherUser == null) startButton.setActivated(false);
        else startButton.setActivated(true);

        return true;
    }

    public boolean updateButtonBar(){

        if(currentChat == null){
            Log.e(TAG,"Current Chat is null");
            return false;
        }

        if(currentChat.chatActive) buttonBar.setVisibility(View.INVISIBLE);
        else buttonBar.setVisibility(View.VISIBLE);

        return true;
    }

    //endregion

    //region Button Methods

    public void onCancelChatPressed(View v){
        currentChat.cancelChat();

        Intent intent =  new Intent(this,MapsActivity.class);
        startActivity(intent);
    }

    public void onStartChatPressed(View v){
        currentChat.activateChat();
    }

    //endregion

    //region Private Methods

    private void setStars(){

        //Set all the starts to invisible
        for(int i = 0; i < stars.getChildCount(); i++){
            stars.getChildAt(i).setVisibility(View.INVISIBLE);
        }

        //Set the stars to match the rating of thr user
        for(int i = 0; i < otherUser.rating; i++){
            stars.getChildAt(i).setVisibility(View.VISIBLE);
        }
    }

    //endregion
}
