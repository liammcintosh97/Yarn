package com.example.liammc.yarn.time;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialogFragment;

public class DurationDialog extends TimeDurationPickerDialogFragment {
    /*This dialog class is used for picking a duration*/

    public long milliSeconds;

    @Override
    protected long getInitialDuration() {
        /*Gets the initial duration and sets the internal milliSeconds duration variable*/

        long initialDuration = 15 * 60 * 1000;
        milliSeconds = initialDuration;

        return initialDuration;
    }

    @Override
    protected int setTimeUnits() {
        /*Sets the format of the duration*/
        return TimeDurationPicker.HH_MM;
    }

    @Override
    public void onDurationSet(TimeDurationPicker view, long _milliSeconds) {
        /*Called when the firebaseUser has set the duration. It sets the internal milliSeconds duration
        * variable */
        milliSeconds = _milliSeconds;
    }

}
