package com.example.liammc.yarn.yarnSupport;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.core.YarnWindow;
import com.example.liammc.yarn.networking.Mailer;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.UUID;

public class ReporterWindow extends YarnWindow {
    /*The reporter window is used to report other users after a chat is completed*/

    private final String TAG = "ReporterWindow";
    private final YarnUser reportedUser;
    private final YarnUser localUser;
    public final static int REPORT_REQUEST_CODE = 0;


    //UI
    private final static int layoutID = R.layout.window_reporter;
    private EditText messageEditText;
    private Button submitButton;
    private Button cancelButton;

    public ReporterWindow(Activity _activity, ViewGroup _parent, LocalUser _localUser
            ,YarnUser _reportedUser, double widthM
            , double heightM){
        super(_activity,_parent,layoutID,widthM,heightM);
        this.reportedUser =  _reportedUser;
        this.localUser = _localUser;

        this.initUI(_activity);
    }

    //region Init
    private void initUI(Activity activity) {
        /*This method initializes the reporter window UI*/
        messageEditText =  getContentView().findViewById(R.id.message);
        initButtons(activity);
    }

    private void initButtons(final Activity activity){
        /*This method initializes the reporter window buttons*/

        submitButton =  getContentView().findViewById(R.id.submitButton);
        cancelButton =  getContentView().findViewById(R.id.cancelButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitClick(activity);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelSubmit();
            }
        });
    }
    //endregion

    //region Button Methods

    private void onSubmitClick(final Activity activity){
        /*This method is called when the user presses the submit buttons.
        It submits the written message by passing the string to an instance of the
        the mailer class. It also flags the user
         */

        Mailer mailer =  new Mailer("Yarn User Report - " + localUser.userID + " - "
                + reportedUser.userID + " - " + UUID.randomUUID().toString());
        mailer.send(activity,messageEditText.getText().toString(),REPORT_REQUEST_CODE);

        flagUser(reportedUser);
    }

    private void onCancelSubmit(){
        /*This method dismisses the window when the user presses the cancel button*/
        dismiss();
    }

    //endregion

    //region Public Methods

    public void flagUser(YarnUser user){
        /*This method increments the flags variable that's in the user's part of the database*/

        //Get the database reference for the user
        DatabaseReference flagsRef =  user.userDatabaseReference.child("flags");

        flagsRef.setValue(user.flags + 1, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                //Log the system to the outcome of the value updateInfoWindow
                if(databaseError == null){
                    Log.d(TAG,"User flagging was successful");
                }
                else Log.e(TAG,"Flagging of the user failed - " + databaseError.getMessage());
            }
        });
    }

    //endregion
}
