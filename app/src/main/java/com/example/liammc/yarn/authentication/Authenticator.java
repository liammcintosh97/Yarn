package com.example.liammc.yarn.authentication;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.liammc.yarn.accounting.TermsActivity;
import com.example.liammc.yarn.core.InitializationActivity;
import com.example.liammc.yarn.interfaces.AuthListener;
import com.google.firebase.auth.AuthCredential;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Authenticator {
    /*The Authenticator is used to log in the firebaseUser and sign them up through Firebase*/

    //region Auth Listener
    /*This listener is used to alert the application when an authentication opteration has
     * completed*/

    protected AuthListener authListener;

    public AuthListener getAuthListener() {
        return authListener;
    }

    public void setAuthListener(AuthListener _authListener) {
        this.authListener = _authListener;
    }
    //endregion

    final FirebaseAuth mAuth;
    final String TAG = "Authenticator";

    //Constructor
    public Authenticator(FirebaseAuth _mAuth) { this.mAuth = _mAuth; }

    //region Public Methods
    public void signUp(final Activity activity, String email, String password) {
        /*This method signs up the firebaseUser to Firebase with an email and password*/

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        /*Runs when the task is complete*/

                        if (task.isSuccessful()) {
                           /*Sign up is successful so go to account set up*/
                            Log.d(TAG, "createUserWithEmail:success");
                            goToTermsAcceptance(activity);
                        } else {
                            //Sign up failed so notify the firebaseUser
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());

                            Toast.makeText(activity, "Authentication failed - "
                                            + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void login(final Activity activity, String email, String password, final AuthListener listener) {
        /*This method logs in the firebaseUser to Firebase with an email and password*/

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        /*Runs when the sign in is complete*/

                        if (task.isSuccessful()) {
                            //Log in was successful so go to the Map
                            Log.d(TAG, "signInWithEmail:success");

                            //goToInitialization(activity);
                            listener.onAuth();
                        } else {
                            //Log in failed so notify the firebaseUser
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(activity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            listener.onError("Authentication failed please try again");
                        }
                    }
                });
    }

    //endregion

    //region Protected Methods

    protected void externalAuth(final Activity activity,AuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, updateInfoWindow UI with the signed-in firebaseUser's information
                            Log.d(TAG, "authenticator:success");

                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            if(isNew) goToTermsAcceptance(activity);
                            else goToInitialization(activity);

                        } else {
                            // If sign in fails, display a message to the firebaseUser.
                            Log.w(TAG, "authenticator:failure", task.getException());

                            Toast.makeText(activity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    protected void goToTermsAcceptance(Activity activity) {
        //Takes the firebaseUser to the Account Set Up activity

        Intent myIntent = new Intent(activity.getBaseContext(),   TermsActivity.class);
        activity.startActivity(myIntent);
    }

    protected  void goToInitialization(Activity activity){
        //Takes the firebaseUser to the Initialization activity

        Intent myIntent = new Intent(activity.getBaseContext(), InitializationActivity.class);
        activity.startActivity(myIntent);
    }

    //endregion
}
