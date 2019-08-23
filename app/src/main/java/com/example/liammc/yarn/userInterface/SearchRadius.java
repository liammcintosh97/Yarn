package com.example.liammc.yarn.userInterface;

import android.util.Log;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.finders.NearbyChatFinder;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class SearchRadius {

    private final String TAG = "SearchRadius";
    private final GoogleMap map;
    private final LocalUser localUser;
    NearbyChatFinder nearbyChatFinder;

    //UI
    Circle circle;

    public SearchRadius(GoogleMap _map,NearbyChatFinder _nearbyChatFinder){
        this.map = _map;
        this.localUser = LocalUser.getInstance();
        this.nearbyChatFinder = _nearbyChatFinder;
        this.Init();
    }

    //region Init

    private void Init(){
        /*Updates the circle on the Map*/

        if(circle != null )circle.remove();
        if(map == null){
            Log.e(TAG,"Couldn't draw circle because the Map is null");
            return;
        }
        if(localUser == null || localUser.lastLatLng == null){
            Log.e(TAG,"Couldn't draw circle because the Local user or their last LatLng is null");
            return;
        }

        circle = map.addCircle(new CircleOptions()
                .center(localUser.lastLatLng)
                .radius(localUser.SEARCH_RADIUS_DEFAULT)
                .strokeColor(R.color.search_radius_stroke)
                .fillColor(R.color.search_radius_fill));

    }

    //endregion

    //region Public Methods

    public void update(double radius, LatLng center){
        if(map == null){
            Log.e(TAG,"Couldn't draw circle because the Map is null");
            return;
        }
        if(localUser == null || localUser.lastLatLng == null){
            Log.e(TAG,"Couldn't draw circle because the Local user or their last LatLng is null");
            return;
        }
        if(circle == null ){
            Log.e(TAG,"Couldn't draw circle because it's null");
            return;
        }

        circle.setRadius(radius);
        circle.setCenter(center);
        nearbyChatFinder.setSearchRadius((int)radius);
    }

    //endregion

}
