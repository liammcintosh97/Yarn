package com.example.liammc.yarn.core;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.utility.CompatibilityTools;

public class YarnWindow extends PopupWindow {
    //This is the parent class for all popup windows in the Yarn application

    private ViewGroup parent;
    private ImageButton closeButton;

    public YarnWindow(Activity _activity, ViewGroup _parent,int _layoutID,double _widthM
            ,double _heightM){

        super(initMainView(_layoutID,_activity,_parent), initWidth(_activity,_widthM)
                ,initHeight(_activity,_heightM),true);
        this.initWindow();
        this.initCloseButton();
        this.parent =  _parent;
    }

    public YarnWindow(Activity _activity, ViewGroup _parent,int _layoutID){

        super(initMainView(_layoutID,_activity,_parent),ViewGroup.LayoutParams.MATCH_PARENT
                ,ViewGroup.LayoutParams.MATCH_PARENT,true);
        this.initWindow();
        this.parent =  _parent;
    }

    //region Init

    private static View initMainView(int _layoutID,Activity _activity, ViewGroup _parent){
        //This method inflates the view and returns it

        LayoutInflater inflater = (LayoutInflater) _activity
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(_layoutID,_parent,false);
    }

    private static int initWidth(Activity _activity, double multiplier){
        /*This method returns an int to be the width of the window. By getting the metrics of the
        activity and multiplying it by a passed value
         */

        DisplayMetrics dm = new DisplayMetrics();
        _activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        return (int) Math.round(dm.widthPixels * multiplier);
    }

    private static int initHeight(Activity _activity, double multiplier){
        /*This method returns an int to be the height of the window. By getting the metrics of the
        activity and multiplying it by a passed value
         */

        DisplayMetrics dm = new DisplayMetrics();
        _activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        return (int) Math.round(dm.heightPixels * multiplier);
    }

    private void initWindow(){
        //This method initializes the properties of the window
        setAnimationStyle(R.style.popup_window_animation_phone);
        setOutsideTouchable(true);
        update();

        CompatibilityTools.setPopupElevation(this,5.0f);
    }

    private void initCloseButton(){
        closeButton =  getContentView().findViewById(R.id.closebutton);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    //endregion

    //region Public Methods

    public void show(int gravity) {
        //This method shows the window relative to the parent view
        if(!isShowing()) showAtLocation(parent, gravity, 0, 0);
    }

    //endregion
}
