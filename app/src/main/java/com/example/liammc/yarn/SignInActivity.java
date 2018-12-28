package com.example.liammc.yarn;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    String TAG = "SignInActivity";
    int GO_SIGN_IN = 1;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

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
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mAuthenticator = new Authenticator(this,mAuth,mCurrentUser);
        mFacebookAuth = new FacebookAuth(this,mAuth, mCurrentUser);
        mGoogleAuth = new GoogleAuth(this,SignInActivity.this,mAuth,mCurrentUser,GO_SIGN_IN);
        mTwitterAuth = new TwitterAuth(this,mAuth,mCurrentUser);
        mPhoneAuth = new PhoneAuth(this,mAuth,mCurrentUser);

        mPhoneAuthWindow = new PhoneAuthWindow(this);

        mPhoneAuth.window = mPhoneAuthWindow;
        mPhoneAuthWindow.auth = mPhoneAuth;

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
            /*
            else if(requestCode == PH_SIGN_IN)
            {
                String id = data.getStringExtra("verificationID");
                String code = data.getStringExtra("code");

                //mPhoneAuth.signUp(code);
            }*/
        }

        mFacebookAuth.mCallbackManager.onActivityResult(requestCode, resultCode, data);
        mTwitterAuth.mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }


    //region Button Methods

    public void onSignInEmailButtonPressed(View view)
    {
        EditText emailEditText = findViewById(R.id.emailInput);
        EditText passwordEditText = findViewById(R.id.passwordInput);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuthenticator.login(email,password);
    }

    public void onSignInFacebookButtonPressed(View view)
    {
        mFacebookAuth.login();
    }

    public void onSignInGoogleButtonPressed(View view)
    {
        mGoogleAuth.login();
    }

    public void onSignInTwitterButtonPressed(View view)
    {
        mTwitterAuth.login();
    }

    public void onSignInPhoneButtonPressed(View view)
    {

    }

    //endregion


}
