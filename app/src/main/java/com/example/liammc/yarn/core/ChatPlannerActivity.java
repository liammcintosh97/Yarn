package com.example.liammc.yarn.core;

import android.app.Activity;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.notifications.Notification;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.notifications.TimeChangeReceiver;
import com.example.liammc.yarn.planner.EventWindow;
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

    //UI
    public ViewGroup parentViewGroup;
    private CompactCalendarView calendarView;
    private TextView monthYearTitle;
    private LinearLayout chatSuggestionElements;
    public EventWindow eventWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_planner);

        initUI();
        initNotifier();
        initEvents();
        initReceivers();
        initChannels();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(timeChangeReceiver.receiver);
    }

    //region Init

    private void initUI(){
        /*initializes the UI for the Chat Planner Activity*/
        parentViewGroup = findViewById(R.id.parentView);
        chatSuggestionElements =  findViewById(R.id.elements);

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

        final ChatPlannerActivity plannerActivity =  this;

        // define a listener to receive callbacks when certain events happen.
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                /*When the firebaseUser clicks on a day show what events are happening*/

                eventWindow = new EventWindow(plannerActivity,dateClicked);
                eventWindow.show(dateClicked);
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

    private void initNotifier() {
        /*initializes the notifier by getting it's instance and setting required listeners*/

        notifier = Notifier.getInstance();

        notifier.setSuggestionListener(new Notifier.SuggestionListener() {
            @Override
            public void onSuggestionAdded(Notification notification, Chat chat) {
               /*When a new suggestion is added to the Notifier the application must update the
               Suggestion Scroll view by adding a new elementView
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
                        chat.updator.initChangeListener(this);
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

    private void initReceivers(){

        //Registers the time change receiver
        timeChangeReceiver = new TimeChangeReceiver(this);
        registerReceiver(timeChangeReceiver.receiver,TimeChangeReceiver.intentFilter);
    }

    private void initChannels(){
        Notifier.getInstance().createNotificationChannel(this);
    }

    //endregion

    //region Private Local Methods

    private void addSuggestion(final Notification notification, final Chat chat) {
        /*Adds a chat to the suggestion scroll view*/

        //Inflate the elementView and add it to the suggestion scroll view
        final View element = inflate(R.layout.chat_suggestion_element,false);
        chatSuggestionElements.addView(element);

        //Set the elementView's button listeners
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

    private View inflate(int layoutID, boolean attachToRoot){
        /*Inflates the layout that has the passed layoutID*/

        LayoutInflater inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutID,parentViewGroup,attachToRoot);

        return view;
    }

    //endregion

    //region Button Methods

    private void onRemoveSuggestionPress(View view,Notification notification) {
        /*Removes suggestion from the suggestion scroll view*/

        chatSuggestionElements.removeView(view);
        notifier.removeSuggestion(notification);
    }

    //endregion
}
