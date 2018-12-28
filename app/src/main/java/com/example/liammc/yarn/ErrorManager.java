package com.example.liammc.yarn;

import java.io.IOException;

public final class ErrorManager {

    //Default constructor
    private ErrorManager()
    {

    }

    public static boolean validatePassword(String password, String passwordConfirm) throws IOException
    {
        String invalidChars = " ,<.>/?;:'[{]}|!@#$%^&*()_+=\\\"`~";

        //Password isn't empty
        if(password == null || password.equals(""))
        {
            throw new IOException("Please enter a password");
        }
        //Confirmed password isn't empty;
        else if(passwordConfirm == null || passwordConfirm.equals(""))
        {
            throw new IOException("Please confirm your password");
        }
        //Password has valid chars
        else if(!checkStringForChars(password, invalidChars))
        {
            throw new IOException("Invalid Password - Only alphabetic, numeric and \"_\" characters are allowed!");
        }
        //Password equals the confirmed password
        else if(!password.equals(passwordConfirm))
        {
            throw new IOException("Password does not equal confirmed password");
        }

        return true;
    }

    public static boolean validateEmail(String email) throws IOException
    {
        String at = "@";
        String dotCom = ".com";

        if(email == null || email.equals(""))
        {
            throw new IOException("Plese enter an email");
        }
        if(!(email.contains(at) && email.contains(dotCom)))
        {
            throw new IOException("Email is an invalid format");
        }

        return true;
    }

    public static boolean validatePhoneNumber(String phoneNumber) throws IOException
    {
        if(phoneNumber == null && phoneNumber.equals(""))
        {
            throw new IOException("Please Enter a phone Number");
        }
        else if(checkStringNumeric(phoneNumber))
        {
            throw new IOException("Phone can only be numeric");
        }
        else if(phoneNumber.length() != 9)
        {
            throw new IOException("Not a valid phone Number");
        }

        return true;
    }

    public static boolean validatePhoneCode(String code) throws IOException
    {
        if(code == null && code.equals(""))
        {
            throw new IOException("Please Enter a Code");
        }

        return true;
    }

    //region Utility
    private static boolean checkStringNumeric(String toCheck)
    {
        try
        {
            double d = Double.parseDouble(toCheck);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    private static boolean checkStringForChars(String toCheck, String against)
    {
        for(int i = 0; i< toCheck.length();i++)
        {
            for(int j = 0; j<against.length();j++)
            {
                if(toCheck.charAt(i) == against.charAt(j)) return false;
            }
        }

        return true;
    }
    //endregion
}
