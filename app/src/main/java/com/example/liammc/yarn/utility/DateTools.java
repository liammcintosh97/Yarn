package com.example.liammc.yarn.utility;

import android.app.Activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class DateTools
{
    public static Calendar StringToCalendar(Activity callingActivity, String timeString)
    {
        Locale locale = callingActivity.getResources().getConfiguration().locale;
        TimeZone timeZone = TimeZone.getDefault();

        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",locale);
        try {

            Calendar calendar = Calendar.getInstance(timeZone,locale);
            calendar.setTime(sdf.parse(timeString));

            return calendar;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Duration StringToDuration(Activity callingActivity, String durationString)
    {
        try
        {
            //Duration duration = new Duration(durationString);

            return null;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static String millisToDurationString(Activity callingActivity,long millis)
    {
        Locale locale = callingActivity.getResources().getConfiguration().locale;

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
