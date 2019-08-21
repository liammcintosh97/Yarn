package com.example.liammc.yarn.yarnSupport;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.core.YarnWindow;
import com.example.liammc.yarn.networking.Mailer;

import java.util.UUID;

public class SupportWindow extends YarnWindow {
    //The Support Window is used when the user wishes to gain support about Yarn

    private static String TAG = "SupportWindow";
    private Mailer mailer;

    //UI
    private final static int layoutID = R.layout.window_support;
    EditText messageEditText;
    Button submitButton;
    Button cancelButton;


    public SupportWindow(Activity _activity, ViewGroup _parent,double widthM, double heightM){
        super(_activity,_parent,layoutID,widthM,heightM);

        this.mailer =  new Mailer("Yarn Support Case " + UUID.randomUUID().toString());
        this.initUI(_activity);
    }

    //region Init

    private void initUI(Activity activity) {
        /*This method initializes the Support window UI*/

        messageEditText =  getContentView().findViewById(R.id.messageInput);
        initButtons(activity);
    }

    private void initButtons(final Activity activity){
        /*This method initializes the Support buttons*/

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
        /*This method runs when the user presses the submit button. It passes the string to an
        instance of the mailer class*/
        mailer.send(activity,messageEditText.getText().toString(),-1);
    }

    private void onCancelSubmit(){
        //This method runs when the user presses the cancel button. It dismisses the window
        dismiss();
    }

    //endregion

}
