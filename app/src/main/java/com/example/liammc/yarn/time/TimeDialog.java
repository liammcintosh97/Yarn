package com.example.liammc.yarn.time;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import java.util.Calendar;

public  class TimeDialog extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    /*This dialog class is used for picking a date*/

    public int hour;
    public int minute;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    //region Public Methods

    public void onTimeSet(TimePicker view, int hourOfDay, int _minute) {
        /*Sets the internal variables when the time is set by the firebaseUser*/

        hour = hourOfDay;
        minute = _minute;

    }

    //endregion

}

