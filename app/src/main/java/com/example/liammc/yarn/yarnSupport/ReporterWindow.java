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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.networking.Mailer;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

public class ReporterWindow {

    private final String TAG = "ReporterWindow";
    private final YarnUser user;

    //UI
    EditText messageEditText;
    Button submitButton;
    Button cancelButton;

    //Window
    private final ViewGroup parentViewGroup;
    public static PopupWindow window;
    public View mainView;

    public ReporterWindow(Activity activity, ViewGroup _parent, YarnUser _user){
        this.parentViewGroup = _parent;
        this.user =  _user;

        this.initPopup(activity);
        this.initUI(activity);
    }

    //region Init

    private void initPopup(Activity activity) {

        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(R.layout.report_window,parentViewGroup,false);

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

        messageEditText =  mainView.findViewById(R.id.messageInput);

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
        Mailer mailer =  new Mailer("Yarn User Report - " + user.userID);
        mailer.send(activity,messageEditText.getText().toString());

        flagUser(user);
    }

    private void onCancelSubmit(){
        dismiss();
    }

    //endregion

    //region Public Methods

    public void flagUser(YarnUser user){

        DatabaseReference flagsRef =  user.userDatabaseReference.child("flags");

        flagsRef.setValue(user.flags + 1, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                if(databaseError == null){
                    Log.d(TAG,"User flagging was successful");
                }
                else Log.e(TAG,"Flagging of the user failed - " + databaseError.getMessage());
            }
        });
    }

    public void show() {
        /*Shows the Phone Auth window*/
        window.showAtLocation(parentViewGroup, Gravity.CENTER, 0, 0);
    }

    public void dismiss() {
        /*Dismisses the Phone Auth window*/
        if(window.isShowing()) window.dismiss();
    }

    //endregion
}
