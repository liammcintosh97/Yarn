package com.example.liammc.yarn.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.liammc.yarn.Events.Chat;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.utility.CompatabiltyTools;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatPlannerActivity extends AppCompatActivity{

    //region Warning Dialog
    public static class WarningDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure you want to cancel this chat? It'll likely disadvantage " +
                    "the other person")
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            Chat chat = getArguments().getParcelable("chat");

                            if(chat != null) {
                                onVerifyCancelPress(chat);
                            }
                            else Log.e("WarningDialog","unable to get chat from bundle");
                        }
                    })
                    .setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                           dismiss();
                        }
                    });

            return builder.create();
        }

        public boolean dissmissDialog(){

            Dialog dialog = getDialog();

            if( dialog != null && getDialog().isShowing()){
                dismiss();
                return true;
            }
            return false;
        }
    }
    //endregion

    private final String TAG = "ChatPlannerActivity";
    private HashMap<Long,ArrayList<Chat>> recordedChats;

    //Window
    public PopupWindow window;

    //Dialog
    private WarningDialog warningDialog;

    //UI
    private ViewGroup parentViewGroup;
    private View eventsView;
    private CompactCalendarView calendarView;
    private TextView monthYearTitle;
    private static ScrollView chatScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_planner);

        setUpCalendarView();
        setUpCalendarViewListeners();

        initializePopUp();
        initialiseUI();

        if(getRecordedChats()){
            initializeEvents(recordedChats);
        }

    }

    @Override
    public void onBackPressed(){

        if(dismissEventWindow() || warningDialog.dissmissDialog()){

        }
        else super.onBackPressed();

    }

    //region Set Up

    private void setUpCalendarView()
    {
        calendarView = findViewById(R.id.compactCalendar);
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);

        monthYearTitle = findViewById(R.id.calendarMonth);
        monthYearTitle.setText(calendarView.getFirstDayOfCurrentMonth().toString());
    }

    private void setUpCalendarViewListeners()
    {
        // define a listener to receive callbacks when certain events happen.
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                showEventWindow(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                monthYearTitle.setText(firstDayOfNewMonth.toString());
            }
        });
    }

    private void initializeEvents(HashMap<Long,ArrayList<Chat>> recordedChats)
    {

        for (HashMap.Entry<Long, ArrayList<Chat>> entry : recordedChats.entrySet()) {

            for(int i = 0 ; i < entry.getValue().size(); i++){

                Chat chat = entry.getValue().get(i);
                if(chat != null)
                {
                    Event newEvent = new Event(Color.GREEN,entry.getKey());
                    calendarView.addEvent(newEvent);

                    Log.d(TAG,"initialized event - " + entry.getKey());
                }
                else{
                    Log.e(TAG, "Fatal error when trying to initialize event. Chat object in" +
                            "recorded chats is null");
                }
            }
        }

    }

    private void initializePopUp()
    {
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = getLayoutInflater();
        eventsView = inflater.inflate(R.layout.chat_window,null);

        // Initialize a new instance of popup window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        double width = dm.widthPixels;
        double height = dm.heightPixels;

        window = new PopupWindow(eventsView,(int)(width * 0.75), (int)(height * 0.90));
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.update();
        window.setOutsideTouchable(true);
        window.setClippingEnabled(true);

        CompatabiltyTools.setPopupElevation(window,10.0f);
    }

    private void initialiseUI(){

        warningDialog = new WarningDialog();
        chatScrollView = eventsView.findViewById(R.id.chatScrollView);
        parentViewGroup = findViewById(R.id.parentView);
    }

    //endregion

    //region Private Local Methods

    private boolean showEventWindow(Date date) {

        chatScrollView.removeAllViews();

        ArrayList<Chat> chats = recordedChats.get(date.getTime());

        if(chats != null){

            for(int i = 0 ; i < chats.size(); i++){

                Chat chat = chats.get(i);
                if(chat != null)
                {
                    addChatToScrollView(chat);
                    Log.d(TAG,"Added chat to scroll view");
                }
                else{
                    Log.e(TAG, "Failed to add chat to scroll view");
                }
            }


            if (!window.isShowing()) {
                window.showAtLocation(parentViewGroup, Gravity.CENTER, 0, 0);
                Log.d(TAG,"Showing event window");
            }
            return true;
        }
        else Log.d(TAG,"No chats on this date");


        return false;
    }

    private boolean getRecordedChats(){

        Intent intent = getIntent();
        recordedChats = (HashMap<Long,ArrayList<Chat>>) intent.getExtras().get("recordedChats");

        if(recordedChats != null)
        {
            return true;
        }
        else{
            Log.e(TAG,"Fatal error - recorded Chats is null");
            return false;
        }
    }

    private void addChatToScrollView(final Chat chat) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View element = inflater.inflate(R.layout.chat_window_scroll_view_element,
                parentViewGroup,false);

        element.findViewById(R.id.cancelChatButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelChatPress(chat);
            }
        });

        String displayText = chat.chatPlaceName + "\n" + chat.chatDate + "\n" + chat.chatTime + "/n"
                + chat.chatLength;
        TextView chatDetails = element.findViewById(R.id.chatDetails);
        chatDetails.setText(displayText);

        chatScrollView.addView(element);
    }

    private static void removeChatFromScrollView(String removedChatID) {
        for(int i = 0; i < chatScrollView.getChildCount(); i++)
        {
            View child = chatScrollView.getChildAt(i);

            if(child.getContentDescription().toString().equals(removedChatID))
            {
                chatScrollView.removeViewAt(i);
            }
        }
    }

    private boolean dismissEventWindow()
    {
        if(window != null && window.isShowing()){
            window.dismiss();
            return true;
        }
        else return false;
    }
    //endregion

    //region Button Methods

    private void onCancelChatPress(Chat chat){
        warningDialog.show(getSupportFragmentManager(),TAG);

        Bundle bundle = new Bundle();
        bundle.putParcelable("chat",chat);
        warningDialog.setArguments(bundle);
    }

    private static void onVerifyCancelPress(Chat chat){
        removeChatFromScrollView(chat.chatID);
        chat.cancelChat();
    }

    //endregion

}
