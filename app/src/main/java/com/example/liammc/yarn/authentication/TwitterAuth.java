package com.example.liammc.yarn.authentication;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.liammc.yarn.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

class TwitterAuth extends Authenticator {
    private final String TAG = "TwitterAuth";

    TwitterAuthClient mTwitterAuthClient;

    //Constructor
    TwitterAuth(Context _context,FirebaseAuth _mAuth) {
        super(_mAuth);
        initTwitter(_context);
        this.mTwitterAuthClient = new TwitterAuthClient();
    }

    //region Init

    private void initTwitter(Context context){

        TwitterConfig config = new TwitterConfig.Builder(context)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(
                        context.getString(R.string.com_twitter_sdk_android_CONSUMER_KEY),
                        context.getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    //endregion

    //region Package Private Methods

    void login(final Activity activity) {
        /*Takes the firebaseUser into Twitter login*/

        mTwitterAuthClient.authorize(activity, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                /*The twitter login was successful*/
                Log.d(TAG, "twitterLogin:success" + twitterSessionResult);
                handleSignInTwitterResult(activity,twitterSessionResult.data);
            }

            @Override
            public void failure(TwitterException e) {
                /*The twitter login was a failure*/
                Log.w(TAG, "twitterLogin:failure", e);
                Toast.makeText(activity, "Twitter sign in Failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    //endregion

    //region Private Methods
    private void handleSignInTwitterResult(Activity activity,TwitterSession session) {
        //Handles the twitter sign in

        Log.d(TAG, "handleTwitterSession:" + session);

        //Get the Twitter credential
        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        externalAuth(activity,credential);
    }
    //endregion
}
