package com.example.liammc.yarn.core;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.authentication.SignInActivity;
import com.example.liammc.yarn.authentication.SignUpActivity;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.utility.PermissionTools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.core.Twitter;

public class MainActivity extends AppCompatActivity {
    /*Thi is the main activity for the application and is the first things that is run when the firebaseUser
    opens the app. It introduces the firebaseUser to a log in and sign in button
     */

    private static final String CHANNEL_ID = "mainActivity";
    private static final int PERMISSION_REQUEST_CODE =1;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*Runs when the activity is created*/

        Twitter.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        Notifier.getInstance().createNotificationChannel(this);
        PermissionTools.requestPermissions(this, PERMISSION_REQUEST_CODE);

        //Got to Initialization if the firebaseUser is signed in
        if(isSignedIn()) GoToInitialization();
    }


    //region Buttons Methods

    public void OnSignInPressed(View view) {
        /*Runs when the firebaseUser presses the sign In button. It takes the firebaseUser to the Sign In Activity*/
        Intent myIntent = new Intent(getBaseContext(),   SignInActivity.class);
        startActivity(myIntent);
    }

    public void OnSignUpPressed(View view) {
        /*Runs when the firebaseUser presses the sign In button. It takes the firebaseUser to the Sign Up Activity*/
        Intent myIntent = new Intent(getBaseContext(),   SignUpActivity.class);
        startActivity(myIntent);
    }
    //endregion

    //region Public Methods

    public void GoToInitialization(){
        /*This method takes the firebaseUser to the initialization activity*/

        Intent myIntent = new Intent(getBaseContext(),   InitializationActivity.class);
        startActivity(myIntent);
    }

    //endregion

    //region Private Methods

    private boolean isSignedIn() {
        /*Returns whether the firebaseUser is signed in or not*/

        FirebaseUser user = mAuth.getCurrentUser();

        return user!= null;
    }

    //endregion

}
