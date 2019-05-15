package com.example.liammc.yarn.authentication;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.liammc.yarn.R;

public class SignInActivity extends AuthActivity {
    /*This activity is used to sign the firebaseUser into Firebase*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*Runs when the activity is created*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
    }

    //region Button Methods

    public void onSignInEmailButtonPressed(View view) {
        EditText emailEditText = findViewById(R.id.emailInput);
        EditText passwordEditText = findViewById(R.id.passwordInput);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuthenticator.login(this,email,password);
    }

    //endregion

}
