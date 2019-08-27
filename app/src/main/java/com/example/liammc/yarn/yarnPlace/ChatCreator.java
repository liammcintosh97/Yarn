package com.example.liammc.yarn.yarnPlace;

import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.core.YarnWindow;
import com.example.liammc.yarn.time.DateDialog;
import com.example.liammc.yarn.time.DurationDialog;
import com.example.liammc.yarn.time.TimeDialog;
import com.example.liammc.yarn.utility.DateTools;

import java.util.HashMap;
import java.util.Locale;


public class ChatCreator extends YarnWindow {
    /*The Chat Creator is used  by the user when they want to create new chats. It is is used in
    conjunction with the Yarn Place Object and it's Info Window*/

    private final String TAG = "ChatCreator";
    private final MapsActivity mapsActivity;

    public TimeDialog timePicker;
    public DateDialog datepicker;
    public DurationDialog durationPicker;

    String localUserID;
    private YarnPlace yarnPlace;

    //TODO stop users from creating chats in the past
    //TODO limit users to selecting meet time ever hour or half past
    //TODO limit user to selecting duration from a set list
    //TODO implement appointment picker dialog
    // (http://www.dappsforpc.site/download-time-slot-picker-for-android-for-pc-windows-and-mac/com.github.irshulx.slotpicker.html)
    //TODO app crashes when user doesn't enter details and presses create

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

    public ChatCreator(MapsActivity _mapsActivity,ViewGroup _parent, String localUserID,double widthM
                       ,double heightM,YarnPlace _yarnPlace) {

        super(_mapsActivity,_parent,R.layout.window_chat_creator,widthM,heightM);

        this.mapsActivity = _mapsActivity;
        this.localUserID = localUserID;
        this.yarnPlace = _yarnPlace;

        init();
    }

    //region init

    private void init(){
        initUIReferences();
        initButtons();
    }

    private void initUIReferences(){
        /*This method gets the UI references from the layout*/

        placeName = getContentView().findViewById(R.id.placeName);
        dateButton = getContentView().findViewById(R.id.dateButton);
        timeButton = getContentView().findViewById(R.id.timeButton);
        durationButton = getContentView().findViewById(R.id.durationButton);
        createChatButton = getContentView().findViewById(R.id.createChatButton);
    }

    private void initButtons(){
        /*This method links the button's clicks to their respective methods*/

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
    /*This region contains all the button methods for the Chat Creator*/

    private void onPickTimePressed() {
        /*This method launches the time picker*/

        timePicker = new TimeDialog();
        timePicker.show(mapsActivity.getSupportFragmentManager(), "timePicker");
    }

    private void onPickDatePressed() {
        /*This method launches the date picker*/

        datepicker = new DateDialog();
        datepicker.show(mapsActivity.getSupportFragmentManager(),"datePicker");
    }

    private void onPickDurationPressed() {
        /*This method launches the duration picker*/

        durationPicker = new DurationDialog();
        durationPicker.show(mapsActivity.getFragmentManager(),"durationPicker");
    }

    private void onCreateChatPressed() {
        /*This method confirms the input and creates a chat from it*/

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

        yarnPlace.yarnPlaceUpdater.removeChildListener();

        HashMap<String,String> chatMap = Chat.buildChatMap(localUserID,date,time,duration);

        new Chat(yarnPlace, chatMap, new Chat.ChatReadyListener(){
            @Override
            public void onReady(Chat chat) {

                Log.d(TAG,"Chat is ready");
                yarnPlace.yarnPlaceUpdater.addToSystem(mapsActivity,chat);
                dismiss();
            }
        });
    }

    //endregion

    //region Public Methods

    public void updateUI(String _placeName) {
        /*This method updates the Chat Creator UI*/

        placeName.setText(_placeName);
    }

    @Override
    public void show(int gravity) {
        /*This method shows the Chat Creator to the firebaseUser*/
        init();

        chatPlaceName = yarnPlace.placeMap.get("name");
        chatPlaceID = yarnPlace.placeMap.get("id");
        chatPlaceAddress = yarnPlace.placeMap.get("address");

        updateUI(chatPlaceName);

        super.show(gravity);
    }

    //endregion
}


