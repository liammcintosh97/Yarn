package com.example.liammc.yarn;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.hbb20.CountryCodePicker;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class SignUpActivity extends AppCompatActivity {

    private int GO_SIGN_IN = 0;
    private int PH_SIGN_IN = 1;

    private String TAG = "SignUpActivity";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    //Google
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleApiClient mGoogleApiClient;

    //FaceBook
    private CallbackManager mCallbackManager;
    private LoginManager mLoginManager;

    //Twitter
    private TwitterAuthClient mTwitterAuthClient;

    //Phone
    private PhoneAuthProvider mPhoneAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mPhoneCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PopupWindow mPopupWindow;
    private Context mContext;
    private ConstraintLayout mMainConstraintLayout;

    private View mPhoneAuthView;

    private Button sendPhoneCodeButton;
    private Button resendPhoneCodeButton;
    private Button verifyPhoneButton;

    private EditText phoneCodeInput;
    private EditText phoneNumberInput;

    private CountryCodePicker countryCodePicker;

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Set up the phone auth interface
        SetUpPhonePopup();
        SetUpPhoneUI();

        //Set up all the authorization systems
        SetUpFirebaseAuth();
        SetUpGoogleAuth();
        SetUpFaceBookAuth();
        SetUpTwitterAuth();
        SetUpPhoneAuth();

        checkSignIn();
    }

    @Override
    public void onBackPressed()
    {
        if(mPopupWindow.isShowing())
        {
            mPopupWindow.dismiss();
        }
        else super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            if (requestCode == GO_SIGN_IN) {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInGoogleResult(task);
            }
            else if(requestCode == PH_SIGN_IN)
            {
                String id = data.getStringExtra("verificationID");
                String code = data.getStringExtra("code");

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(id, code);
                firebaseAuthPhone(credential);
            }
        }

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
    }

    //region Set Up
    private void SetUpFirebaseAuth()
    {
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
    }

    private void SetUpGoogleAuth()
    {
        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleApiClient = new GoogleApiClient.Builder(SignUpActivity.this)
                .enableAutoManage(SignUpActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Log.d(TAG,"Failed to connect to Google- " + connectionResult) ;

                        Toast.makeText(SignUpActivity.this, "Failed to connect to Google",
                                Toast.LENGTH_SHORT).show();
                    }
                } )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
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
                        Log.d(TAG, "Facebook Login is successful");
                        firebaseAuthFacebook(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(SignUpActivity.this, "Facebook Login Cancel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(SignUpActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void SetUpTwitterAuth()
    {

        mTwitterAuthClient = new TwitterAuthClient();
    }

    private void SetUpPhoneAuth()
    {
        mPhoneAuth = PhoneAuthProvider.getInstance();

        mPhoneCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                Log.d(TAG, "onVerificationCompleted:" + credential);

                firebaseAuthPhone(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Log.w(TAG, "onVerificationFailed", e);

                Toast.makeText(SignUpActivity.this, "Phone Verification Failed",
                        Toast.LENGTH_SHORT).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    Log.v(TAG,e.getMessage());
                } else if (e instanceof FirebaseTooManyRequestsException)
                {
                    Log.v(TAG,e.getMessage());
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                Log.d(TAG, "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;

                ShowVerifyPhoneUI();
                ShowResendPhoneUI();
            }
        };
    }

    //endregion

    //region Button Methods
    public void OnSignUpPressed(View view)
    {
        //Get User Input
        EditText emailEditText = findViewById(R.id.emailInput);
        EditText passwordEditText = findViewById(R.id.passwordInput);
        EditText confirmPasswordEditText = findViewById(R.id.confirmPasswordInput);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        try
        {
            if(ErrorManager.validateEmail(email)
                    && ErrorManager.validatePassword(password,confirmPassword))
            {
                firebaseAuthEmail(email,password);
            }
        }
        catch(IOException e)
        {
            Toast.makeText(SignUpActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void OnSignUpFacebookPressed(View view)
    {
        mLoginManager.logInWithReadPermissions(SignUpActivity.this,
                Arrays.asList("public_profile", "user_friends"));
    }

    public void OnSignUpGooglePressed(View view)
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GO_SIGN_IN);
    }

    public void OnSignUpTwitterPressed(View view)
    {
        mTwitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                Log.d(TAG, "twitterLogin:success" + twitterSessionResult);
                handleSignInTwitterResult(twitterSessionResult.data);
            }

            @Override
            public void failure(TwitterException e) {
                Log.w(TAG, "twitterLogin:failure", e);

                Toast.makeText(SignUpActivity.this, "Twitter sign in Failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void OnSignUpPhonePressed(View view)
    {
        mPopupWindow.showAtLocation(mMainConstraintLayout, Gravity.CENTER,0,0);
    }

    public void OnSendPhoneCodePressed(View view)
    {
        //Get user Input
        String phoneNumber = phoneNumberInput.getText().toString();
        String countryCode = countryCodePicker.getSelectedCountryCode();

        try
        {
            if(ErrorManager.validatePhoneNumber(phoneNumber))
            {
                String number = countryCode + phoneNumber;

                mPhoneAuth.verifyPhoneNumber(number, 60, TimeUnit.SECONDS, this, mPhoneCallbacks);
            }
        }
        catch(IOException e)
        {
            Toast.makeText(SignUpActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void OnResendPhoneCodePressed(View view)
    {
        //Get user Input
        String phoneNumber = phoneNumberInput.getText().toString();
        String countryCode = countryCodePicker.getSelectedCountryCode();

        try
        {
            if(ErrorManager.validatePhoneNumber(phoneNumber))
            {
                String number = countryCode + phoneNumber;

                mPhoneAuth.verifyPhoneNumber(number, 60, TimeUnit.SECONDS, this, mPhoneCallbacks,mResendToken);
            }
        }
        catch(IOException e)
        {
            Toast.makeText(SignUpActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void OnVerifyPhonePressed(View view)
    {
        String code  = phoneCodeInput.getText().toString();

        try
        {
            if(ErrorManager.validatePhoneCode(code))
            {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,code);
                firebaseAuthPhone(credential);
            }
        }
        catch(IOException e)
        {
            Toast.makeText(SignUpActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void OnClosePhoneAuthPressed(View view)
    {
        mPopupWindow.dismiss();
    }

    //endregion

    //region Sign in Handles
    private void handleSignInGoogleResult(Task<GoogleSignInAccount> completedTask)
    {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if(account != null)firebaseAuthGoogle(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());

            Toast.makeText(SignUpActivity.this, "Google sign in Failed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignInTwitterResult(TwitterSession session)
    {
        Log.d(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        firebaseAuthTwitter(credential);
    }
    //endregion

    //region FireBaseAuth
    private void firebaseAuthEmail(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            mCurrentUser = mAuth.getCurrentUser();
                            goToAccountSetup();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());

                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void firebaseAuthFacebook(AccessToken token)
    {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            mCurrentUser = mAuth.getCurrentUser();
                            goToAccountSetup();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            Toast.makeText(SignUpActivity.this, "Authentication failed ",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void firebaseAuthGoogle(GoogleSignInAccount acct)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            goToAccountSetup();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            Toast.makeText(SignUpActivity.this, "Authentication failed ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void firebaseAuthTwitter(AuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            mCurrentUser = mAuth.getCurrentUser();

                            goToAccountSetup();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void firebaseAuthPhone(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            mCurrentUser = task.getResult().getUser();
                            goToAccountSetup();

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                Toast.makeText(SignUpActivity.this, "Invalid Credentials",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    //endregion

    //region UI
    private void ShowVerifyPhoneUI()
    {
        phoneCodeInput.setVisibility(View.VISIBLE);
        verifyPhoneButton.setVisibility(View.VISIBLE);
    }

    private void ShowResendPhoneUI()
    {
        sendPhoneCodeButton.setVisibility(View.INVISIBLE);
        resendPhoneCodeButton.setVisibility(View.VISIBLE);
    }

    private void SetUpPhonePopup()
    {
        // Get the application context
        mContext = getApplicationContext();

        // Get the widgets reference from XML layout
        mMainConstraintLayout = findViewById(R.id.mainConstraintLayout);

        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        mPhoneAuthView = inflater.inflate(R.layout.popup_sign_up_phone,null);

        double width =  ConstraintLayout.LayoutParams.MATCH_PARENT  ;
        double height = ConstraintLayout.LayoutParams.MATCH_PARENT  ;

        // Initialize a new instance of popup window
        mPopupWindow = new PopupWindow(mPhoneAuthView, (int) width, (int) height,true);
        mPopupWindow.setAnimationStyle(R.style.popup_window_animation_phone);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.update();

        // Set an elevation value for popup window
        // Call requires API level 21
        if(Build.VERSION.SDK_INT>=21){
            mPopupWindow.setElevation(5.0f);
        }
    }

    private void SetUpPhoneUI()
    {
        //Get Phone buttons
        Button closePhoneAuthButton = mPhoneAuthView.findViewById(R.id.closePhoneButton);
        sendPhoneCodeButton = mPhoneAuthView.findViewById(R.id.sendPhoneCodeButton);
        verifyPhoneButton = mPhoneAuthView.findViewById(R.id.verifyPhoneCodeButton);
        resendPhoneCodeButton = mPhoneAuthView.findViewById(R.id.resendPhoneCodeButton);

        //Get Phone Inputs
        phoneCodeInput = mPhoneAuthView.findViewById(R.id.phoneCodeInput);
        phoneNumberInput = mPhoneAuthView.findViewById(R.id.phoneNumberInput);

        //Get Country Code Picker
        countryCodePicker =  (CountryCodePicker) mPhoneAuthView.findViewById(R.id.ccp);

        //Set up the listners for all the buttons
        sendPhoneCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnSendPhoneCodePressed(view);
            }
        });

        resendPhoneCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnResendPhoneCodePressed(view);
            }
        });

        closePhoneAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnClosePhoneAuthPressed(view);
            }
        });

        verifyPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnVerifyPhonePressed(view);
            }
        });

        //Intialize visiblities
        phoneCodeInput.setVisibility(View.INVISIBLE);
        verifyPhoneButton.setVisibility(View.INVISIBLE);

        sendPhoneCodeButton.setVisibility(View.VISIBLE);
        resendPhoneCodeButton.setVisibility(View.INVISIBLE);
    }
    //endregion

    //region Utility

    private void checkSignIn()
    {
        if(mCurrentUser != null)
        {
            goToAccountSetup();
        }
    }

    private void goToAccountSetup()
    {
        if(mPopupWindow.isShowing()) mPopupWindow.dismiss();

        Intent myIntent = new Intent(getBaseContext(),   IntroActivity.class);
        startActivity(myIntent);
    }
    //endregion

}
