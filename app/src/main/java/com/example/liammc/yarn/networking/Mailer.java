package com.example.liammc.yarn.networking;

import android.app.Activity;
import android.content.Intent;

public class Mailer {

    private final String TAG = "Mailer";

    private final String to = "liam.ed.mc97@gmail.com";
    private String subject;
    private Intent emailIntent;

    public Mailer(String _subject){
        this.subject = _subject;
    }

    //region Public Methods

    public void send(Activity activity, String message, int requestCode){
        setEmailIntent(message);

        activity.startActivityForResult(Intent.createChooser(emailIntent, "Select Email Sending App :")
        ,requestCode);
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
