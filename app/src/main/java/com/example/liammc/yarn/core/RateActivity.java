package com.example.liammc.yarn.core;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.authentication.SocialPoster;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.yarnSupport.ReporterWindow;

public class RateActivity extends YarnActivity {

    private final String TAG =  "RateActivity";
    private final int FB_POST_REQUEST_CODE = 1;
    private final int TW_POST_REQUEST_CODE = 2;
    private YarnUser otherUser;
    private Chat chat;
    private SocialPoster socialPoster;

    //UI
    private ReporterWindow reporterWindow;
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

    @Override
    protected void onStop(){
        super.onStop();

        onSubmitRatingPressed(null);
    }

    @Override
    public void onBackPressed() {
        if(reporterWindow.isShowing()){
            reporterWindow.dismiss();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode != RESULT_OK) return;

        if (requestCode == ReporterWindow.REPORT_REQUEST_CODE) {
            reporterWindow.dismiss();
        }
    }

    //region Init

    private void init(){
        chat =  recorder.getRecordedChat(getIntent().getStringExtra("chatID"));
        otherUser =  chat.getOtherUser();

        reporterWindow =  new ReporterWindow(this,(ViewGroup)findViewById(R.id.mainLayout)
                ,otherUser,0.75,0.75);

        socialPoster =  new SocialPoster(this);
    }

    private void initUI(){

        ratingBar = findViewById(R.id.ratingBar);
        profilePicture = findViewById(R.id.profilePicture);
        userName = findViewById(R.id.userName);

        ratingBar.setMax(5);
        ratingBar.setNumStars(5);
        ratingBar.setRating(5);
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

        finishChat();
    }

    public void onCancelPressed(View v){
        finishChat();
    }

    public void onReportUserPressed(View v){
        reporterWindow.show(Gravity.CENTER);
    }

    public void onPostFacebookPressed(View v){
        socialPoster.postFB(null);
    }

    public void onPostTwitterPressed(View v){socialPoster.postTW(TW_POST_REQUEST_CODE);}

    //endregion

    //region Private Methods

    public void finishChat(){
        if(chat != null) chat.removeChat();

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);

    }

    //endregion
}
