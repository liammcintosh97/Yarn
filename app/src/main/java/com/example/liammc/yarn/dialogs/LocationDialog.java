package com.example.liammc.yarn.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class LocationDialog extends DialogFragment {

    private final String TAG = "InternetDialog";
    private final String message = "Please enable location Services";

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setCancelable(false);

        return builder.create();
    }

    //region Public Methods

    public void alert(FragmentManager fm, String TAG){
        try{
            setCancelable(false);
            show(fm,TAG);
        }catch(Exception e){
            Log.e(TAG,e.getMessage());
        }
    }

    public void dismissDialog(Activity _activity){
        /*Dismisses the Dialog*/

        try{
            Dialog dialog = getDialog();

            if( !_activity.isFinishing() && dialog != null && dialog.isShowing()){
                dismiss();
            }
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }

    }

    //endregion
}
