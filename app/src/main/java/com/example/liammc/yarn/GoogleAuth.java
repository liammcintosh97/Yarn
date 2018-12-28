package com.example.liammc.yarn;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleAuth extends com.example.liammc.yarn.Authenticator
{
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleApiClient mGoogleApiClient;
    private int resultCode;
    private FragmentActivity fragAct;

    //Constructor
    public GoogleAuth(Activity _callingActivity, FragmentActivity _fragAct, FirebaseAuth _mAuth, FirebaseUser _currentUser,int _resultCode)
    {
        super(_callingActivity,_mAuth, _currentUser);
        this.resultCode = _resultCode;
        this.fragAct = _fragAct;
        this.SetUpGoogleAuth();
    }

    public void login()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        callingActivity.startActivityForResult(signInIntent, resultCode);
    }

    private void SetUpGoogleAuth()
    {
        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(callingActivity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(callingActivity, gso);

        mGoogleApiClient = new GoogleApiClient.Builder(callingActivity)
                .enableAutoManage(fragAct, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Log.d(CALLINGTAG,"Failed to connect to Google- " + connectionResult) ;

                        Toast.makeText(callingActivity, "Failed to connect to Google",
                                Toast.LENGTH_SHORT).show();
                    }
                } )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void handleSignInGoogleResult(Task<GoogleSignInAccount> completedTask)
    {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if(account != null)firebaseAuthGoogle(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(CALLINGTAG, "signInResult:failed code=" + e.getStatusCode());

            Toast.makeText(callingActivity, "Google sign in Failed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthGoogle(GoogleSignInAccount acct)
    {
        Log.d(CALLINGTAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(callingActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(CALLINGTAG, "signInWithCredential:success");

                            goToAccountSetup();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(CALLINGTAG, "signInWithCredential:failure", task.getException());

                            Toast.makeText(callingActivity, "Authentication failed ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
