package com.example.liammc.yarn.core;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.chats.Chat;

public class RateActivity extends AppCompatActivity {

    private final String TAG =  "RateActivity";
    private YarnUser otherUser;
    private Chat chat;
    private Recorder recorder;

    //UI
    private RatingBar ratingBar;
    private ImageView profilePicture;
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        init();
        initUI();
    }

    //region Init

    private void init(){
        recorder =  Recorder.getInstance();
        chat =  recorder.getRecordedChat(getIntent().getStringExtra("chatID"));
        otherUser =  chat.getOtherUser();
    }

    private void initUI(){

        ratingBar = findViewById(R.id.ratingBar);
        profilePicture = findViewById(R.id.profilePicture);
        userName = findViewById(R.id.userName);

        ratingBar.setMax(5);
        profilePicture.setImageBitmap(otherUser.profilePicture);
        userName.setText(otherUser.userName);
    }

    //endregion

    //region Button Methods

    public void onSubmitRatingPressed(View v){

        try {
            otherUser.updator.addUserRating(LocalUser.getInstance().userID,
                    ratingBar.getNumStars());
        }catch (Exception e){
            Log.e(TAG,"Couldn't pass rating");
        }

        if(chat != null) chat.removeChat();

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);

    }

    public void onCancelPressed(View v){
        chat.removeChat();

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void onReportUserPressed(View v){

    }

    //endregion
}
