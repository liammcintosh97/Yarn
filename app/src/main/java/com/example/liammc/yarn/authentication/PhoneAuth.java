package com.example.liammc.yarn.authentication;


import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneAuth extends Authenticator
{
    public PhoneAuthWindow window;

    private PhoneAuthProvider mPhoneAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mPhoneCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    PhoneAuth(Activity _callingActivity, FirebaseAuth _mAuth, FirebaseUser _currentUser)
    {
        super(_callingActivity,_mAuth, _currentUser);
        this.SetUpPhoneAuth();
    }

    private void SetUpPhoneAuth()
    {
        mPhoneAuth = PhoneAuthProvider.getInstance();

        mPhoneCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                Log.d(CALLINGTAG, "onVerificationCompleted:" + credential);

                firebaseAuthPhone(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Log.w(CALLINGTAG, "onVerificationFailed", e);

                Toast.makeText(callingActivity, "Phone Verification Failed",
                        Toast.LENGTH_SHORT).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    Log.v(CALLINGTAG,e.getMessage());
                } else if (e instanceof FirebaseTooManyRequestsException)
                {
                    Log.v(CALLINGTAG,e.getMessage());
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                Log.d(CALLINGTAG, "onCodeSent:" + verificationId);

                mVerificationId = verificationId;
                mResendToken = token;

                window.ShowVerifyPhoneUI();
                window.ShowResendPhoneUI();
            }
        };
    }

    void verify(String number)
    {
        mPhoneAuth.verifyPhoneNumber(number, 60, TimeUnit.SECONDS, callingActivity, mPhoneCallbacks);
    }

    void resend(String number)
    {
        mPhoneAuth.verifyPhoneNumber(number, 60, TimeUnit.SECONDS, callingActivity, mPhoneCallbacks,mResendToken);
    }

    void signIn(String code)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,code);
        firebaseAuthPhone(credential);
    }

    private void firebaseAuthPhone(PhoneAuthCredential credential)
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
                            // Sign in failed, display a message and update the UI
                            Log.w(CALLINGTAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                Toast.makeText(callingActivity, "Invalid Credentials",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public void goToAccountSetup()
    {
        window.dissmissPhoneAuth();
        super.goToAccountSetup();
    }

    public void goToMap()
    {
        window.dissmissPhoneAuth();
        super.goToMap();
    }
}
