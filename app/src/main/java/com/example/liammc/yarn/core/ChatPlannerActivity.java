package com.example.liammc.yarn.core;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.liammc.yarn.Events.Chat;
import com.example.liammc.yarn.R;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatPlannerActivity extends AppCompatActivity{

    private final String TAG = "ChatPlannerActivity";

    private CompactCalendarView calendarView;
    private TextView monthYearTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_planner);

        setUpCalendarView();
        setUpCalendarViewListeners();


        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        HashMap<Long,Chat> recordedChats =
                (HashMap<Long,Chat>) intent.getExtras().get("recordedChats");

        if(recordedChats != null)
        {
            initializeEvents(recordedChats);
        }
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
                List<Event> events = calendarView.getEvents(dateClicked);
                Log.d(TAG, "Day was clicked: " + dateClicked + " with events " + events);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                monthYearTitle.setText(firstDayOfNewMonth.toString());
            }
        });
    }

    private void initializeEvents(HashMap<Long,Chat> recordedChats)
    {

        for (HashMap.Entry<Long, Chat> entry : recordedChats.entrySet()) {

            Chat chat = entry.getValue();

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

    //endregion

}
