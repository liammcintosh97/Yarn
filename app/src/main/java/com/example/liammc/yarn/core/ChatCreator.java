package com.example.liammc.yarn.core;

import android.app.Activity;
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
import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.example.liammc.yarn.time.DateDialog;
import com.example.liammc.yarn.time.DurationDialog;
import com.example.liammc.yarn.time.TimeDialog;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.example.liammc.yarn.utility.DateTools;

import java.util.HashMap;
import java.util.Locale;


public class ChatCreator
{
    /*The Chat Creator is a firebaseUser interface for creating new chats. It is is used in conjunction with
    the Yarn Place Object*/

    private final String TAG = "ChatCreator";
    //private MapsActivity mapsActivity;
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

    public ChatCreator(FragmentActivity activity, ViewGroup _parent, String localUserID)
    {
        this.parentViewGroup = _parent;

        this.localUserID = localUserID;

        initWindow(activity);
        initUI(activity);
    }

    //region init

    private void initWindow(Activity activity) {
        /*This method initializes the window for the Chat Creator*/

        // Initialize a new instance of LayoutInflater service
        mChatCreatorView = inflate(activity,R.layout.popup_chat_creator,parentViewGroup);

        // Initialize a new instance of popup window
        double width =  ConstraintLayout.LayoutParams.MATCH_PARENT  ;
        double height = ConstraintLayout.LayoutParams.MATCH_PARENT  ;

        window = new PopupWindow(mChatCreatorView, (int) width, (int) height,true);
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.setOutsideTouchable(true);
        window.update();

        CompatibilityTools.setPopupElevation(window,5.0f);
    }

    private void initUI(FragmentActivity activity) {
        /*This method initializes the Chat Creator's UI by getting the references and initializing
        the buttons
         */
        initUIReferences();
        initButtons(activity);
    }

    private void initUIReferences(){
        /*This method gets the UI references from the layout*/

        placeName = mChatCreatorView.findViewById(R.id.placeName);
        dateButton = mChatCreatorView.findViewById(R.id.dateButton);
        timeButton = mChatCreatorView.findViewById(R.id.timeButton);
        durationButton = mChatCreatorView.findViewById(R.id.durationButton);
        createChatButton = mChatCreatorView.findViewById(R.id.createChatButton);
    }

    private void initButtons(final FragmentActivity activity){
        /*This method links the button's clicks to their respective methods*/

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickDatePressed(activity);
            }
        });
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickTimePressed(activity);
            }
        });
        durationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickDurationPressed(activity);
            }
        });
        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCreateChatPressed(activity);
            }
        });
    }

    //endregion

    //region Button Methods
    /*This region contains all the button methods for the Chat Creator*/

    private void onPickTimePressed(FragmentActivity activity) {
        /*This method launches the time picker*/

        timePicker = new TimeDialog();
        timePicker.show(activity.getSupportFragmentManager(), "timePicker");
    }

    private void onPickDatePressed(FragmentActivity activity) {
        /*This method launches the date picker*/

        datepicker = new DateDialog();
        datepicker.show(activity.getSupportFragmentManager(),"datePicker");
    }

    private void onPickDurationPressed(FragmentActivity activity) {
        /*This method launches the duration picker*/

        durationPicker = new DurationDialog();
        durationPicker.show(activity.getFragmentManager(),"durationPicker");
    }

    private void onCreateChatPressed(final FragmentActivity activity) {
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

        Chat chat  = new Chat(yarnPlace, chatMap, new Chat.ChatReadyListener(){
            @Override
            public void onReady(Chat chat) {

                Log.d(TAG,"Chat is ready");
                chat.yarnPlace.addChat(chat);
                Recorder.getInstance().recordChat(chat);
                chat.yarnPlace.yarnPlaceUpdater.initChildListener(activity);
                chat.yarnPlace.yarnPlaceUpdater.addChildListener();

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

    public void show(YarnPlace _yarnPlace) {
        /*This method shows the Chat Creator to the firebaseUser*/

        yarnPlace = _yarnPlace;

        chatPlaceName = _yarnPlace.placeMap.get("name");
        chatPlaceID = _yarnPlace.placeMap.get("id");
        chatPlaceAddress = _yarnPlace.placeMap.get("address");

        updateUI(chatPlaceName);

        window.showAtLocation(parentViewGroup, Gravity.CENTER, 0, 0);
    }

    public void dismiss() {
        /*This method dismisses the Chat Creator*/

        if(window != null && window.isShowing()) window.dismiss();
    }
    //endregion

    //region Private Methods

    private View inflate(Activity activity, int layoutID, ViewGroup parent ) {
        /*Inflates the given layout ID*/

        LayoutInflater inflater = activity.getLayoutInflater();
        return inflater.inflate(layoutID,parent);
    }

    //endregion
}


