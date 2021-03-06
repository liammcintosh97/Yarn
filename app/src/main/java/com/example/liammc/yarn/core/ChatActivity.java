package com.example.liammc.yarn.core;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.chats.ChatLogger;
import com.example.liammc.yarn.interfaces.ReadyListener;
import com.example.liammc.yarn.utility.DateTools;
import com.example.liammc.yarn.yarnPlace.YarnPlace;


public class ChatActivity extends YarnActivity {
    /*The Chat Activity is used when the user clicks on a chat from the Info Window or in the
    Event window. Its a way of displaying Chat information, seeing who has joined and starting the chat
     */

    private final String TAG = "ChatActivity";
    private Chat currentChat;
    private YarnUser otherUser;
    private YarnPlace currentYarnPlace;

    //UI
    private View personInfo;
    private TextView personNameTextView;
    private TextView ageTextView;
    private TextView genderTextview;
    private ImageView personImageView;
    private RatingBar ratingBar;

    private TextView placeTitleTextView;
    private ImageView placeImageView;
    private TextView placeAddressTextView;

    private TextView dateTextView;
    private TextView timeTextView;
    private TextView lengthTextView;

    private TextView messageTextView;
    private Button startButton;
    private ViewGroup buttonBar;

    //TODO fix formatting of meeting time
    //TODO Add a Yarn Wave

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initCurrentChat();

        if(currentChat == null){
            Log.e(TAG,"Current chat is null!");
        }else{
            initYarnPlace();
            initUI();
        }
    }

    //region Init

    private void initUI(){
        /*Initializes UI references and states*/

        personInfo =  findViewById(R.id.personInfo);
        personNameTextView =  personInfo.findViewById(R.id.name);
        ageTextView = personInfo.findViewById(R.id.ageValue);
        genderTextview =  personInfo.findViewById(R.id.genderValue);
        personImageView =  personInfo.findViewById(R.id.profilePicture);
        ratingBar = personInfo.findViewById(R.id.ratingBar);

        placeTitleTextView = findViewById(R.id.placeTitle);
        placeImageView =  findViewById(R.id.placeImage);
        placeAddressTextView =  findViewById(R.id.placeAddress);

        dateTextView =  findViewById(R.id.date);
        timeTextView =  findViewById(R.id.time);
        lengthTextView = findViewById(R.id.length);

        messageTextView =  findViewById(R.id.message);
        startButton =  findViewById(R.id.startButton);
        buttonBar =  findViewById(R.id.buttonBar);

        initPlaceInfo();
        initChatInfo();
        update();

        if(currentChat.chatActive) startChat();
    }

    private void initCurrentChat(){
        /*Initializes the current chat*/

        String currentChatID = getIntent().getStringExtra("chatID");

        if(currentChatID == null || currentChatID.equals("")){
            Log.e(TAG,"The current Chat ID is null!");
            return;
        }

        currentChat = Recorder.getInstance().getRecordedChat(currentChatID);
        currentChat.updator.initChangeListener(this);
    }

    private void initYarnPlace(){
        /*initializes the current Yarn Place*/

        currentYarnPlace =  currentChat.yarnPlace;
    }

    public boolean initPlaceInfo(){
        /*Initializes the place info on the Chat Activity UI*/

        if(currentYarnPlace == null){
            Log.e(TAG,"Yarn place is null");
            return false;
        }

        placeTitleTextView.setText(currentYarnPlace.placeMap.get("name"));
        placeImageView.setImageBitmap(currentYarnPlace.placePhoto);
        placeAddressTextView.setText(currentYarnPlace.address.getAddressLine(0));

        return true;
    }

    public boolean initChatInfo(){
        /*Initializes the Chat Info on the Chat Activity UI*/

        if(currentChat == null){
            return false;
        }

        dateTextView.setText(currentChat.chatDate);
        timeTextView.setText(currentChat.chatTime);
        lengthTextView.setText(currentChat.chatLength);

        return true;
    }

    //endregion

    //region Public Methods

    public void update(){
        /*This method updates all of the UI for the Chat Activity. It should be run when the state
        of the Chat changes
         */

        updateOtherUser();
        updateMessageText();
        updateButtonBar();
        updateStartButton();
    }

    public void updateOtherUser(){
        /*Updates the UI related to the other joined user*/

        localUser = LocalUser.getInstance();

        //Set the other user info
        if(!currentChat.hostUser.userID.equals(localUser.userID)) otherUser = currentChat.hostUser;
        else otherUser = currentChat.guestUser;

        if(!currentChat.chatActive) personInfo.setVisibility(View.GONE);

        if(otherUser != null ){
            otherUser.setReadyListener(new ReadyListener() {
                @Override
                public void onReady() {
                    //The other user is ready and he Chat is active so show the info and start the chat
                    if(currentChat.chatActive){
                        personInfo.setVisibility(View.VISIBLE);

                        personNameTextView.setText(otherUser.userName);
                        ageTextView.setText(String.valueOf(otherUser.age));
                        genderTextview.setText(otherUser.gender);

                        personImageView.setImageBitmap(otherUser.profilePicture);
                        setRatingBar();
                        startChat();
                    }
                }
            });
        }

    }

    public void updateMessageText(){
        /*Updates the UI related to the message text*/

        //There is no other user
        if(otherUser == null){
            messageTextView.setVisibility(View.VISIBLE);
            messageTextView.setText("There is no one to chat with");
        }
        //There is another user
        else{
            //The chat is active so disable the message
            if(currentChat.chatActive){
                messageTextView.setVisibility(View.GONE);
            }
            //The chat isn't active so show the user a message to alert them that someone is ready
            else{
                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setText("Someone is ready to chat with you! Get to your meeting" +
                        "place and start the chat");
            }
        }
    }

    public boolean updateStartButton(){
        /*Updated the UI related to the Start button*/

        if(currentChat == null){
            Log.e(TAG,"Current Chat is null");
            return false;
        }

        //The other user doesn't exist so disable the start button
        if(otherUser == null) startButton.setVisibility(View.INVISIBLE);
        //The other user does exist so enable the start button
        else startButton.setVisibility(View.VISIBLE);

        return true;
    }

    public boolean updateButtonBar(){
        /*Updates the UI related to the Button  bar*/

        if(currentChat == null){
            Log.e(TAG,"Current Chat is null");
            return false;
        }

        //The current chat is active so disable the button bar
        if(currentChat.chatActive) buttonBar.setVisibility(View.INVISIBLE);
        //The current chat is active so enable the button bar
        else buttonBar.setVisibility(View.VISIBLE);

        return true;
    }

    //endregion

    //region Button Methods

    public void onCancelChatPressed(View v){
        /*This method is run when the user clicks the cancel button*/

        currentChat.removeChat();

        Intent intent =  new Intent(this,MapsActivity.class);
        startActivity(intent);
    }

    public void onStartChatPressed(View v){
        /*This method runs when the user clicks the start button*/
        currentChat.activateChat();
    }

    //endregion

    //region Private Methods

    private void setRatingBar(){
        /*This method sets all the starts to match the other user's meanRating*/

        ratingBar.setMax(5);
        ratingBar.setNumStars((int)localUser.meanRating);
    }

    private void startChat(){

        //TODO change debug length upon release

        long millisLength = DateTools.HHmmssStringToMillis(currentChat.chatLength);
        long debugLength = 1000;

        final  Activity activity =  this;

        new CountDownTimer(debugLength, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                Log.d(TAG,"The chat is finished");
                ChatLogger.getInstance().logChat(otherUser,currentChat);

                Intent intent =  new Intent(activity, RateActivity.class);
                intent.putExtra("chatID",currentChat.chatID);
                activity.startActivity(intent);
            }
        }.start();
    }
    //endregion
}
