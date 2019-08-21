package com.example.liammc.yarn.yarnSupport;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.authentication.Authenticator;
import com.example.liammc.yarn.core.YarnWindow;
import com.example.liammc.yarn.interfaces.AuthListener;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.example.liammc.yarn.utility.ErrorManager;

import java.io.IOException;

public class EmailUpdator extends YarnWindow {

    private static String TAG = "EmailUpdator";
    private LocalUser localUser;
    private Authenticator authenticator;

    //UI
    EditText passwordInput;
    EditText newEmailInput;
    EditText confirmNewEmailInput;
    Button submitButton;
    Button cancelButton;

    public EmailUpdator(Activity _activity, ViewGroup _parent,double widthM, double heightM){
        super(_activity,_parent,R.layout.window_email_updator,widthM,heightM);

        this.localUser =  LocalUser.getInstance();
        this.authenticator =  new Authenticator(this.localUser.firebaseAuth);

        this.initUI(_activity);
    }

    //region Init

    private void initUI(Activity activity) {
        /*This method initializes the Phone auth window UI*/

        passwordInput =  getContentView().findViewById(R.id.emailInput);
        newEmailInput =  getContentView().findViewById(R.id.newEmailInput);
        confirmNewEmailInput =  getContentView().findViewById(R.id.confirmNewEmailInput);

        CompatibilityTools.setPasswordAutoFill(passwordInput);
        CompatibilityTools.setEmailAutoFill(newEmailInput);
        CompatibilityTools.setEmailAutoFill(confirmNewEmailInput);

        initButtons(activity);
    }

    private void initButtons(final Activity activity){
        /*This method initializes the phone auth buttons*/

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

        //Check if all the fields are full
        if(!validateEmptyFields()){
            Toast.makeText(activity,"Please fill in all fields",Toast.LENGTH_SHORT).show();
            return;
        }

        final String newEmail = newEmailInput.getText().toString();
        String confirmEmail = confirmNewEmailInput.getText().toString();

        if(!validateSameEmail(newEmail,confirmEmail)){
            Toast.makeText(activity,"Emails are not the same",Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            //Validate the new password
            ErrorManager.validateEmail(newEmail);

            //log the user
            authenticator.login(activity, localUser.email, passwordInput.getText().toString()
                    , new AuthListener() {
                        @Override
                        public void onAuth() {
                            //If the user logged in correctly updateInfoWindow their password
                            localUser.updator.updateUserEmail(newEmail
                                    , new AuthListener() {
                                        @Override
                                        public void onAuth() {
                                            //The password updated so close the Password Resetter
                                            Toast.makeText(activity,"Updated Email - Please check "
                                                            + newEmail + " to validate your new email"
                                                    ,Toast.LENGTH_LONG).show();
                                            dismiss();
                                        }

                                        @Override
                                        public void onError(String message) {
                                            //The password updateInfoWindow failed
                                            Toast.makeText(activity,message
                                                    ,Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                        @Override
                        public void onError(String message) {
                            //The user couldn't log in so alert them
                            Toast.makeText(activity,message
                                    ,Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch(IOException e){
            Toast.makeText(activity,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void onCancelSubmit(){
        dismiss();
    }

    //endregion

    //region private Methods

    private boolean validateEmptyFields(){

        if(passwordInput.getText().toString().equals("")||
                newEmailInput.getText().toString().equals("")||
                confirmNewEmailInput.getText().toString().equals("")) return false;

        return true;
    }

    private boolean validateSameEmail(String email1,  String email2){

        if(email1.equals(email1))return true;

        return false;
    }

    //endregion

}
