package com.example.liammc.yarn.time;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialogFragment;

public class DurationDialog extends TimeDurationPickerDialogFragment {

    public long milliSeconds;

    @Override
    protected long getInitialDuration() {
        return 15 * 60 * 1000;
    }


    @Override
    protected int setTimeUnits() {
        return TimeDurationPicker.HH_MM;
    }

    @Override
    public void onDurationSet(TimeDurationPicker view, long _milliSeconds)
    {
        milliSeconds = _milliSeconds;
    }


}
