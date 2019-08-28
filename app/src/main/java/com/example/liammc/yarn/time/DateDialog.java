package com.example.liammc.yarn.time;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class DateDialog extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    /*This dialog class is used for picking a Date*/

    public int year;
    public int month;
    public int day;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR) - 18;
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    //region Public Methods

    public void onDateSet(DatePicker view, int _year, int _month, int _day) {
        /*This method is called when the firebaseUser sets the date. It sets the internal year, month and
        day variables
         */
        year = _year;
        month = _month;
        day = _day;
    }

    //endregion

}


