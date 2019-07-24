package com.example.liammc.yarn.authentication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.interfaces.AuthListener;
import com.example.liammc.yarn.yarnSupport.PasswordResetter;

public class SignInActivity extends AuthActivity {
    /*This activity is used to sign the firebaseUser into Firebase*/

    PasswordResetter passwordResetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*Runs when the activity is created*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initAuthenticators();
        initPhoneAuthWindow();
        initUI();

        passwordResetter  = new PasswordResetter(this,
                (ViewGroup) findViewById(R.id.mainConstraintLayout));
    }


    //region Button Methods

    public void onSignInEmailButtonPressed(View view) {
        EditText emailEditText = findViewById(R.id.emailInput);
        EditText passwordEditText = findViewById(R.id.passwordInput);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        final Activity a =  this;

        mAuthenticator.login(this, email, password, new AuthListener() {
            @Override
            public void onAuth() {
                mAuthenticator.goToInitialization(a);
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    public void onResetPasswordButton(View view){
        passwordResetter.show();
    }

    //endregion

}
