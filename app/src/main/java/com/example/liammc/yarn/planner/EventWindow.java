package com.example.liammc.yarn.planner;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.ChatPlannerActivity;
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.core.YarnWindow;

import java.util.ArrayList;
import java.util.Date;

public class EventWindow extends YarnWindow {

    private final String TAG = "EventWindow";
    public final ChatPlannerActivity plannerActivity;
    private Recorder recorder;
    private static ArrayList<EventElement> eventChats =  new ArrayList<>();
    private final Date date;

    //UI
    private static final int layoutID = R.layout.window_events;
    public LinearLayout chatScrollElements;


    public EventWindow(ChatPlannerActivity _plannerActivity, ViewGroup _parent, Date _date){

        super(_plannerActivity, _parent,layoutID,0.75,0.90);
        this.plannerActivity = _plannerActivity;
        this.date = _date;

        this.init();
    }

    //region Init

    private void init(){
        recorder =  Recorder.getInstance();
        chatScrollElements = getContentView().findViewById(R.id.chatScrollView)
                .findViewById(R.id.elements);
    }

    //endregion

    //region Public Methods

    @Override
    public void show(int gravity) {
        /*Shows the Events that are on the passed date*/

        updateEventWindow(gravity);
    }

    public void updateEventWindow(int gravity){
        //Clear the scroll view
        chatScrollElements.removeAllViews();
        eventChats.clear();

        //Get all the chats that fall under the passed date
        ArrayList<Chat> chats = recorder.recordedChats.get(date.getTime());

        if(chats != null){
            //Loop over the lists of chats and add them to the scroll view
            for(int i = 0 ; i < chats.size(); i++){

                Chat chat = chats.get(i);
                if(chat != null) {
                    EventElement e = new EventElement(this,chat);
                    eventChats.add(e);
                    chatScrollElements.addView(e.parentView);
                    Log.d(TAG,"Added chat to scroll view");
                }
                else{
                    Log.e(TAG, "Failed to add chat to scroll view");
                }
            }

            //Show the window to the firebaseUser
            super.show(gravity);
        }
        else Log.d(TAG,"No chats on this date");
    }

    public void removeChat(String removedChatID) {
        /*Removes a particular chat elementView that matches the passed chat ID*/

        //Loop over all the elements
        for(int i = 0; i < chatScrollElements.getChildCount(); i++) {
            View child = chatScrollElements.getChildAt(i);

            /*Check if the description matches the passed Chat ID. If it does remove the view*/
            if(child.getContentDescription().toString().equals(removedChatID)) {
                chatScrollElements.removeViewAt(i);

                for (EventElement p: eventChats) {
                    if(p.chat.chatID.equals(removedChatID)) eventChats.remove(p);
                }
            }
        }
    }

    //endregion
}
