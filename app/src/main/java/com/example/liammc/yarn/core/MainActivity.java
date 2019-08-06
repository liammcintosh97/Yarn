package com.example.liammc.yarn.core;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.authentication.Authenticator;
import com.example.liammc.yarn.authentication.SignInActivity;
import com.example.liammc.yarn.authentication.SignUpActivity;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.utility.PermissionTools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.core.Twitter;

public class MainActivity extends YarnActivity {
    /*This is the main activity for the application and is the first things that is run when the firebaseUser
    opens the app. It introduces the firebaseUser to a log in and sign in button
     */

    private static final String CHANNEL_ID = "mainActivity";
    private static final int PERMISSION_REQUEST_CODE =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*Runs when the activity is created*/
        super.onCreate(savedInstanceState);

        Twitter.initialize(this);
        setContentView(R.layout.activity_main);
        PermissionTools.requestPermissions(this, PERMISSION_REQUEST_CODE);

        //debugSignIn();

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

        FirebaseUser user = userAuth.getCurrentUser();

        return user!= null;
    }

    private void debugSignIn(){

        String email = "challenger@live.com";
        String password = "123456";

        final Activity act = this;

        userAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        /*Runs when the sign in is complete*/

                        if (task.isSuccessful()) {
                            //Log in was successful so go to the Map
                            Log.d("MainActivity", "signInWithEmail:success");

                            GoToInitialization();
                        } else {
                            //Log in failed so notify the firebaseUser
                            Log.w("MainActivity", "signInWithEmail:failure", task.getException());
                            Toast.makeText(act, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //endregion

}
