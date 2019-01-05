package com.example.liammc.yarn.authentication;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.liammc.yarn.utility.CompatabiltyTools;
import com.example.liammc.yarn.utility.ErrorManager;
import com.example.liammc.yarn.R;
import com.hbb20.CountryCodePicker;

import java.io.IOException;

public class PhoneAuthWindow
{
    private static String TAG = "PhoneAuthWindow";

    public PhoneAuth auth;

    private final Activity mCallingActivity;
    private final ViewGroup parentViewGroup;

    public static PopupWindow window;

    public View mPhoneAuthView;

    private Button sendPhoneCodeButton;
    private Button resendPhoneCodeButton;
    private Button verifyPhoneButton;
    private EditText phoneCodeInput;
    private EditText phoneNumberInput;

    private CountryCodePicker countryCodePicker;

    PhoneAuthWindow(Activity _callingActivity,ViewGroup _parent)
    {
        this.mCallingActivity = _callingActivity;
        this.parentViewGroup = _parent;

        this.SetUpPhonePopup();
        this.SetUpPhoneUI();
    }


    //region Button Methods

    private void OnSendPhoneCodePressed()
    {
        //Get user Input
        String phoneNumber = phoneNumberInput.getText().toString();
        String countryCode = countryCodePicker.getSelectedCountryCode();

        try
        {
            ErrorManager.validatePhoneNumber(phoneNumber);

            String number = "+" + countryCode + phoneNumber;

            Log.d(TAG,"phone number = " + number);

            auth.verify(number);

        }
        catch(IOException e)
        {
            Toast.makeText(mCallingActivity, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void OnResendPhoneCodePressed()
    {
        //Get user Input
        String phoneNumber = phoneNumberInput.getText().toString();
        String countryCode = countryCodePicker.getSelectedCountryCode();

        try
        {
            ErrorManager.validatePhoneNumber(phoneNumber);

            String number = countryCode + phoneNumber;
            auth.resend(number);
        }
        catch(IOException e)
        {
            Toast.makeText(mCallingActivity, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void OnVerifyPhonePressed()
    {
        String code  = phoneCodeInput.getText().toString();

        try
        {
            ErrorManager.validatePhoneCode(code);
            auth.signIn(code);
        }
        catch(IOException e)
        {
            Toast.makeText(mCallingActivity, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void OnClosePhoneAuthPressed()
    {
        dissmissPhoneAuth();
    }
    //endregion Button Methods

    //region Set Up

    private void SetUpPhonePopup()
    {

        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) mCallingActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mPhoneAuthView = inflater.inflate(R.layout.popup_sign_up_phone,parentViewGroup,false);

        // Initialize a new instance of popup window
        double width =  ConstraintLayout.LayoutParams.MATCH_PARENT  ;
        double height = ConstraintLayout.LayoutParams.MATCH_PARENT  ;

        window = new PopupWindow(mPhoneAuthView, (int) width, (int) height,true);
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.setOutsideTouchable(true);
        window.update();

        CompatabiltyTools.setPopupElevation(window,5.0f);
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
        countryCodePicker = mPhoneAuthView.findViewById(R.id.ccp);

        //Set up the listners for all the buttons
        sendPhoneCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnSendPhoneCodePressed();
            }
        });

        resendPhoneCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnResendPhoneCodePressed();
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
                OnVerifyPhonePressed();
            }
        });

        //Intialize visiblities
        phoneCodeInput.setVisibility(View.INVISIBLE);
        verifyPhoneButton.setVisibility(View.INVISIBLE);

        sendPhoneCodeButton.setVisibility(View.VISIBLE);
        resendPhoneCodeButton.setVisibility(View.INVISIBLE);
    }

    //endregion

    //region UI
    void ShowPhoneAuth()
    {
        window.showAtLocation(parentViewGroup, Gravity.CENTER, 0, 0);
    }

    void ShowVerifyPhoneUI()
    {
        phoneCodeInput.setVisibility(View.VISIBLE);
        verifyPhoneButton.setVisibility(View.VISIBLE);
    }

    void ShowResendPhoneUI()
    {
        sendPhoneCodeButton.setVisibility(View.INVISIBLE);
        resendPhoneCodeButton.setVisibility(View.VISIBLE);
    }

    void dissmissPhoneAuth()
    {
        if(window.isShowing()) window.dismiss();
    }
    //endregion
}
