package com.example.liammc.yarn.core;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.IntroActivity;
import com.example.liammc.yarn.authentication.SignInActivity;
import com.example.liammc.yarn.authentication.SignUpActivity;
import com.example.liammc.yarn.networking.InternetListener;
import com.example.liammc.yarn.utility.PermissionTools;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.core.Twitter;

public class MainActivity extends AppCompatActivity {
    /*This is the main activity for the application and is the first things that is run when the firebaseUser
    opens the app. It introduces the firebaseUser to a log in and sign in button
     */

    private final String TAG = "Main Activity";
    private static final String CHANNEL_ID = "mainActivity";
    private static final int PERMISSION_REQUEST_CODE =1;
    private SharedPreferences prefs = null;
    private boolean connected =  false;
    private InternetListener internetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*Runs when the activity is created*/
        super.onCreate(savedInstanceState);

        Twitter.initialize(this);
        setContentView(R.layout.activity_main);
        PermissionTools.requestPermissions(this, PERMISSION_REQUEST_CODE);

        internetListener =  new InternetListener(this);

        prefs = getSharedPreferences("com.example.liammc.yarn", MODE_PRIVATE);

        //Got to Initialization if the firebaseUser is signed in
        if(isSignedIn()) GoToInitialization();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit().putBoolean("firstrun", false).apply();
            goToIntroduction();
        }
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

    public void goToIntroduction(){
        Intent intent = new Intent(getBaseContext(), IntroActivity.class);
        startActivity(intent);
    }

    //endregion

    //region Private Methods


    private boolean isSignedIn() {
        /*Returns whether the firebaseUser is signed in or not*/

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        return user!= null;
    }

    private void debugSignIn(){

        String email = "challenger@live.com";
        String password = "123456";
        FirebaseAuth userAuth = FirebaseAuth.getInstance();

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
