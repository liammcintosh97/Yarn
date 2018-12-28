package com.example.liammc.yarn;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.hbb20.CountryCodePicker;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity {

    private int GO_SIGN_IN = 0;
    private int PH_SIGN_IN = 1;

    private String TAG = "SignUpActivity";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    //Authentication
    private Authenticator mAuthenticator;
    private FacebookAuth mFacebookAuth;
    private GoogleAuth mGoogleAuth;
    private TwitterAuth mTwitterAuth;
    private PhoneAuth mPhoneAuth;

    private PhoneAuthWindow mPhoneAuthWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mAuthenticator = new Authenticator(this,mAuth,mCurrentUser);
        mFacebookAuth = new FacebookAuth(this,mAuth, mCurrentUser);
        mGoogleAuth = new GoogleAuth(this,SignUpActivity.this,mAuth,mCurrentUser,GO_SIGN_IN);
        mTwitterAuth = new TwitterAuth(this,mAuth,mCurrentUser);
        mPhoneAuth = new PhoneAuth(this,mAuth,mCurrentUser);

        mPhoneAuthWindow = new PhoneAuthWindow(this);

        mPhoneAuth.window = mPhoneAuthWindow;
        mPhoneAuthWindow.auth = mPhoneAuth;

        checkSignIn();
    }

    @Override
    public void onBackPressed()
    {
        if(PhoneAuthWindow.window.isShowing())
        {
            PhoneAuthWindow.window.dismiss();
        }
        else super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            if (requestCode == GO_SIGN_IN) {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                mGoogleAuth.handleSignInGoogleResult(task);
            }
            else if(requestCode == PH_SIGN_IN)
            {
                String id = data.getStringExtra("verificationID");
                String code = data.getStringExtra("code");

                mPhoneAuth.signUp(code);
            }
        }

        mFacebookAuth.mCallbackManager.onActivityResult(requestCode, resultCode, data);
        mTwitterAuth.mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }

    //region Button Methods
    public void OnSignUpPressed(View view)
    {
        //Get User Input
        EditText emailEditText = findViewById(R.id.emailInput);
        EditText passwordEditText = findViewById(R.id.passwordInput);
        EditText confirmPasswordEditText = findViewById(R.id.confirmPasswordInput);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        try
        {
            if(ErrorManager.validateEmail(email)
                    && ErrorManager.validatePassword(password,confirmPassword))
            {
                mAuthenticator.signUp(email,password);
            }
        }
        catch(IOException e)
        {
            Toast.makeText(SignUpActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void OnSignUpFacebookPressed(View view)
    {
        mFacebookAuth.login();
    }

    public void OnSignUpGooglePressed(View view)
    {
        mGoogleAuth.login();
    }

    public void OnSignUpTwitterPressed(View view)
    {
        mTwitterAuth.login();
    }

    public void OnSignUpPhonePressed(View view)
    {
       mPhoneAuthWindow.ShowPhoneAuth();
    }
    //endregion

    //region Utility
    private void checkSignIn()
    {
        mPhoneAuthWindow.dissmissPhoneAuth();

        if(mCurrentUser != null)
        {
            mAuthenticator.goToAccountSetup();
        }

    }
    //endregion

}
