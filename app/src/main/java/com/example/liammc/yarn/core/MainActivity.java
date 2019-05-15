package com.example.liammc.yarn.core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.authentication.SignInActivity;
import com.example.liammc.yarn.authentication.SignUpActivity;
import com.example.liammc.yarn.utility.PermissionTools;
import com.google.firebase.auth.FirebaseAuth;
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
        createNotificationChannel();
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

    private void createNotificationChannel() {
        /*This Method creates a notification Channel but only if the device's API is 26+*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "notifier";
            String description = "notifies the firebaseUser about app activities";

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private boolean isSignedIn() {
        /*Returns whether the firebaseUser is signed in or not*/

        return mAuth.getCurrentUser() != null;
    }

    //endregion

}
