package com.example.liammc.yarn;

import android.app.Activity;
import android.support.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.concurrent.Executor;

public class FacebookAuth extends com.example.liammc.yarn.Authenticator
{
    //FaceBook
    public CallbackManager mCallbackManager;
    public LoginManager mLoginManager;

    //Constructor
    public FacebookAuth(Activity _callingActivity, FirebaseAuth _mAuth, FirebaseUser _currentUser)
    {
        super(_callingActivity,_mAuth, _currentUser);
        this.SetUpFaceBookAuth();
    }

    public void login()
    {
        mLoginManager.logInWithReadPermissions(callingActivity,
                Arrays.asList("public_profile", "user_friends"));
    }

    private void SetUpFaceBookAuth()
    {
        //FacebookSdk.sdkInitialize(this.getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();

        mLoginManager.registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(CALLINGTAG, "Facebook Login is successful");
                        firebaseAuthFacebook(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(callingActivity, "Facebook Login Cancel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(callingActivity, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void firebaseAuthFacebook(AccessToken token)
    {
        Log.d(CALLINGTAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(callingActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(CALLINGTAG, "signInWithCredential:success");
                            mCurrentUser = mAuth.getCurrentUser();
                            goToAccountSetup();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(CALLINGTAG, "signInWithCredential:failure", task.getException());

                            Toast.makeText(callingActivity, "Authentication failed ",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

}
