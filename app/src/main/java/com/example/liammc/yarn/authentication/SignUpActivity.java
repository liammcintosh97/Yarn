package com.example.liammc.yarn.authentication;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.liammc.yarn.utility.CompatibilityTools;
import com.example.liammc.yarn.utility.ErrorManager;
import com.example.liammc.yarn.R;

import java.io.IOException;

public class SignUpActivity extends AuthActivity {
    /*This class is used by the firebaseUser when they sign up to firebase*/

    //UI
    EditText confirmPasswordInput;
    EditText phoneNumberInput;
    EditText codeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initAuthenticators();
        initPhoneAuthWindow();
        initUI();

        checkSignIn();
        initSignUpUI();
    }

    @Override
    public void onBackPressed() {
        /*Runs when the firebaseUser presses the back button on their device*/

        if(PhoneAuthWindow.window.isShowing()) {
            /*If the phone auth window is showing dismiss it*/
            PhoneAuthWindow.window.dismiss();
        }
        else super.onBackPressed();
    }

    //region Init

    private void initSignUpUI(){
        /*Initializes the sign up UI*/

        confirmPasswordInput = findViewById(R.id.emailInput);
        phoneNumberInput = mPhoneAuthWindow.mPhoneAuthView.findViewById(R.id.phoneNumberInput);
        codeInput = mPhoneAuthWindow.mPhoneAuthView.findViewById(R.id.phoneCodeInput);

        CompatibilityTools.setPasswordAutoFill(confirmPasswordInput);
        CompatibilityTools.setNumberAutoFill(phoneNumberInput);
        CompatibilityTools.setNumberAutoFill(codeInput);
    }

    //endregion

    //region Button Methods
    public void OnSignUpPressed(View view) {
        /*Runs when the firebaseUser pressed the Sign Up button*/

        //Get the edit texts
        EditText emailEditText = findViewById(R.id.emailInput);
        EditText passwordEditText = findViewById(R.id.passwordInput);
        EditText confirmPasswordEditText = findViewById(R.id.confirmPasswordInput);

        //Get the User Input strings
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        try {
            //Validate the input
            ErrorManager.validateEmail(email);
            ErrorManager.validatePassword(password,confirmPassword);

            mAuthenticator.signUp(this,email,password);
        }
        catch(IOException e) {
            //An exception has occurred so alert the firebaseUser
            Toast.makeText(SignUpActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
    //endregion

    //region Private Methods
    private void checkSignIn() {
        /*Checks if the User is signed in*/

        //Dismiss the Phone auth
        mPhoneAuthWindow.dismissPhoneAuth();

        if(mCurrentUser != null) {
            /*The current firebaseUser isn't null so they are logged in. Take the firebaseUser to the account
            Set up */
            mAuthenticator.goToAccountSetup(this);
        }

    }
    //endregion

}
