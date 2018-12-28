package com.example.liammc.yarn;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

import java.io.IOException;

public class PhoneAuthWindow
{
    public PhoneAuth auth;

    private Activity mCallingActivity;

    public static PopupWindow window;
    private Context mContext;
    private ConstraintLayout mMainConstraintLayout;

    private View mPhoneAuthView;

    private Button sendPhoneCodeButton;
    private Button resendPhoneCodeButton;
    private Button verifyPhoneButton;
    private EditText phoneCodeInput;
    private EditText phoneNumberInput;

    private CountryCodePicker countryCodePicker;

    public PhoneAuthWindow(Activity _callingActivity)
    {
        this.mCallingActivity = _callingActivity;

        this.SetUpPhonePopup();
        this.SetUpPhoneUI();
    }

    //region Button Methods

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
                auth.verify(number);
            }
        }
        catch(IOException e)
        {
            Toast.makeText(mCallingActivity, e.getMessage(),
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

                auth.resend(number);
            }
        }
        catch(IOException e)
        {
            Toast.makeText(mCallingActivity, e.getMessage(),
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
                auth.signUp(code);
            }
        }
        catch(IOException e)
        {
            Toast.makeText(mCallingActivity, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void OnClosePhoneAuthPressed(View view)
    {
        dissmissPhoneAuth();
    }
    //endregion Button Methods

    //region Set Up

    private void SetUpPhonePopup()
    {
        // Get the application context
        mContext = mCallingActivity.getApplicationContext();

        // Get the widgets reference from XML layout
        mMainConstraintLayout = mCallingActivity.findViewById(R.id.mainConstraintLayout);

        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mCallingActivity.LAYOUT_INFLATER_SERVICE);
        mPhoneAuthView = inflater.inflate(R.layout.popup_sign_up_phone,null);

        double width =  ConstraintLayout.LayoutParams.MATCH_PARENT  ;
        double height = ConstraintLayout.LayoutParams.MATCH_PARENT  ;

        // Initialize a new instance of popup window
        window = new PopupWindow(mPhoneAuthView, (int) width, (int) height,true);
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.setOutsideTouchable(true);
        window.update();

        // Set an elevation value for popup window
        // Call requires API level 21
        if(Build.VERSION.SDK_INT>=21){
            window.setElevation(5.0f);
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
        countryCodePicker = mPhoneAuthView.findViewById(R.id.ccp);

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

    //region UI
    public void ShowPhoneAuth()
    {
        window.showAtLocation(mMainConstraintLayout, Gravity.CENTER,0,0);
    }

    public void ShowVerifyPhoneUI()
    {
        phoneCodeInput.setVisibility(View.VISIBLE);
        verifyPhoneButton.setVisibility(View.VISIBLE);
    }

    public void ShowResendPhoneUI()
    {
        sendPhoneCodeButton.setVisibility(View.INVISIBLE);
        resendPhoneCodeButton.setVisibility(View.VISIBLE);
    }

    public void dissmissPhoneAuth()
    {
        if(window.isShowing()) window.dismiss();
    }
    //endregion
}
