package com.example.liammc.yarn.authentication;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.liammc.yarn.core.MapsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Authenticator
{
    final Activity callingActivity;
    final FirebaseAuth mAuth;
    final String CALLINGTAG;

    //Constructor
    Authenticator(Activity _callingActivity, FirebaseAuth _mAuth, FirebaseUser _mCurrentUser)
    {
        this.callingActivity = _callingActivity;
        this.CALLINGTAG = _callingActivity.getLocalClassName();

        this.mAuth = _mAuth;
    }

    void signUp(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(callingActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(CALLINGTAG, "createUserWithEmail:success");
                            goToAccountSetup();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(CALLINGTAG, "createUserWithEmail:failure", task.getException());

                            Toast.makeText(callingActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    void login(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(callingActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(CALLINGTAG, "signInWithEmail:success");

                            goToMap();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(CALLINGTAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(callingActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //region Utility
    public void goToAccountSetup()
    {
        Intent myIntent = new Intent(callingActivity.getBaseContext(),   CreateAccountActivity.class);
        callingActivity.startActivity(myIntent);
    }

    public void goToMap()
    {
        Intent myIntent = new Intent(callingActivity.getBaseContext(), MapsActivity.class);
        callingActivity.startActivity(myIntent);
    }

    //endregion
}
