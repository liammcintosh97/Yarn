package com.example.liammc.yarn.utility;

import java.io.IOException;

public final class ErrorManager {
    /*This class is used for managing firebaseUser input errors*/

    private static String TAG = "ErrorManager";

    public static void validatePassword(String password, String passwordConfirm) throws IOException {
        /*This method validates the firebaseUser's passed passwords by comparing its characters with the
        invalid Chars string, checking if they aren't empty and they match each other
         */

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
    }

    public static void validateEmail(String email) throws IOException {
        /*This method checks if the passed string is a valid email by checking that it isn't empty
        * and has a "@" and a ".com"*/

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
    }

    public static void validatePhoneNumber(String phoneNumber) throws IOException {
        /*This method checks if the passed string is a valid phone number by checking that it isn't
        empty, is numeric and has 9 numbers
         */

        if(phoneNumber == null || phoneNumber.equals(""))
        {
            throw new IOException("Please enter a phone number");
        }
        else if(!checkStringNumeric(phoneNumber))
        {
            throw new IOException("Phone can only be numeric");
        }
        else if(phoneNumber.length() != 9)
        {
            throw new IOException("Not a valid phone number");
        }
    }

    public static void validatePhoneCode(String code) throws IOException {
        /*Checks if the passed string is a phone code by checking if it isn't empty and is numeric*/

        if(code == null || code.equals(""))
        {
            throw new IOException("Please Enter a Code");
        }
        else if(!checkStringNumeric(code))
        {
            throw new IOException("Phone code can only be numeric");
        }
    }

    //region Private Methods
    private static boolean checkStringNumeric(String toCheck) {
        /*Checks if the passed string is numeric by looping over the string and seeing if it doesn't
        trigger a parseInt exception
         */

        try
        {
            Integer.parseInt(toCheck);
            return true;
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
    }

    private static boolean checkStringForChars(String toCheck, String against) {
        /*Checks if the toCheck string contains any character in the against string. It goes through
        each character of the toCheck string and then loops over the against string to see if there
        is an equality
         */

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
