package com.example.liammc.yarn.authentication;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuth extends Authenticator {
    /*This class is used for logging users into Firebase through a Phone.*/

    private String TAG = "PhoneAuth";
    public PhoneAuthWindow window;

    private PhoneAuthProvider mPhoneAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mPhoneCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    PhoneAuth(Activity _callingActivity, FirebaseAuth _mAuth) {
        super(_mAuth);
        this.initPhoneAuth(_callingActivity);
    }

    //region Init

    private void initPhoneAuth(final Activity activity) {
        /*This Method initializes the Phone Authentication*/

        //Get the client
        mPhoneAuth = PhoneAuthProvider.getInstance();

        //Initialize the callbacks
        mPhoneCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                /*Runs when the phone verification is successful. So proceed to log the firebaseUser into
                Firebase */

                Log.d(TAG, "onVerificationCompleted:" + credential);
                externalAuth(activity,credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                /*Runs when the phone verification fails. So proceed to alert the firebaseUser to the error */

                Toast.makeText(activity, "Phone Verification Failed",
                        Toast.LENGTH_SHORT).show();

                //Internally log the error message
                Log.e(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    Log.e(TAG,e.getMessage());
                } else if (e instanceof FirebaseTooManyRequestsException)
                {
                    Log.e(TAG,e.getMessage());
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                /*Runs when the users request for a verification code to be sent. So the application
                 * must show some UI to input the code into */

                Log.d(TAG, "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;

                window.ShowVerify();
                window.ShowResend();
            }
        };
    }

    //endregion

    //region Public Methods

    @Override
    public void goToTermsAcceptance(Activity activity) {
        /*This overriding method goes to account Setup but also dismisses the Phone Authentication
        window*/
        window.dismiss();
        super.goToTermsAcceptance(activity);
    }

    @Override
    public void goToInitialization(FirebaseAuth auth,Activity activity) {
        /*This overriding method goes to account Setup but also dismisses the Phone Authentication
        window*/
        window.dismiss();
        super.goToInitialization(auth,activity);
    }

    //endregion

    //region Package Private Methods

    void verify(Activity activity,String number) {
       /*Initiates the verification of the passed phone number*/
        mPhoneAuth.verifyPhoneNumber(number, 60, TimeUnit.SECONDS, activity, mPhoneCallbacks);
    }

    void resend(Activity activity,String number) {
       /*Resend the verification phone number to the passed phone number*/
        mPhoneAuth.verifyPhoneNumber(number, 60, TimeUnit.SECONDS, activity, mPhoneCallbacks,mResendToken);
    }

    void signIn(Activity activity,String code) {
       /*Gets the sign in credentials from the passed phone verification code. Then proceeds to
       process that credential*/

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,code);
        externalAuth(activity, credential);
    }

    //endregion
}
