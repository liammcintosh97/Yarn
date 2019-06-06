package com.example.liammc.yarn.planner;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.ChatPlannerActivity;
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.utility.CompatibilityTools;

import java.util.ArrayList;
import java.util.Date;

public class EventWindow {

    private final String TAG = "EventWindow";
    public final ChatPlannerActivity plannerActivity;
    private Recorder recorder;
    private static ArrayList<EventElement> eventChats =  new ArrayList<>();
    private final Date date;

    //UI
    public View parentView;
    public PopupWindow window;
    public LinearLayout chatScrollElements;


    public EventWindow(ChatPlannerActivity _plannerActivity, Date _date){
        this.plannerActivity = _plannerActivity;
        this.date = _date;

        this.init();
    }

    //region Init

    private void init(){
        recorder =  Recorder.getInstance();
        parentView = inflate(R.layout.chat_window,false);

        chatScrollElements = parentView.findViewById(R.id.elements);

        initWindow();
    }

    private void initWindow(){
        /*Creates a new window instance and set's its characteristics*/

        DisplayMetrics dm = new DisplayMetrics();
        plannerActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        window = new PopupWindow(parentView,(int)(dm.widthPixels * 0.75), (int)(dm.heightPixels * 0.90));
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.update();
        window.setOutsideTouchable(true);
        window.setClippingEnabled(true);

        CompatibilityTools.setPopupElevation(window,10.0f);
    }

    //endregion

    //region Public Methods

    public void show(Date date) {
        /*Shows the Events that are on the passed date*/
        window.showAtLocation(plannerActivity.parentViewGroup,Gravity.CENTER,0,0);
        update();
    }

    public void update(){
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
            if (!window.isShowing()) {
                window.showAtLocation(plannerActivity.parentViewGroup, Gravity.CENTER, 0, 0);
                Log.d(TAG,"Showing event window");
            }
        }
        else Log.d(TAG,"No chats on this date");
    }

    public boolean dismiss() {
        /*Dismisses the event Window*/

        if(window != null && window.isShowing()){
            window.dismiss();
            return true;
        }
        else return false;
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

    //region private

    private View inflate(int layoutID, boolean attachToRoot){
        /*Inflates the layout that has the passed layoutID*/

        LayoutInflater inflater = (LayoutInflater) plannerActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutID,plannerActivity.parentViewGroup,attachToRoot);

        return view;
    }

    //endregion
}
