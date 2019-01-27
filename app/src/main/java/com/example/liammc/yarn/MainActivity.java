package com.example.liammc.yarn;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.liammc.yarn.authentication.SignInActivity;
import com.example.liammc.yarn.authentication.SignUpActivity;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.utility.PermissionTools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.core.Twitter;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE =1;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Twitter.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        PermissionTools.requestPermissions(this, PERMISSION_REQUEST_CODE);

        if(isSignedIn()) goToMap();
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

    private void goToMap()
    {
        Intent myIntent = new Intent(getBaseContext(),   MapsActivity.class);
        startActivity(myIntent);
    }


    //region Utility

    private boolean isSignedIn()
    {
        return mAuth.getCurrentUser() != null;
    }

    //endregion

}
