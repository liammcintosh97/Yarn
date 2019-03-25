package com.example.liammc.yarn.core;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.liammc.yarn.InitializationActivity;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.authentication.SignInActivity;
import com.example.liammc.yarn.authentication.SignUpActivity;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.utility.PermissionTools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.core.Twitter;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "mainActivity";
    private static final int PERMISSION_REQUEST_CODE =1;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Twitter.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        createNotificationChannel();

        PermissionTools.requestPermissions(this, PERMISSION_REQUEST_CODE);

        if(isSignedIn()) GoToInitialization();
    }


    public void OnSignInPressed(View view)
    {
        Intent myIntent = new Intent(getBaseContext(),   SignInActivity.class);
        startActivity(myIntent);
    }

    public void OnSignUpPressed(View view)
    {
        Intent myIntent = new Intent(getBaseContext(),   SignUpActivity.class);
        startActivity(myIntent);
    }

    public void GoToInitialization(){
        Intent myIntent = new Intent(getBaseContext(),   InitializationActivity.class);
        startActivity(myIntent);
    }

    //region Utility

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "notifier";
            String description = "notifies the user about app activities";

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private boolean isSignedIn()
    {
        return mAuth.getCurrentUser() != null;
    }

    //endregion

}
