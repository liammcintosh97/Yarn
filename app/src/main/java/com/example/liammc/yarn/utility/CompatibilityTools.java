package com.example.liammc.yarn.utility;

import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;


public final class CompatibilityTools {
    /*This class is used for setting particular android systems that is dependent on the Android
    * OS that is running on the devise*/

    //region Public methods
    public static void setPasswordAutoFill(EditText passwordEditText) {
        /*Sets the password auto fill on a passed EditText*/

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            passwordEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD);
        }
    }

    public static void setEmailAutoFill(EditText emailEditText) {
        /*Sets the email auto fill on a passed EditText*/

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            emailEditText.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS);
        }
    }

    public static void setUserNameAutoFill(EditText userNameEditText) {
        /*Sets the firebaseUser name auto fill on a passed EditText*/

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            userNameEditText.setAutofillHints(View.AUTOFILL_HINT_NAME);
        }
    }

    public static void setNumberAutoFill(EditText numberEditText) {
        /*Sets the phone number auto fill on a passed EditText*/

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            numberEditText.setAutofillHints(View.AUTOFILL_HINT_PHONE);
        }
    }

    public static void setPopupElevation(PopupWindow window,float elevation) {
        /*Sets the window elevation on a passed window*/

        if(android.os.Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP)
        {
            window.setElevation(elevation);
        }
    }

    //endregion
}
