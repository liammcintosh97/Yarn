package com.example.liammc.yarn.userInterface;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.example.liammc.yarn.R;

public class LoadingSymbol {

    private  final String TAG = "Loading Symbol";
    private ImageView loadingSymbol;
    private RotateAnimation animation;

    public LoadingSymbol(Activity _activity){
        init(_activity);
    }

    //region Init Methods

    private void init(Activity activity){
        loadingSymbol =  activity.findViewById(R.id.loadingSymbol);

        if(loadingSymbol == null){
            Log.e(TAG,"The loading symbol could not be found in this layout");
        }

        loadingSymbol.setVisibility(View.INVISIBLE);
        initAnimation();
    }

    private void initAnimation(){
        animation = new RotateAnimation(0, 358,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setDuration(1000);
        animation.setRepeatCount(Integer.MAX_VALUE);
        animation.setInterpolator(new LinearInterpolator());
    }

    //endregion

    //region Public Methods

    public void start(){
        loadingSymbol.setVisibility(View.VISIBLE);
        loadingSymbol.setAnimation(animation);
    }

    public void stop(){
        loadingSymbol.setVisibility(View.INVISIBLE);
        loadingSymbol.setAnimation(null);
    }

    //endregion
}
