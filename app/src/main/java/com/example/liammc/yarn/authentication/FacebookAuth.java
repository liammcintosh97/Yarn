package com.example.liammc.yarn.authentication;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

class FacebookAuth extends Authenticator {
    /*This class is used for logging users into Firebase through Facebook. If they do this for the
    first time a new Firebase account is created. Every other login after that is to that previously
    made Firebase Account unless its removed
     */

    private final String TAG = "FacebookAuth";

    //FaceBook
    CallbackManager mCallbackManager;
    LoginManager mLoginManager;

    //Constructor
    FacebookAuth(Activity _activity,FirebaseAuth _mAuth) {
        super(_mAuth);
        this.init(_activity);
    }

    //region init

    private void init(final Activity activity) {
        /*Initializes the Facebook systems for logging in*/

        mCallbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();

        /*Set the callback to process what happens once the firebaseUser returns from the login
        process*/
        mLoginManager.registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        /*The Facebook login was successful so now use that result to connect to
                        Firebase*/

                        Log.d(TAG, "Facebook Login is successful");

                        //Get the access token and credential
                        AccessToken token = loginResult.getAccessToken();
                        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

                        externalAuth(activity, credential);
                    }

                    @Override
                    public void onCancel() {
                        /*The firebaseUser cancelled the login so alert them*/
                        Toast.makeText(activity, "Facebook Login Cancel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        /*There was an error trying to login through Facebook so alert the firebaseUser*/
                        Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }
    //endregion

    //region Public Methods
    public void login(Activity activity) {
        /*Logs the firebaseUser in through facebook*/

        mLoginManager.logInWithReadPermissions(activity,
                Arrays.asList("public_profile", "user_friends"));
    }
    //endregion

    //region Private Methods
    private void firebaseAuthFacebook(final Activity activity,AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        //Get the credential from the access token
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, updateInfoWindow UI with the signed-in firebaseUser's information
                            Log.d(TAG, "signInWithCredential:success");

                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            if(isNew) goToTermsAcceptance(activity);
                            else goToInitialization(activity);


                        } else {
                            // If sign in fails, display a message to the firebaseUser.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            Toast.makeText(activity, "Authentication failed ",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    //endregion Private Methods
}
