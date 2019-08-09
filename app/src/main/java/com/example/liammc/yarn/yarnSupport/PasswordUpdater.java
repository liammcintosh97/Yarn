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

public class PasswordUpdater extends YarnWindow {
    //The Password Updator is used when the the user wants to updateInfoWindow their password

    private static String TAG = "PasswordUpdater";
    private LocalUser localUser;
    private Authenticator authenticator;

    //UI
    private final static int layoutID = R.layout.window_password_update;
    private EditText passwordInput;
    private EditText newPasswordInput;
    private EditText confirmNewPasswordInput;
    private Button submitButton;
    private Button cancelButton;


    public PasswordUpdater(Activity _activity, ViewGroup _parent){
        super(_activity,_parent,layoutID);

        this.localUser =  LocalUser.getInstance();
        this.authenticator =  new Authenticator(this.localUser.firebaseAuth);

        this.initUI(_activity);
    }

    //region Init

    private void initUI(Activity activity) {
        /*This method initializes the Password Updater window UI*/

        passwordInput =  getContentView().findViewById(R.id.emailInput);
        newPasswordInput =  getContentView().findViewById(R.id.newPasswordInput);
        confirmNewPasswordInput =  getContentView().findViewById(R.id.confirmNewPasswordInput);

        CompatibilityTools.setPasswordAutoFill(passwordInput);
        CompatibilityTools.setPasswordAutoFill(newPasswordInput);
        CompatibilityTools.setPasswordAutoFill(confirmNewPasswordInput);

        initButtons(activity);
    }

    private void initButtons(final Activity activity){
        /*This method initializes the Password Updater window buttons*/

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
        //This method submits the new password when the user clicks the submit button

        //Check if all the fields are full
        if(!validateEmptyFields()){
            Toast.makeText(activity,"Please fill in all fields",Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            //Validate the new password
            ErrorManager.validatePassword(newPasswordInput.getText().toString(),
                    confirmNewPasswordInput.getText().toString());

            //log in the user
            authenticator.login(activity, localUser.email, passwordInput.getText().toString()
                    , new AuthListener() {
                        @Override
                        public void onAuth() {
                            //If the user logged in correctly updateInfoWindow their password
                            localUser.updator.updatePassword(newPasswordInput.getText().toString()
                                    , new AuthListener() {
                                        @Override
                                        public void onAuth() {
                                            //The password updated so close the Password Resetter
                                            Toast.makeText(activity,"Updated password"
                                                    ,Toast.LENGTH_SHORT).show();
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
        /*This method dismisses the window when the user presses the cancel button*/
        dismiss();
    }

    //endregion

    //region private Methods

    private boolean validateEmptyFields(){
        //This method validates all the password fields
        if(passwordInput.getText().toString().equals("")||
        newPasswordInput.getText().toString().equals("")||
        confirmNewPasswordInput.getText().toString().equals("")) return false;

        return true;
    }

    //endregion
}
