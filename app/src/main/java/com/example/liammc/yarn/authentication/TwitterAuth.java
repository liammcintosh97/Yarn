package com.example.liammc.yarn.authentication;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

class TwitterAuth extends Authenticator
{

   TwitterAuthClient mTwitterAuthClient;

    //Constructor
    TwitterAuth(Activity _callingActivity, FirebaseAuth _mAuth, FirebaseUser _currentUser)
    {
        super(_callingActivity,_mAuth, _currentUser);
        this.SetUpTwitterAuth();
    }

    void login()
    {
        mTwitterAuthClient.authorize(callingActivity, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                Log.d(CALLINGTAG, "twitterLogin:success" + twitterSessionResult);
                handleSignInTwitterResult(twitterSessionResult.data);
            }

            @Override
            public void failure(TwitterException e) {
                Log.w(CALLINGTAG, "twitterLogin:failure", e);

                Toast.makeText(callingActivity, "Twitter sign in Failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SetUpTwitterAuth()
    {
        mTwitterAuthClient = new TwitterAuthClient();
    }

    private void handleSignInTwitterResult(TwitterSession session)
    {
        Log.d(CALLINGTAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        firebaseAuthTwitter(credential);
    }

    private void firebaseAuthTwitter(AuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(callingActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(CALLINGTAG, "signInWithCredential:success");

                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            if(isNew) goToAccountSetup();
                            else goToMap();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(CALLINGTAG, "signInWithCredential:failure", task.getException());

                            Toast.makeText(callingActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
