package com.example.liammc.yarn.yarnPlace;

import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.time.DateDialog;
import com.example.liammc.yarn.time.DurationDialog;
import com.example.liammc.yarn.time.TimeDialog;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.example.liammc.yarn.utility.DateTools;

import java.util.HashMap;
import java.util.Locale;


public class ChatCreator {
    /*The Chat Creator is a firebaseUser interface for creating new chats. It is is used in conjunction with
    the Yarn Place Object*/

    private final String TAG = "ChatCreator";
    private final MapsActivity mapsActivity;

    public PopupWindow window;
    public TimeDialog timePicker;
    public DateDialog datepicker;
    public DurationDialog durationPicker;
    public View mChatCreatorView;

    String localUserID;
    private YarnPlace yarnPlace;

    //UI
    private final ViewGroup parentViewGroup;
    TextView placeName;
    Button dateButton;
    Button timeButton;
    Button durationButton;
    Button createChatButton;

    //Chat details
    public String chatPlaceID;
    public String chatPlaceName;
    public String chatPlaceAddress;

    public ChatCreator(MapsActivity mapsActivity, String localUserID, YarnPlace _yarnPlace) {
        this.parentViewGroup = mapsActivity.findViewById(R.id.constraintLayout);
        this.mapsActivity = mapsActivity;
        this.localUserID = localUserID;
        this.yarnPlace = _yarnPlace;

        init();
    }

    //region init

    private void init(){
        initWindow();
        initUIReferences();
        initButtons();
    }

    private void initWindow() {
        /*This method initializes the window for the Chat Creator*/

        // Initialize a new instance of LayoutInflater service
        mChatCreatorView = inflate(R.layout.popup_chat_creator);

        // Initialize a new instance of popup window
        double width =  ConstraintLayout.LayoutParams.MATCH_PARENT  ;
        double height = ConstraintLayout.LayoutParams.MATCH_PARENT  ;

        window = new PopupWindow(mChatCreatorView, (int) width, (int) height,true);
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.setOutsideTouchable(true);
        window.update();

        CompatibilityTools.setPopupElevation(window,5.0f);
    }

    private void initUIReferences(){
        /*This method gets the UI references from the layout*/

        placeName = mChatCreatorView.findViewById(R.id.placeName);
        dateButton = mChatCreatorView.findViewById(R.id.dateButton);
        timeButton = mChatCreatorView.findViewById(R.id.timeButton);
        durationButton = mChatCreatorView.findViewById(R.id.durationButton);
        createChatButton = mChatCreatorView.findViewById(R.id.createChatButton);
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

    public void show() {
        /*This method shows the Chat Creator to the firebaseUser*/
        init();

        chatPlaceName = yarnPlace.placeMap.get("name");
        chatPlaceID = yarnPlace.placeMap.get("id");
        chatPlaceAddress = yarnPlace.placeMap.get("address");

        updateUI(chatPlaceName);

        window.showAtLocation(parentViewGroup, Gravity.CENTER, 0, 0);
    }

    public void dismiss() {
        /*This method dismisses the Chat Creator*/

        if(window != null && window.isShowing()) window.dismiss();
    }

    //endregion

    //region Private Methods

    private View inflate(int layoutID) {
        /*Inflates the given layout ID*/

        LayoutInflater inflater = mapsActivity.getLayoutInflater();
        return inflater.inflate(layoutID,null);
    }

    //endregion
}


