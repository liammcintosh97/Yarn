package com.example.liammc.yarn.utility;


import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class DateTools
{
    /*This class is used for formatting dates and times*/

    public static Long dateStringToMilli(String dateString) {
        /*Converts a date string into a milli. It first parses the string into a SimpleDateFormat
        * to see if its a valid date. If it doesn't trigger an exception its a valid date and then
        * gets the milli time*/

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy",Locale.getDefault());
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

    public static String parse(int day, int month, int year){

        String result;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy", Locale.getDefault());
        result = sdf.format(calendar.getTime());

        return result;
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

    public static Date stringTohmma(String dateString) {
        /*Converts a date string into a date object. It first parses the string into a SimpleDateFormat
         * to see if its a valid date then returns the date object*/

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a",Locale.getDefault());
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

    public static  Long HHmmssStringToMillis(String timeString){

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(timeString);

            return date.getTime();
        }
        catch(ParseException e)
        {
            e.printStackTrace();
            Log.e("DateTools","Cannot parse time string");
            return 0L;
        }

    }
}
