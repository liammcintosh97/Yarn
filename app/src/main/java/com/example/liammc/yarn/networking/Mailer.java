package com.example.liammc.yarn.networking;

import android.app.Activity;
import android.content.Intent;

import java.util.UUID;

public class Mailer {

    private final String TAG = "Mailer";

    private final String to = "liam.ed.mc97@gmail.com";
    private String subject;
    private String message;
    private Intent emailIntent;

    public Mailer(){
        this.initSubject();
    }

    //region Init

    private void initSubject(){
        subject = "Yarn Support Case " + UUID.randomUUID().toString();
    }


    //endregion

    //region Public Methods

    public void send(Activity activity, String message){
        setEmailIntent(message);

        activity.startActivity(Intent.createChooser(emailIntent, "Select Email Sending App :"));
    }

    //endregion

    //region Public Methods

    public void setEmailIntent(String message){
        emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        emailIntent.setType("message/rfc822");
    }

    //endregion

}
