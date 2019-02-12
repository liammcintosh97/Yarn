package com.example.liammc.yarn.Events;

import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.text.Layout;
import android.view.Gravity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.core.ChatRecorder;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.time.DateDialog;
import com.example.liammc.yarn.time.DurationDialog;
import com.example.liammc.yarn.time.TimeDialog;
import com.example.liammc.yarn.utility.CompatabiltyTools;
import com.example.liammc.yarn.utility.DateTools;

import java.util.Locale;


public class ChatCreator
{
    private MapsActivity mapsActivity;
    private final ViewGroup parentViewGroup;

    public PopupWindow window;
    public TimeDialog timePicker;
    public DateDialog datepicker;
    public DurationDialog durationPicker;
    public View mChatCreatorView;

    String localUserID;
    public YarnPlace yarnPlace;

    //UI
    TextView placeName;
    Button dateButton;
    Button timeButton;
    Button durationButton;
    Button createChatButton;


    //Chat details
    public String chatPlaceID;
    public String chatPlaceName;
    public String chatPlaceAddress;

    public ChatCreator(MapsActivity _mapsActivity, ViewGroup _parent, String localUserID)
    {
        this.parentViewGroup = _parent;
        this.mapsActivity = _mapsActivity;

        this.localUserID = localUserID;

        SetUpChatCreatorPopUp();
        SetUpChatUI();
    }

    //region Set Up

    private void SetUpChatCreatorPopUp()
    {
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) mapsActivity
                .getSystemService(mapsActivity.LAYOUT_INFLATER_SERVICE);
        mChatCreatorView = inflater.inflate(R.layout.popup_chat_creator,parentViewGroup,
                false);

        // Initialize a new instance of popup window
        double width =  ConstraintLayout.LayoutParams.MATCH_PARENT  ;
        double height = ConstraintLayout.LayoutParams.MATCH_PARENT  ;

        window = new PopupWindow(mChatCreatorView, (int) width, (int) height,true);
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.setOutsideTouchable(true);
        window.update();

        CompatabiltyTools.setPopupElevation(window,5.0f);
    }

    private void SetUpChatUI()
    {
        placeName = mChatCreatorView.findViewById(R.id.placeName);
        dateButton = mChatCreatorView.findViewById(R.id.dateButton);
        timeButton = mChatCreatorView.findViewById(R.id.timeButton);
        durationButton = mChatCreatorView.findViewById(R.id.durationButton);
        createChatButton = mChatCreatorView.findViewById(R.id.createChatButton);

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickDatePressed();
            }
        });
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickTimePressed();
            }
        });
        durationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickDurationPressed();
            }
        });
        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCreateChatPressed();
            }
        });
    }

    //endregion

    //region Button Methods

    private void onPickTimePressed()
    {
        timePicker = new TimeDialog();
        timePicker.show(mapsActivity.getSupportFragmentManager(), "timePicker");
    }

    private void onPickDatePressed()
    {
        datepicker = new DateDialog();
        datepicker.show(mapsActivity.getSupportFragmentManager(),"datePicker");
    }

    private void onPickDurationPressed()
    {
        durationPicker = new DurationDialog();
        durationPicker.show(mapsActivity.getFragmentManager(),"durationPicker");
    }

    private void onCreateChatPressed()
    {
        int year = datepicker.year;
        int intMonth = datepicker.month + 1;
        int intDay = datepicker.day;
        int hour = timePicker.hour;
        int minute = timePicker.minute;

        //Format duration
        String duration = DateTools.millisToDurationString(Locale.getDefault()
                ,durationPicker.milliSeconds);

        //Format day
        String day;
        if(intDay < 10) {
            day = "0" + intDay;
        }
        else day = String.valueOf(intDay);

        //Format month
        String month;
        if(intMonth < 10) {
            month = "0" + intMonth;
        }else month = String.valueOf(intMonth);

        //Format date
        String date = day + "-" + month + "-" + year;

        //Format time
        String time = hour + ":" + minute;

        Chat chat = new Chat("ChatCreator",localUserID,localUserID,chatPlaceID,
                chatPlaceName, yarnPlace.address,yarnPlace.latLng, yarnPlace.placeType,date
                ,time,duration);

        ChatRecorder.getInstance().recordChat(mapsActivity,chat);

        dissmissChatCreator();
    }

    //endregion

    //region UI

    public void updateUI(String _placeName)
    {
        placeName.setText(_placeName);
    }

    public void showChatCreator(YarnPlace _yarnPlace)
    {
        yarnPlace = _yarnPlace;

        chatPlaceName = _yarnPlace.placeMap.get("name");
        chatPlaceID = _yarnPlace.placeMap.get("id");
        chatPlaceAddress = _yarnPlace.placeMap.get("address");

        updateUI(chatPlaceName);

        window.showAtLocation(parentViewGroup, Gravity.CENTER, 0, 0);
    }

    public void dissmissChatCreator()
    {
        if(window != null && window.isShowing()) window.dismiss();
    }
    //endregion

}


