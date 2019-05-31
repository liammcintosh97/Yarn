package com.example.liammc.yarn.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {
    /*The AuthActivity class is the parent class for activities that sign in and up users. It
    * handles the initialization of Authenticators, the Firebase Auth and User and shared UI
    * elements. Activities that are used to sign in or up users must extend this one*/

    private static String TAG = "AuthActivity";

    //Sign in result codes
    public static final int GO_SIGN_IN = 1;
    public static int PH_SIGN_IN = 2;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    //Authentication
    protected Authenticator mAuthenticator;
    protected FacebookAuth mFacebookAuth;
    protected GoogleAuth mGoogleAuth;
    protected TwitterAuth mTwitterAuth;
    protected PhoneAuth mPhoneAuth;

    //UI
    protected ViewGroup parentViewGroup;
    protected PhoneAuthWindow mPhoneAuthWindow;
    protected EditText passwordInput;
    protected EditText emailInput;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*Runs when an activity returns a result to this activity*/
        super.onActivityResult(requestCode, resultCode, data);

        //The result is ok
        if(resultCode == RESULT_OK) {

            if (requestCode == GO_SIGN_IN) {
                /*The result is a google sign in so handle it's result*/
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                mGoogleAuth.handleResult(this,task);
            }
            else if(requestCode == PH_SIGN_IN) {
                /*The result is a phone sign in so handle it's result*/
                String code = data.getStringExtra("code");

                mPhoneAuth.signIn(this,code);
            }
        }

        //Process the FaceBook and Twitter activity results
        mFacebookAuth.mCallbackManager.onActivityResult(requestCode, resultCode, data);
        mTwitterAuth.mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }

    //region init
    protected void initAuthenticators(){
        /*Initializes the authenticators*/

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mAuthenticator = new Authenticator(mAuth);
        mFacebookAuth = new FacebookAuth(this,mAuth);
        mGoogleAuth = new GoogleAuth(this,mAuth);
        mTwitterAuth = new TwitterAuth(this,mAuth);
        mPhoneAuth = new PhoneAuth(this,mAuth);
    }

    protected void initPhoneAuthWindow(){
        /*Initializes the Phone Auth window*/

        parentViewGroup = findViewById(R.id.mainConstraintLayout);

        mPhoneAuthWindow = new PhoneAuthWindow(this,parentViewGroup);
        mPhoneAuth.window = mPhoneAuthWindow;
        mPhoneAuthWindow.auth = mPhoneAuth;
    }

    protected void initUI(){
        /*Initializes UI elements*/

        passwordInput = findViewById(R.id.passwordInput);
        emailInput = findViewById(R.id.emailInput);

        CompatibilityTools.setPasswordAutoFill(passwordInput);
        CompatibilityTools.setEmailAutoFill(emailInput);
    }
    //endregion

    //region Protected Methods

    protected void onSignInFacebookButtonPressed(View view) {
        mFacebookAuth.login(this);
    }

    protected void onSignInGoogleButtonPressed(View view) {
        mGoogleAuth.login(this);
    }

    protected void onSignInTwitterButtonPressed(View view) {
        mTwitterAuth.login(this);
    }

    protected void onSignInPhoneButtonPressed(View view) {
        mPhoneAuthWindow.ShowAuth();
    }

    //endregion

}
