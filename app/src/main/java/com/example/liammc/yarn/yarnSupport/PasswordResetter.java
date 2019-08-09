package com.example.liammc.yarn.yarnSupport;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.authentication.Authenticator;
import com.example.liammc.yarn.core.YarnWindow;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.example.liammc.yarn.utility.ErrorManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

public class PasswordResetter extends YarnWindow {
    private static String TAG = "PasswordUpdater";
    private LocalUser localUser;
    private Authenticator authenticator;

    //UI
    private static final int layotuID  = R.layout.window_password_reset;
    EditText emailInput;
    Button submitButton;
    Button cancelButton;


    public PasswordResetter(Activity _activity, ViewGroup _parent){
        super(_activity,_parent,layotuID);

        this.localUser =  LocalUser.getInstance();
        this.authenticator =  new Authenticator(this.localUser.firebaseAuth);

        this.initUI(_activity);
    }

    //region Init

    private void initUI(Activity activity) {
        /*This method initializes the Password Resetter window UI*/

        emailInput =  getContentView().findViewById(R.id.emailInput);

        CompatibilityTools.setEmailAutoFill(emailInput);

        initButtons(activity);
    }

    private void initButtons(final Activity activity){
        /*This method initializes the Password Resetter  buttons*/

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
        /*This method runs when the user presses the submit button. It sends a Password reset email
        to the email that the user inputs
         */

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
        /*This method runs when the user presses the cancel button. It dismisses the window
         */
        dismiss();
    }

    //endregion

    //region private Methods

    private boolean validateEmptyFields(){
        /*This method validates the required fields*/
        if(emailInput.getText().toString().equals("")) return false;

        return true;
    }

    //endregion
}
