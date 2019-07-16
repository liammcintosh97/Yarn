package com.example.liammc.yarn.authentication;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.interfaces.AuthListener;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.example.liammc.yarn.utility.ErrorManager;

import java.io.IOException;

public class PasswordUpdator {

    private static String TAG = "PasswordUpdator";
    private LocalUser localUser;
    private Authenticator authenticator;

    //UI
    EditText passwordInput;
    EditText newPasswordInput;
    EditText confirmNewPasswordInput;
    Button submitButton;
    Button cancelButton;

    //Window
    private final ViewGroup parentViewGroup;
    public static PopupWindow window;
    public View mainView;

    public PasswordUpdator(Activity activity, ViewGroup _parent){
        this.parentViewGroup = _parent;
        this.localUser =  LocalUser.getInstance();
        this.authenticator =  new Authenticator(this.localUser.firebaseAuth);

        this.initPopup(activity);
        this.initUI(activity);
    }

    //region Init

    private void initPopup(Activity activity) {

        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(R.layout.password_update_window,parentViewGroup,false);

        // Initialize a new instance of popup window
        double width =  ConstraintLayout.LayoutParams.MATCH_PARENT  ;
        double height = ConstraintLayout.LayoutParams.MATCH_PARENT  ;

        window = new PopupWindow(mainView, (int) width, (int) height,true);
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.setOutsideTouchable(true);
        window.update();

        CompatibilityTools.setPopupElevation(window,5.0f);
    }

    private void initUI(Activity activity) {
        /*This method initializes the Phone auth window UI*/

        passwordInput =  mainView.findViewById(R.id.emailInput);
        newPasswordInput =  mainView.findViewById(R.id.newPasswordInput);
        confirmNewPasswordInput =  mainView.findViewById(R.id.confirmNewPasswordInput);

        CompatibilityTools.setPasswordAutoFill(passwordInput);
        CompatibilityTools.setPasswordAutoFill(newPasswordInput);
        CompatibilityTools.setPasswordAutoFill(confirmNewPasswordInput);

        initButtons(activity);
    }

    private void initButtons(final Activity activity){
        /*This method initializes the phone auth buttons*/

        submitButton =  mainView.findViewById(R.id.submitButton);
        cancelButton =  mainView.findViewById(R.id.cancelButton);

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

        try{
            //Validate the new password
            ErrorManager.validatePassword(newPasswordInput.getText().toString(),
                    confirmNewPasswordInput.getText().toString());

            //log the user
            authenticator.login(activity, localUser.email, passwordInput.getText().toString()
                    , new AuthListener() {
                        @Override
                        public void onAuth() {
                            //If the user logged in correctly update their password
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
                                            //The password update failed
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

    //region Public Methods

    public void show() {
        /*Shows the Phone Auth window*/
        window.showAtLocation(parentViewGroup, Gravity.CENTER, 0, 0);
    }

    public void dismiss() {
        /*Dismisses the Phone Auth window*/
        if(window.isShowing()) window.dismiss();
    }

    //endregion

    //region private Methods

    private boolean validateEmptyFields(){

        if(passwordInput.getText().toString().equals("")||
        newPasswordInput.getText().toString().equals("")||
        confirmNewPasswordInput.getText().toString().equals("")) return false;

        return true;
    }

    //endregion
}
