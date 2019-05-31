package com.example.liammc.yarn.core;

import android.app.Activity;
import android.graphics.Color;
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

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.dialogs.CancelDialog;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.notifications.Notification;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.notifications.TimeChangeReceiver;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatPlannerActivity extends AppCompatActivity{
    /*The Chat Planner activity is were the User interacts with the chats that they have created
    or joined. They also get a feed of recently made chats that are near by them.
     */

    private final String TAG = "ChatPlannerActivity";
    Recorder recorder;
    Notifier notifier;
    TimeChangeReceiver timeChangeReceiver;

    //Window
    public PopupWindow window;

    //Dialog
    private CancelDialog cancelDialog;

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

        initUI();
        initNotifier();
        initEvents();

        //Registers the time change receiver
        timeChangeReceiver = new TimeChangeReceiver(this);
        registerReceiver(timeChangeReceiver.receiver,TimeChangeReceiver.intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(timeChangeReceiver.receiver);
    }

    //region Init

    private void initUI(){
        /*initializes the UI for the Chat Planner Activity*/

        cancelDialog = new CancelDialog();
        cancelDialog.init(this);
        chatSuggestionElements = findViewById(R.id.suggestionScrollView).findViewById(R.id.elements);
        parentViewGroup = findViewById(R.id.parentView);

        initPopUp();
        initCalendarView();
    }

    private void initCalendarView() {
        /*Initializes the Calendar View*/

        calendarView = findViewById(R.id.compactCalendar);
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);

        monthYearTitle = findViewById(R.id.calendarMonth);
        monthYearTitle.setText(calendarView.getFirstDayOfCurrentMonth().toString());

        initCalendarViewListeners();
    }

    private void initCalendarViewListeners() {
        /*Initializes the Calendar View Listeners to check for firebaseUser interaction with the calendar*/

        // define a listener to receive callbacks when certain events happen.
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                /*When the firebaseUser clicks on a day show what events are happening*/
                showEventWindow(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                /*Update the Month and Year title on the calendar when the firebaseUser scrolls through the
                months
                 */
                monthYearTitle.setText(firstDayOfNewMonth.toString());
            }
        });
    }

    private void initPopUp() {
        /*Initializes the window Pop Up*/

        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = getLayoutInflater();
        eventsView = inflater.inflate(R.layout.chat_window,null);

        chatScrollElements = eventsView.findViewById(R.id.elements);

        //Get dimensions of the screen
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        // Initialize a new instance of popup window
        initWindow(dm.widthPixels,dm.heightPixels);
    }

    private void initWindow(double width, double height){
        /*Creates a new window instance and set's its characteristics*/

        window = new PopupWindow(eventsView,(int)(width * 0.75), (int)(height * 0.90));
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.update();
        window.setOutsideTouchable(true);
        window.setClippingEnabled(true);

        CompatibilityTools.setPopupElevation(window,10.0f);
    }

    private void initNotifier() {
        /*initializes the notifier by getting it's instance and setting required listeners*/

        notifier = Notifier.getInstance();

        notifier.setSuggestionListener(new Notifier.SuggestionListener() {
            @Override
            public void onSuggestionAdded(Notification notification, Chat chat) {
               /*When a new suggestion is added to the Notifier the application must update the
               Suggestion Scroll view by adding a new element
                */

                addSuggestion(notification,chat);
            }
        });
    }

    private void initEvents() {
        /*Initialise all the events in the Calendar from the recorded Chats*/

        recorder = Recorder.getInstance();

        if(recorder.recordedChats != null && recorder.recordedChats.size() > 0) {

            /*Loop over all the the recorded Chat Entries. Each entry is a HashMap containing a long
            value which represents a date in milliseconds and a list of Chats that fall under that
            date */
            for (HashMap.Entry<Long, ArrayList<Chat>> entry : recorder.recordedChats.entrySet()) {

                //Loop over the list of chats from the entry
                for (int i = 0; i < entry.getValue().size(); i++) {

                    Chat chat = entry.getValue().get(i);

                    if (chat != null) {
                        /*Create a new event at the date in which this particular chat lies under
                        and add it to the calendar
                         */
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

    //endregion

    //region Private Local Methods

    private boolean showEventWindow(Date date) {
        /*Shows the Events that are on the passed date*/

        //Clear the scroll view
        chatScrollElements.removeAllViews();

        //Get all the chats that fall under the passed date
        ArrayList<Chat> chats = recorder.recordedChats.get(date.getTime());

        if(chats != null){
            //Loop over the lists of chats and add them to the scroll view
            for(int i = 0 ; i < chats.size(); i++){

                Chat chat = chats.get(i);
                if(chat != null) {
                    addChat(chat);
                    Log.d(TAG,"Added chat to scroll view");
                }
                else{
                    Log.e(TAG, "Failed to add chat to scroll view");
                }
            }

            //Show the window to the firebaseUser
            if (!window.isShowing()) {
                window.showAtLocation(parentViewGroup, Gravity.CENTER, 0, 0);
                Log.d(TAG,"Showing event window");
            }
            return true;
        }
        else Log.d(TAG,"No chats on this date");

        return false;
    }

    private void addSuggestion(final Notification notification, final Chat chat) {
        /*Adds a chat to the suggestion scroll view*/

        //Inflate the element and add it to the suggestion scroll view
        final View element = inflate(R.layout.chat_suggestion_element,false);
        chatSuggestionElements.addView(element);

        //Set the element's button listeners
        element.findViewById(R.id.removeSuggestionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveSuggestionPress(element,notification);
            }
        });

        //Set the text and descriptions
        String suggestionText = notification.message;
        TextView suggestionTextView = findViewById(R.id.suggestionText);
        suggestionTextView.setContentDescription(chat.chatID);
        suggestionTextView.setText(suggestionText);
    }

    private void addChat(final Chat chat) {
        /*Adds a chat to the event window scroll view*/

        //Inflate the element and add it to the event window scroll view
        final View element = inflate(R.layout.chat_window_scroll_view_element,false);
        chatScrollElements.addView(element);

        //Set the element's button listeners
        element.findViewById(R.id.cancelChatButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCancelChatPress(chat);
            }
        });

        //Set the text and descriptions
        String displayText = chat.yarnPlace.placeMap.get("name") + "\n" + chat.chatDate + "\n"
                + chat.chatTime + "/n" + chat.chatLength;
        TextView chatDetails = element.findViewById(R.id.chatDetails);
        element.setContentDescription(chat.chatID);
        chatDetails.setText(displayText);
    }

    private static void removeChatFromScrollView(LinearLayout elements, String removedChatID) {
        /*Removes a particular chat element that matches the passed chat ID*/

        //Loop over all the elements
        for(int i = 0; i < elements.getChildCount(); i++) {
            View child = elements.getChildAt(i);

            /*Check if the description matches the passed Chat ID. If it does remove the view*/
            if(child.getContentDescription().toString().equals(removedChatID)) {
                elements.removeViewAt(i);
            }
        }
    }

    private View inflate(int layoutID, boolean attachToRoot){
        /*Inflates the layout that has the passed layoutID*/

        LayoutInflater inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutID,parentViewGroup,attachToRoot);

        return view;
    }

    private boolean dismissEventWindow() {
        /*Dismisses the event Window*/

        if(window != null && window.isShowing()){
            window.dismiss();
            return true;
        }
        else return false;
    }
    //endregion

    //region Button Methods

    private void onCancelChatPress(Chat chat){
       /*Runs when the firebaseUser clicks on the cancel button of the chat*/

        //Show the warning dialog
        cancelDialog.show(getSupportFragmentManager(),TAG);

        //Pass the chat to the warning dialog so that it can interact with it
        Bundle bundle = new Bundle();
        bundle.putString("chatID",chat.chatID);
        cancelDialog.setArguments(bundle);
    }

    private void onRemoveSuggestionPress(View view,Notification notification) {
        /*Removes suggestion from the suggestion scroll view*/

        chatSuggestionElements.removeView(view);
        notifier.removeSuggestion(notification);
    }

    public static void onVerifyCancelPress(ChatPlannerActivity planner,Chat chat){
        /*This runs when the firebaseUser verifies that they want to cancel the chat. It removes it from the
        * event scroll view, the database and the application's system*/
        removeChatFromScrollView(chatScrollElements,chat.chatID);
        chat.yarnPlace.infoWindow.removeChatFromScrollView(chat.chatID);
        chat.cancelChat();

        if(chatScrollElements.getChildCount() == 0) planner.dismissEventWindow();
    }

    //endregion
}
