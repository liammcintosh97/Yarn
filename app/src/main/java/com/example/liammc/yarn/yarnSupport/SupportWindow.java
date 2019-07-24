package com.example.liammc.yarn.yarnSupport;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.networking.Mailer;
import com.example.liammc.yarn.utility.CompatibilityTools;

import java.util.UUID;

public class SupportWindow {

    private static String TAG = "SupportWindow";
    private LocalUser localUser;
    private Mailer mailer;

    //UI
    EditText messageEditText;
    Button submitButton;
    Button cancelButton;

    //Window
    private final ViewGroup parentViewGroup;
    public static PopupWindow window;
    public View mainView;

    public SupportWindow(Activity activity, ViewGroup _parent){
        this.parentViewGroup = _parent;
        this.localUser =  LocalUser.getInstance();
        this.mailer =  new Mailer("Yarn Support Case " + UUID.randomUUID().toString());

        this.initPopup(activity);
        this.initUI(activity);
    }

    //region Init

    private void initPopup(Activity activity) {

        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mainView = inflater.inflate(R.layout.support_window,parentViewGroup,false);

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
        mailer.send(activity,messageEditText.getText().toString());
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


}
