package com.example.liammc.yarn.chats;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.authentication.CreateAccountActivity;
import com.example.liammc.yarn.core.YarnWindow;

public class ProfilePictureWindow extends YarnWindow {

    //UI
    private static final int layoutID = R.layout.window_profile_clarification;
    private Button okButton;

    public ProfilePictureWindow(Activity _activity, ViewGroup _parent, double widthM, double heightM) {
        super(_activity, _parent, layoutID, widthM, heightM);
        this.initUI(_activity);
    }

    //region Init

    private void initUI(final Activity activity){
        okButton =  getContentView().findViewById(R.id.submitButton);
        okButton.setText(activity.getResources().getString(R.string.ok_button));

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOKPressed(activity);
            }
        });
    }

    //endregion

    //region Button Methods

    private void onOKPressed(Activity _activity){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        _activity.startActivityForResult(intent,CreateAccountActivity.CAMERA_PIC_REQUEST);
    }

    //endregion
}
