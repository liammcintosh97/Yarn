package com.example.liammc.yarn.authentication;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

class GoogleAuth extends Authenticator {
    /*This class is used for logging users into Firebase through Google. If they do this for the
    first time a new Firebase account is created. Every other login after that is to that previously
    made Firebase Account unless its removed
     */

    //Google
    private GoogleApiClient apiClient;
    private GoogleSignInOptions gso;
    private GoogleSignInClient signInClient;

    private final String TAG = "GoogleAuth";

    //Constructor
    GoogleAuth(Activity activity, FirebaseAuth _mAuth) {
        super(_mAuth);
        this.InitGoogleAuth(activity);
    }

    //region Init

    private void InitGoogleAuth(final Activity activity) {
        /*This method initializes the Google Authentication*/

        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        signInClient = GoogleSignIn.getClient(activity, gso);

        apiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage((FragmentActivity) activity, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG,"Failed to connect to Google- " + connectionResult) ;

                        Toast.makeText(activity, "Failed to connect to Google",
                                Toast.LENGTH_SHORT).show();
                    }
                } )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    //endregion

    //region Public Methods

    public void login(Activity activity) {
        /*This method takes the firebaseUser to the Google login activity so that it's results can be
        processed
         */

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
        activity.startActivityForResult(signInIntent, AuthActivity.GO_SIGN_IN);
    }

    //endregion

    //region Private Methods

    void handleResult(Activity activity, Task<GoogleSignInAccount> completedTask) {
        /*This method Handles the Google Sign in result*/

        try {
            /*If the account sign in is successful then proceed to log into Firebase*/

            //Get the account and credential
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

            if(account != null)externalAuth(activity,credential);

        } catch (ApiException e) {
            /*If there was an exception to the Google sign in process alert the firebaseUser*/
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());

            Toast.makeText(activity, "Google sign in Failed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //endregion
}
