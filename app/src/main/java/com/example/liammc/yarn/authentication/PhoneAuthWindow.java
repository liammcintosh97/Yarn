package com.example.liammc.yarn.authentication;

import android.app.Activity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.liammc.yarn.utility.CompatibilityTools;
import com.example.liammc.yarn.utility.ErrorManager;
import com.example.liammc.yarn.R;
import com.hbb20.CountryCodePicker;

import java.io.IOException;

public class PhoneAuthWindow {
    /*This Class is the window interface used for logging the firebaseUser into Firebase with a phone*/

    private static String TAG = "PhoneAuthWindow";
    public PhoneAuth auth;

    //Window
    private final ViewGroup parentViewGroup;
    public static PopupWindow window;
    public View mPhoneAuthView;

    //UI
    private Button sendPhoneCodeButton;
    private Button resendPhoneCodeButton;
    private Button verifyPhoneButton;
    private Button closePhoneAuthButton;
    private EditText phoneCodeInput;
    private EditText phoneNumberInput;
    private CountryCodePicker countryCodePicker;

    PhoneAuthWindow(Activity activity,ViewGroup _parent) {
        this.parentViewGroup = _parent;

        this.initPopup(activity);
        this.initUI(activity);
    }

    //region Init

    private void initPopup(Activity activity) {

        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mPhoneAuthView = inflater.inflate(R.layout.popup_sign_up_phone,parentViewGroup,false);

        // Initialize a new instance of popup window
        double width =  ConstraintLayout.LayoutParams.MATCH_PARENT  ;
        double height = ConstraintLayout.LayoutParams.MATCH_PARENT  ;

        window = new PopupWindow(mPhoneAuthView, (int) width, (int) height,true);
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.setOutsideTouchable(true);
        window.update();

        CompatibilityTools.setPopupElevation(window,5.0f);
    }

    private void initUI(Activity activity) {
        /*This method initializes the Phone auth window UI*/

        //Get Phone Inputs
        phoneCodeInput = mPhoneAuthView.findViewById(R.id.phoneCodeInput);
        phoneNumberInput = mPhoneAuthView.findViewById(R.id.phoneNumberInput);

        //Get Country Code Picker
        countryCodePicker = mPhoneAuthView.findViewById(R.id.ccp);

        initButtons(activity);

        //Initialize visibilities
        phoneCodeInput.setVisibility(View.INVISIBLE);
        verifyPhoneButton.setVisibility(View.INVISIBLE);

        sendPhoneCodeButton.setVisibility(View.VISIBLE);
        resendPhoneCodeButton.setVisibility(View.INVISIBLE);
    }

    private void initButtons(final Activity activity){
        /*This method initializes the phone auth buttons*/

        //Get Phone buttons
        closePhoneAuthButton = mPhoneAuthView.findViewById(R.id.closePhoneButton);
        sendPhoneCodeButton = mPhoneAuthView.findViewById(R.id.sendPhoneCodeButton);
        verifyPhoneButton = mPhoneAuthView.findViewById(R.id.verifyPhoneCodeButton);
        resendPhoneCodeButton = mPhoneAuthView.findViewById(R.id.resendPhoneCodeButton);

        //Set up the listeners for all the buttons
        sendPhoneCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnSendPhoneCodePressed(activity);
            }
        });

        resendPhoneCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnResendPhoneCodePressed(activity);
            }
        });

        closePhoneAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnClosePhoneAuthPressed();
            }
        });

        verifyPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnVerifyPhonePressed(activity);
            }
        });
    }

    //endregion

    //region Button Methods

    private void OnSendPhoneCodePressed(Activity activity) {
        /*Runs when the firebaseUser presses the send Phone Code*/

        //Get firebaseUser Input
        String phoneNumber = phoneNumberInput.getText().toString();
        String countryCode = countryCodePicker.getSelectedCountryCode();

        try {
            //Validate and format the phone number
            ErrorManager.validatePhoneNumber(phoneNumber);
            String number = "+" + countryCode + phoneNumber;
            Log.d(TAG,"phone number = " + number);

            auth.verify(activity,number);
        }
        catch(IOException e) {
            /*An exception has occurred so alert the firebaseUser*/
            Toast.makeText(activity, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void OnResendPhoneCodePressed(Activity activity) {
        /*Runs when the firebaseUser presses the resend code button*/

        //Get firebaseUser Input
        String phoneNumber = phoneNumberInput.getText().toString();
        String countryCode = countryCodePicker.getSelectedCountryCode();

        try {
            //Validate and format the phone number
            ErrorManager.validatePhoneNumber(phoneNumber);
            String number = "+" + countryCode + phoneNumber;
            Log.d(TAG,"phone number = " + number);

            auth.resend(activity,number);
        }
        catch(IOException e) {
            /*An exception has occurred so alert the firebaseUser*/
            Toast.makeText(activity, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void OnVerifyPhonePressed(Activity activity) {
        /*Runs when the firebaseUser presses the verify phone number button*/

        String code  = phoneCodeInput.getText().toString();

        try {
            //Validate the phone code
            ErrorManager.validatePhoneCode(code);

            auth.signIn(activity,code);
        }
        catch(IOException e) {
            /*An exception has occurred so alert the firebaseUser*/
            Toast.makeText(activity, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void OnClosePhoneAuthPressed() {
        /*Runs when the firebaseUser presses the close Phone Auth button*/
        dismissPhoneAuth();
    }
    //endregion

    //region Private Methods
    void ShowAuth() {
        /*Shows the Phone Auth window*/
        window.showAtLocation(parentViewGroup, Gravity.CENTER, 0, 0);
    }

    void ShowVerify() {
        /*Shows the UI needed to verify the phone number*/

        phoneCodeInput.setVisibility(View.VISIBLE);
        verifyPhoneButton.setVisibility(View.VISIBLE);
    }

    void ShowResend() {
        /*Shows the UI needed to resend the phone code*/

        sendPhoneCodeButton.setVisibility(View.INVISIBLE);
        resendPhoneCodeButton.setVisibility(View.VISIBLE);
    }

    void dismissPhoneAuth() {
        /*Dismisses the Phone Auth window*/
        if(window.isShowing()) window.dismiss();
    }
    //endregion
}
