package com.example.liammc.yarn;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class SignUpPhoneActivity extends AppCompatActivity {

    private String TAG = "SignUpPhoneActivity";
    private double PWIDTH = 0.8;
    private double PHEIGHT = 0.8;

    private EditText codeInput;
    private Button verifyButton;

    private PhoneAuthProvider mPhoneAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mPhoneCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_phone);

        SetDisplay();
        InitUI();
        SetUpPhoneAuth();
    }

    //region SetUP
    private void SetUpPhoneAuth()
    {
        mPhoneAuth = PhoneAuthProvider.getInstance();

        mPhoneCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential)
            {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                returnData(mVerificationId,credential.getSmsCode());
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                ShowVerifyUI();
                // ...
            }
        };
    }
    //endregion

    //region Buttons
    public void OnSendCodePressed(View view)
    {
        //Get user Input
        EditText phoneNumberInput = findViewById(R.id.phoneNumberInput);
        String phoneNumber = phoneNumberInput.getText().toString();

        mPhoneAuth.verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, mPhoneCallbacks);
    }

    public void OnVerifyPressed(View view)
    {
        //Get user Input
        String code = codeInput.getText().toString();
        returnData(mVerificationId,code);
    }
    //endregion

    //region UI
    private void InitUI()
    {
        codeInput = findViewById(R.id.codeInput);
        verifyButton = findViewById(R.id.verifyPhoneButton);

        codeInput.setVisibility(View.INVISIBLE);
        verifyButton.setVisibility(View.INVISIBLE);
    }

    private void ShowVerifyUI()
    {
        codeInput.setVisibility(View.VISIBLE);
        verifyButton.setVisibility(View.VISIBLE);
    }

    private void SetDisplay()
    {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * PWIDTH),(int)(height * PHEIGHT));
    }
    //endregion

    //region Utility
    private void returnData(String verificationID, String code)
    {
        Intent data = new Intent();

        data.putExtra("verificationID",verificationID);
        data.putExtra("code",code);
        setResult(RESULT_OK, data);

        finish();
    }
    //endregion
}
