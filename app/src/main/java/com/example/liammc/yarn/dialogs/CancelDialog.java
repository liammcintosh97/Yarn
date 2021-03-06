package com.example.liammc.yarn.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.util.Log;

import com.example.liammc.yarn.planner.EventElement;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.Recorder;

public class CancelDialog extends DialogFragment {
    /*This warning Dialog is used for warning the User to cancelling Chats*/

    private final String TAG = "CancelDialog";
    private EventElement eventElement;
    private final String message = "Are you sure you want to cancel this chat? It'll likely " +
            "disadvantage the other person";

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);

        setPositiveButton(builder);
        setNegativeButton(builder);

        return builder.create();
    }

    //region Init

    public void init(EventElement _eventElement){
        eventElement = _eventElement;
    }

    //endregion

    //region Public Methods
    public boolean dissmissDialog(){
        /*Dismisses the Dialog*/

        Dialog dialog = getDialog();

        if( dialog != null && getDialog().isShowing()){
            dismiss();
            return true;
        }
        return false;
    }
    //endregion

    //region Private Methods

    private void setPositiveButton(AlertDialog.Builder builder){
        /*Sets the action of the dialog's positive button*/

        builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String chatId = getArguments().getString("chatID");
                Recorder recorder = Recorder.getInstance();
                Chat chat = recorder.getRecordedChat(chatId);

                Log.d(TAG,String.valueOf(chatId));

                if(chatId == null || chatId.isEmpty()){
                    Log.e(TAG,"Unable to cancel chat - chatID is null");
                    return;
                }

                eventElement.onVerifyCancelPress(chat);
            }
        });
    }

    private void setNegativeButton(AlertDialog.Builder builder){
        /*Sets the action of the dialog's positive button*/

        builder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });
    }

    //endregion
}
