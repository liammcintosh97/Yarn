package com.example.liammc.yarn.utility;

import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;


public final class CompatabiltyTools
{
    //region Set Auto Fill
    public static void setPasswordAutofill(EditText passwordEditText)
    {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            passwordEditText.setAutofillHints(View.AUTOFILL_HINT_PASSWORD);
        }
    }

    public static void setEmailAutofill(EditText emailEditText)
    {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            emailEditText.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS);
        }
    }

    public static void setUserNameAutoFill(EditText userNameEditText)
    {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            userNameEditText.setAutofillHints(View.AUTOFILL_HINT_NAME);
        }
    }

    public static void setNumberAutoFill(EditText numberEditText)
    {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            numberEditText.setAutofillHints(View.AUTOFILL_HINT_PHONE);
        }
    }
    //endregion

    //region PopUpwindow Elevation

    public static void setPopupElevation(PopupWindow window,float evelvation)
    {
        if(android.os.Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP)
        {
            window.setElevation(evelvation);
        }
    }

    //endregion
}
