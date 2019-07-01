package com.example.liammc.yarn.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.MapsActivity;

public class RateDialog extends DialogFragment {

    private final String TAG = "RateDialog";
    private Chat chat;
    private YarnUser otherUser;
    private AlertDialog.Builder builder;
    private EditText input;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        builder = new AlertDialog.Builder(getActivity());

        setEditText();
        setPositiveButton();
        setNegativeButton();
        builder.setMessage("The chat is finished! how do you rate " + otherUser.userName + " ?");

        return builder.create();
    }

    //region Init
    public void init(Chat _chat, YarnUser _otherUser){
        chat = _chat;
        chat.updator.initChangeListener(getActivity());
        otherUser = _otherUser;
    }

    //endregion

    //region Private Methods

    private void setEditText(){
        // Set up the input
        input = new EditText(getActivity());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_FLAG_SIGNED);
        builder.setView(input);
    }

    private void setPositiveButton(){
        /*Sets the action of the dialog's positive button*/

        builder.setPositiveButton("rate", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                try {
                    otherUser.updator.addUserRating(LocalUser.getInstance().userID,
                            Integer.parseInt(input.getText().toString()));
                }catch (Exception e){
                    Log.e(TAG,"Couldn't pass rating");
                }

                if(chat != null) chat.removeChat();
                dismiss();

                Intent intent = new Intent(getActivity(), MapsActivity.class);
                getActivity().startActivity(intent);
            }
        });
    }

    private void setNegativeButton(){
        /*Sets the action of the dialog's positive button*/

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                chat.removeChat();
                dismiss();

                Intent intent = new Intent(getActivity(), MapsActivity.class);
                getActivity().startActivity(intent);
            }
        });
    }

    //endregion

}
