package com.example.liammc.yarn;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class Authenticator
{
    Activity callingActivity;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;
    String CALLINGTAG = "";

    //Constructor
    public Authenticator(Activity _callingActivity, FirebaseAuth _mAuth, FirebaseUser _mCurrentUser)
    {
        this.callingActivity = _callingActivity;
        this.CALLINGTAG = _callingActivity.getLocalClassName();

        this.mAuth = _mAuth;
        this.mCurrentUser = _mCurrentUser;
    }

    public void signUp(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(callingActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(CALLINGTAG, "createUserWithEmail:success");
                            mCurrentUser = mAuth.getCurrentUser();
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

    public void login(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(callingActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(CALLINGTAG, "signInWithEmail:success");
                            mCurrentUser = mAuth.getCurrentUser();

                            goToAccountSetup();
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
        Intent myIntent = new Intent(callingActivity.getBaseContext(),   IntroActivity.class);
        callingActivity.startActivity(myIntent);
    }
    //endregion
}
