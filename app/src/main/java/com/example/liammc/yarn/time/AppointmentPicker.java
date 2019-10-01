package com.example.liammc.yarn.time;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.liammc.yarn.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppointmentPicker extends LinearLayout {

    private static String TAG =  "AppointmentPicker";
    private static long weekMilli = 604800000;

    ArrayList<AppointmentElement> appointments = new ArrayList<>();

    DatePicker datePicker;
    TextView timeSelection;
    TextView dateSelection;
    LinearLayout amLayout;
    LinearLayout pmLayout;

    public String selectedTime;
    public String selectedDate;

    public AppointmentPicker(Context _context){
        super(_context);

        this.initView();
        this.initUI();
    }

    public AppointmentPicker(Context _context, AttributeSet _attrs){
        super(_context,_attrs);

        this.initView();
        this.initUI();
    }

    public AppointmentPicker(Context _context, AttributeSet _attrs, int _defStyle){
        super(_context,_attrs,_defStyle);

        this.initView();
        this.initUI();
    }

    //region init

    private void initView(){
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.picker_appointment,this,true);
    }

    private void initUI(){

        LinearLayout main  = (LinearLayout) getChildAt(0);

        LinearLayout selectionLayout = (LinearLayout) main.getChildAt(1);

        dateSelection =  (TextView) ((LinearLayout)selectionLayout.getChildAt(0)).getChildAt(0);
        timeSelection =  (TextView) ((LinearLayout)selectionLayout.getChildAt(0)).getChildAt(1);
        datePicker = (DatePicker) main.getChildAt(0);

        LinearLayout svl = (LinearLayout) main.getChildAt(2);
        ScrollView amsv = (ScrollView) svl.getChildAt(0);
        ScrollView pmsv = (ScrollView) svl.getChildAt(1);
        amLayout = (LinearLayout) amsv.getChildAt(0);
        pmLayout = (LinearLayout) pmsv.getChildAt(0);

        initDatePicker();
        initAppointmentTimes();
    }

    private void initDatePicker(){

        Calendar date = Calendar.getInstance();
        // reset hour, minutes, seconds and millis
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        datePicker.init(date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);

                String date = dayOfMonth + "/" + month + "/" + year;
                selectedDate =  date;
                dateSelection.setText(date);
            }
        });

        datePicker.setMinDate(date.getTimeInMillis());
        datePicker.setMaxDate(date.getTimeInMillis()+ weekMilli);
    }

    private void initAppointmentTimes(){

        for(int i = 0; i < amLayout.getChildCount(); i++){
            appointments.add((AppointmentElement) amLayout.getChildAt(i));
        }

        for(int i = 0; i < pmLayout.getChildCount(); i++){
            appointments.add((AppointmentElement) pmLayout.getChildAt(i));
        }

        updateAppointmentTimes();
    }

    //endregion

    //region Public Methods

    public boolean validSelection(){

        String dateDefault = getContext().getResources().getString(R.string.date_default);
        String timeDefault = getContext().getResources().getString(R.string.time_default);
        String selectionTime = timeSelection.getText().toString();
        String selectionDate = dateSelection.getText().toString();


        if(selectionTime.equals(timeDefault) || selectionDate.equals(dateDefault)) return false;
        else return true;

    }

    public void updateAppointmentTimes(){

        for(int i = 0; i < appointments.size(); i++){

            AppointmentElement e = appointments.get(i);

            if(e.validTime(Calendar.getInstance(Locale.getDefault()),null,null)){
                e.setButtons(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button b = (Button)v;
                        selectedTime  = b.getText().toString();
                        timeSelection.setText(selectedTime);
                    }
                });
            }

        }
    }

    //endregion

}
