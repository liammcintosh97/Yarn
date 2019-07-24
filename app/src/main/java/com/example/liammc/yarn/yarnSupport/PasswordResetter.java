package com.example.liammc.yarn.yarnSupport;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.authentication.Authenticator;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.example.liammc.yarn.utility.ErrorManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

public class PasswordResetter {
    private static String TAG = "PasswordUpdator";
    private LocalUser localUser;
    private Authenticator authenticator;

    //UI
    EditText emailInput;
    Button submitButton;
    Button cancelButton;

    //Window
    private final ViewGroup parentViewGroup;
    public static PopupWindow window;
    public View mainView;

    public PasswordResetter(Activity activity, ViewGroup _parent){
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
        mainView = inflater.inflate(R.layout.password_reset_window,parentViewGroup,false);

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

        emailInput =  mainView.findViewById(R.id.emailInput);

        CompatibilityTools.setEmailAutoFill(emailInput);

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
            final String email =  emailInput.getText().toString();

            ErrorManager.validateEmail(email);

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Password reset email sent.");
                                Toast.makeText(activity,"Password reset sent to  - " + email
                                        ,Toast.LENGTH_LONG).show();
                                dismiss();
                            }
                            else{
                                Log.e(TAG,"Password reset email failed to send - "
                                        + task.getException());
                                Toast.makeText(activity,"Failed to send password reset email to  - " + email
                                        ,Toast.LENGTH_LONG).show();
                            }
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

        if(emailInput.getText().toString().equals("")) return false;

        return true;
    }

    //endregion
}
