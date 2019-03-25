package com.example.liammc.yarn.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.liammc.yarn.Events.Chat;
import com.example.liammc.yarn.Events.Notifier;
import com.example.liammc.yarn.Notification;
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

            final String TAG ="Warning Dialog";

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure you want to cancel this chat? It'll likely disadvantage " +
                    "the other person")
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            String chatId = getArguments().getString("chatId");
                            Recorder recorder = Recorder.getInstance();
                            Chat chat = recorder.getRecordedChat(chatId);

                            if(chatId == null || chatId.isEmpty()){
                                Log.e(TAG,"Unable to cancel chat - chatID is null");
                                return;
                            }

                            if(chat == null){
                                Log.e(TAG,"Unable to cancel chat - chat is null");
                                return;
                            }

                            onVerifyCancelPress(chat);
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
    //private HashMap<Long,ArrayList<Chat>> recordedYarnPlaces;
    Recorder recorder;
    Notifier notifier;

    //Window
    public PopupWindow window;

    //Dialog
    private WarningDialog warningDialog;

    //UI
    private ViewGroup parentViewGroup;
    private View eventsView;
    private CompactCalendarView calendarView;
    private TextView monthYearTitle;
    private static LinearLayout chatScrollElements;
    private static LinearLayout chatSuggestionElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_planner);

        setUpCalendarView();
        setUpCalendarViewListeners();

        initializePopUp();
        initialiseUI();

        recorder = Recorder.getInstance();
        initializeEvents();

        notifier = Notifier.getInstance();

        registerReceiver(notifier.timeChangeReceiver,notifier.intentFilter);
        setNotifier();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notifier.timeChangeReceiver);
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

    private void setNotifier()
    {
        notifier = Notifier.getInstance();

        notifier.setSuggestionListener(new Notifier.SuggestionListener() {

            @Override
            public void onSuggestionAdded(Notification notification, Chat chat)
            {
                addChatToSuggestionScrollView(notification,chat);
            }
        });
    }

    private void initializeEvents()
    {
        if(recorder.recordedChats != null && recorder.recordedChats.size() > 0) {

            for (HashMap.Entry<Long, ArrayList<Chat>> entry : recorder.recordedChats.entrySet()) {

                for (int i = 0; i < entry.getValue().size(); i++) {

                    Chat chat = entry.getValue().get(i);

                    if (chat != null) {
                        Event newEvent = new Event(Color.GREEN, entry.getKey());
                        calendarView.addEvent(newEvent);

                        Log.d(TAG, "initialized event - " + entry.getKey());
                    } else {
                        Log.e(TAG, "Fatal error when trying to initialize event. Chat object in" +
                                "recorded chats is null");
                    }
                }
            }
        }
        else{
            Log.d(TAG,"There are no recorded chats");
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
        chatScrollElements = eventsView.findViewById(R.id.elements);
        chatSuggestionElements = findViewById(R.id.suggestionScrollView).findViewById(R.id.elements);
        parentViewGroup = findViewById(R.id.parentView);
    }

    //endregion

    //region Private Local Methods

    private boolean showEventWindow(Date date) {

        chatScrollElements.removeAllViews();

        ArrayList<Chat> chats = recorder.recordedChats.get(date.getTime());

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

    private void addChatToSuggestionScrollView(final Notification notification, final Chat chat)
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View element = inflater.inflate(R.layout.chat_suggestion_element,
                parentViewGroup,false);

        element.findViewById(R.id.removeSuggestionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveSuggestionPress(element,notification);
            }
        });

        String suggestionText = notification.message;

        TextView suggestionTextView = findViewById(R.id.suggestionText);
        suggestionTextView.setContentDescription(chat.chatID);
        suggestionTextView.setText(suggestionText);

        chatSuggestionElements.addView(element);
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

        String displayText = chat.yarnPlace.placeMap.get("name") + "\n" + chat.chatDate + "\n"
                + chat.chatTime + "/n" + chat.chatLength;

        TextView chatDetails = element.findViewById(R.id.chatDetails);
        chatDetails.setContentDescription(chat.chatID);
        chatDetails.setText(displayText);

        chatScrollElements.addView(element);
    }

    private static void removeChatFromScrollView(LinearLayout elements, String removedChatID) {
        for(int i = 0; i < elements.getChildCount(); i++)
        {
            View child = elements.getChildAt(i);

            if(child.getContentDescription().toString().equals(removedChatID))
            {
                elements.removeViewAt(i);
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
        bundle.putString("chatID",chat.chatID);
        warningDialog.setArguments(bundle);
    }

    private void onRemoveSuggestionPress(View view,Notification notification)
    {
        chatSuggestionElements.removeView(view);

        notifier.removeSuggestion(notification);
    }

    private static void onVerifyCancelPress(Chat chat){
        removeChatFromScrollView(chatScrollElements,chat.chatID);
        chat.cancelChat();
    }

    //endregion
}
