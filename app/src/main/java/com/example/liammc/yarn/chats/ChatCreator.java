package com.example.liammc.yarn.chats;

import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.core.YarnWindow;
import com.example.liammc.yarn.time.AppointmentPicker;
import com.example.liammc.yarn.time.DateDialog;
import com.example.liammc.yarn.time.DurationDialog;
import com.example.liammc.yarn.time.TimeDialog;
import com.example.liammc.yarn.utility.DateTools;
import com.example.liammc.yarn.yarnPlace.YarnPlace;

import java.util.HashMap;
import java.util.Locale;


public class ChatCreator extends YarnWindow {
    /*The Chat Creator is used  by the user when they want to create new chats. It is is used in
    conjunction with the Yarn Place Object and it's Info Window*/

    private final String TAG = "ChatCreator";
    private final MapsActivity mapsActivity;
    private final long minuteMilli = 60000;

    public AppointmentPicker appointmentPicker;
    public Spinner durationSpinner;

    String localUserID;
    private YarnPlace yarnPlace;

    //TODO stop users from creating chats in the past and outside of business hours
    //TODO date is being formatted incorrectly. Shows as a month before

    //UI
    TextView placeName;
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

        appointmentPicker =  getContentView().findViewById(R.id.appointmentPicker);
        placeName = getContentView().findViewById(R.id.placeName);
        createChatButton = getContentView().findViewById(R.id.createChatButton);
        durationSpinner =  getContentView().findViewById(R.id.durationSpinner);

        String[] durations = {"15 mins","30 mins", "60 mins"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                mapsActivity, android.R.layout.simple_spinner_item, durations);

        durationSpinner.setAdapter(adapter);
    }

    private void initButtons(){
        /*This method links the button's clicks to their respective methods*/
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


    private void onCreateChatPressed() {
        /*This method confirms the input and creates a chat from it*/

        String durationSelection = (String)durationSpinner.getSelectedItem();

        if(!appointmentPicker.validSelection() || durationSelection == null){
            printError("Please enter all details");
            return;
        }

        //Format duration
        if(durationSelection == null) return;

        long dl = (Long.valueOf(durationSelection.substring(0,2))) * minuteMilli;
        String formattedDuration = DateTools.millisToDurationString(Locale.getDefault()
                ,dl);

        yarnPlace.yarnPlaceUpdater.removeChildListener();

        HashMap<String,String> chatMap = Chat.buildChatMap(localUserID,appointmentPicker.selectedDate
                ,appointmentPicker.selectedTime,formattedDuration);

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

    //region Private Methods

    private void printError(String errorMessage){
        Toast.makeText(mapsActivity,errorMessage,Toast.LENGTH_LONG).show();
    }

    //endregion
}


