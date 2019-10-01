package com.example.liammc.yarn.time;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.liammc.yarn.R;
import com.example.liammc.yarn.utility.DateTools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppointmentElement  extends LinearLayout {

    private final String TAG = "AppointmentElement";
    private final String toTheHour = ":00 ";
    private final String halfPast = ":30 ";

    private Calendar timeCal;
    private Calendar timeHalfCal;
    private Date timeDate;
    private Date timeHalfDate;
    private Button timeButton;
    private Button timeButtonHalf;
    private TextView timeTitle;

    public String selectedTime;
    public String timeString;
    public String timeHalfString;

    public AppointmentElement(Context _context){
        super(_context);
        this.initView();
        this.initCustomAttrs(_context,null,0);
    }

    public AppointmentElement(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initView();
        this.initCustomAttrs(context,attrs,0);
    }

    public AppointmentElement(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initView();
        this.initCustomAttrs(context,attrs,defStyle);
    }

    //region init

    public void initView(){
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.element_time, this, true);
    }

    public void initUI(int _hour, String _ampm){

        initTime(_hour,_ampm);

        LinearLayout main =  (LinearLayout) getChildAt(0);
        timeTitle =  (TextView) main.getChildAt(0);
        timeTitle.setText(timeString);

        LinearLayout bl = (LinearLayout) main.getChildAt(1);
        timeButton =  (Button) bl.getChildAt(0);
        timeButton.setText(timeString);
        timeButtonHalf = (Button) bl.getChildAt(1);
        timeButtonHalf.setText(timeHalfString);

    }

    public void initCustomAttrs(Context _context,AttributeSet attrs, int defStyle){
        TypedArray a = _context.obtainStyledAttributes(attrs,
                R.styleable.AppointmentElement, defStyle, 0);
        int hour = a.getInteger(R.styleable.AppointmentElement_hour,0);
        String ampm = a.getString(R.styleable.AppointmentElement_am_pm);
        a.recycle();

        initUI(hour,ampm);
    }

    public void initTime(int _hour, String _ampm) {
        timeString = _hour + toTheHour + _ampm;
        timeHalfString = _hour + halfPast + _ampm;

        SimpleDateFormat format = new SimpleDateFormat("h:mm a");

        //Make unformatted Calenders
        Calendar ufToHour = Calendar.getInstance(Locale.getDefault());
        ufToHour.setTime(DateTools.stringTohmma(timeString));

        Calendar ufHalfPast = Calendar.getInstance(Locale.getDefault());
        ufHalfPast.setTime(DateTools.stringTohmma(timeHalfString));

        //Format to the hour calendar
        timeCal = Calendar.getInstance(Locale.getDefault());

        timeCal.set(Calendar.HOUR_OF_DAY,ufToHour.get(Calendar.HOUR_OF_DAY));
        timeCal.set(Calendar.MINUTE,ufToHour.get(Calendar.MINUTE));

        timeDate =  timeCal.getTime();

        //Format half past calendar
        timeHalfCal = Calendar.getInstance(Locale.getDefault());

        timeHalfCal.set(Calendar.HOUR_OF_DAY,ufHalfPast.get(Calendar.HOUR_OF_DAY));
        timeHalfCal.set(Calendar.MINUTE,ufHalfPast.get(Calendar.MINUTE));

        timeHalfDate = timeHalfCal.getTime();
    }

    //endregion

    //region Public Methods

    public void setButtons(OnClickListener listener){
        timeButton.setOnClickListener(listener);
        timeButtonHalf.setOnClickListener(listener);
    }

    public boolean validTime(Calendar currentTime, Calendar openingTime, Calendar closingTime){

        Date currentDate = currentTime.getTime();
        boolean valid;

        //To the hour
        if(timeDate.before(currentDate)){
            setEnabled(timeButton);
            valid = false;
        }
        else{
            //if(timeDate.before(openingTime) || timeDate.after(closingTime)){
                //setEnabled(timeButton);
                //valid =  false;
            //}
            valid = true;
        }

        //Half past the hour
        if(timeHalfDate.before(currentDate)){
            setEnabled(timeButtonHalf);
            valid =  false;
        }
        else{
            //if(timeHalfDate.before(openingTime) || timeHalfDate.after(closingTime)){
                //setEnabled(timeButtonHalf);
                //valid =  false;
            //}
            valid = true;
        }
        return valid;
    }

    //endregion

    //region Private Methods

    private void setEnabled(Button tb){

        tb.setVisibility(GONE);

        //Sets the visibility of the entire view if both times are gone
        if(timeButton.getVisibility() == GONE && timeButtonHalf.getVisibility() == GONE){
            setVisibility(GONE);
        }
    }

    //endregion
}
