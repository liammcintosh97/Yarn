package com.example.liammc.yarn.utility;

import android.app.Activity;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class DateTools
{
    public static Long dateStringToMilli(String dateString)
    {
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

    public static Date dateStringToDateObject(String dateString)
    {
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

    public static String millisToDurationString(Locale locale,long millis)
    {
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
