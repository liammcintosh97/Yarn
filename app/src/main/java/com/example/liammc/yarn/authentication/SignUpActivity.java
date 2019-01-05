package com.example.liammc.yarn.authentication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.liammc.yarn.utility.CompatabiltyTools;
import com.example.liammc.yarn.utility.ErrorManager;
import com.example.liammc.yarn.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {

    private final int GO_SIGN_IN = 0;
    final int PH_SIGN_IN = 1;

    //Firebase
    FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    //Authentication
    private Authenticator mAuthenticator;
    private FacebookAuth mFacebookAuth;
    private GoogleAuth mGoogleAuth;
    private TwitterAuth mTwitterAuth;
    private PhoneAuth mPhoneAuth;

    private PhoneAuthWindow mPhoneAuthWindow;
    EditText passwordInput;
    EditText confirmPasswordInput;
    EditText emailInput;
    EditText phoneNumberInput;
    EditText codeInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mAuthenticator = new Authenticator(this,mAuth,mCurrentUser);
        mFacebookAuth = new FacebookAuth(this,mAuth, mCurrentUser);
        mGoogleAuth = new GoogleAuth(this,SignUpActivity.this,mAuth,
                mCurrentUser,GO_SIGN_IN);
        mTwitterAuth = new TwitterAuth(this,mAuth,mCurrentUser);
        mPhoneAuth = new PhoneAuth(this,mAuth,mCurrentUser);

        ViewGroup parentViewGroup = findViewById(R.id.mainConstraintLayout);

        mPhoneAuthWindow = new PhoneAuthWindow(this, parentViewGroup);

        mPhoneAuth.window = mPhoneAuthWindow;
        mPhoneAuthWindow.auth = mPhoneAuth;

        checkSignIn();

        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.emailInput);
        emailInput = findViewById(R.id.confirmPasswordInput);
        phoneNumberInput = mPhoneAuthWindow.mPhoneAuthView.findViewById(R.id.phoneNumberInput);
        codeInput = mPhoneAuthWindow.mPhoneAuthView.findViewById(R.id.phoneCodeInput);

        CompatabiltyTools.setPasswordAutofill(passwordInput);
        CompatabiltyTools.setEmailAutofill(emailInput);
        CompatabiltyTools.setPasswordAutofill(confirmPasswordInput);
        CompatabiltyTools.setNumberAutoFill(phoneNumberInput);
        CompatabiltyTools.setNumberAutoFill(codeInput);
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
                //String id = data.getStringExtra("verificationID");
                String code = data.getStringExtra("code");

                mPhoneAuth.signIn(code);
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
            ErrorManager.validateEmail(email);
            ErrorManager.validatePassword(password,confirmPassword);

            mAuthenticator.signUp(email,password);

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
