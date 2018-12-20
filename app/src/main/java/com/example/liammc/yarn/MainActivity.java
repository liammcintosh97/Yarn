package com.example.liammc.yarn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.twitter.sdk.android.core.Twitter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Twitter.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void OnLoginPressed(View view)
    {

    }

    public void OnSignUpEmailPressed(View view)
    {
        Intent myIntent = new Intent(getBaseContext(),   SignUpEmailActivity.class);
        startActivity(myIntent);
    }

    public void OnSignUpPhonePressed(View view)
    {
        Intent myIntent = new Intent(getBaseContext(),   SignUpPhoneActivity.class);
        startActivity(myIntent);
    }

    public void OnSignUpGooglePressed(View view)
    {
        Intent myIntent = new Intent(getBaseContext(),   SignUpGoogleActivity.class);
        startActivity(myIntent);
    }

    public void OnSignUpFacebookPressed(View view)
    {
        Intent myIntent = new Intent(getBaseContext(),   SignUpFacebookActivity.class);
        startActivity(myIntent);
    }

    public void OnSignUpTwitterPressed(View view)
    {
        Intent myIntent = new Intent(getBaseContext(),   SignUpTwitterActivity.class);
        startActivity(myIntent);
    }
}
