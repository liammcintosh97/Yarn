package com.example.liammc.yarn.utility;


import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class DateTools
{
    /*This class is used for formatting dates and times*/

    public static Long dateStringToMilli(String dateString) {
        /*Converts a date string into a milli. It first parses the string into a SimpleDateFormat
        * to see if its a valid date. If it doesn't trigger an exception its a valid date and then
        * gets the milli time*/

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy",Locale.getDefault());
            Date date = sdf.parse(dateString);

            return date.getTime();
        }
        catch(ParseException e)
        {
            e.printStackTrace();
            Log.e("DateTools","Cannot parse date string");
            return 0L;
        }
    }

    public static Date stringToddMMYY(String dateString) {
        /*Converts a date string into a date object. It first parses the string into a SimpleDateFormat
         * to see if its a valid date then returns the date object*/

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy",Locale.getDefault());
            Date date = sdf.parse(dateString);

            return date;
        }
        catch(ParseException e)
        {
            e.printStackTrace();
            Log.e("DateTools","Cannot parse date string");
            return null;
        }
    }

    public static Date stringTohMM(String dateString) {
        /*Converts a date string into a date object. It first parses the string into a SimpleDateFormat
         * to see if its a valid date then returns the date object*/

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:MM",Locale.getDefault());
            Date date = sdf.parse(dateString);

            return date;
        }
        catch(ParseException e)
        {
            e.printStackTrace();
            Log.e("DateTools","Cannot parse date string");
            return null;
        }
    }

    public static String millisToDurationString(Locale locale,long millis) {
        /*Converts a milliseconds long into a Duration string*/

        try {
            String duration = String.format(locale, "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

            return duration;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }
}
