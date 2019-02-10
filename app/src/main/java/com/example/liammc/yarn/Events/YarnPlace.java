package com.example.liammc.yarn.Events;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.core.ChatRecorder;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.CompatabiltyTools;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

public class YarnPlace
{
    public final class PlaceType
    {
        public static final String BAR = "BAR";
        public static final String CAFE = "CAFE";
        public static final String RESTAURANT = "RESTAURANT";

        private PlaceType(){}
    }

    private Geocoder geocoder;
    private MapsActivity mapsActivity;
    public ChatCreator chatCreator;
    private ChatFinder chatFinder;

    //Chat Data
    public HashMap<String, String>  placeMap;
    public Address address;
    public LatLng latLng;
    public String placeType;
    public List<Chat> chats;

    //Google
    private GoogleMap map;
    public Marker marker;

    //Window
    public PopupWindow window;
    public int windowWidth;
    public int windowHeight;

    //UI
    private ViewGroup parentViewGroup;
    private View yarnPlaceInfoWindow;
    private TextView placeNameTitle;
    private ImageView placePicture;
    private Button googleMapsButton;
    private Button joinChatButton;
    private Button createChatButton;
    private ScrollView chatScrollView;

    public YarnPlace(MapsActivity _mapsActivity, GoogleMap _map, HashMap<String, String> _placeMap, String _placeType)
    {
        this.mapsActivity = _mapsActivity;
        this.geocoder = new Geocoder(_mapsActivity
                ,_mapsActivity.getResources().getConfiguration().locale);

        this.placeMap = _placeMap;
        this.placeType = _placeType;
        this.map = _map;

        this.createMarker();
        this.chatFinder = new ChatFinder("YarnPlace",mapsActivity.localUser.userID
                ,this);

        this.parentViewGroup = _mapsActivity.findViewById(R.id.map);


        this.chatCreator = new ChatCreator(_mapsActivity,parentViewGroup,
                mapsActivity.localUser.userID);
        this.initializePopUp();
        this.initializeUI();
    }

    //region Initialisation

    private void createMarker()
    {
        MarkerOptions markerOptions = new MarkerOptions();

        String placeName = placeMap.get("place_name");
        String vicinity = placeMap.get("vicinity");

        double lat = Double.parseDouble( placeMap.get("lat"));
        double lng = Double.parseDouble( placeMap.get("lng"));

        latLng = new LatLng( lat, lng);
        markerOptions.position(latLng);
        markerOptions.title(placeName + " : "+ vicinity);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        marker = map.addMarker(markerOptions);
        address = AddressTools.getAddressFromLocation(geocoder,latLng);
    }

    private void initializePopUp()
    {
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = mapsActivity.getLayoutInflater();
        yarnPlaceInfoWindow = inflater.inflate(R.layout.info_window,null);

        // Initialize a new instance of popup window
        DisplayMetrics dm = new DisplayMetrics();
        mapsActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        double width = dm.widthPixels;
        double height = dm.heightPixels;

        //double width =  LinearLayout.LayoutParams.WRAP_CONTENT;
        //double height = LinearLayout.LayoutParams.WRAP_CONTENT;

        window = new PopupWindow(yarnPlaceInfoWindow,(int)(width * 0.5), (int)(height * 0.40));
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.update();
        window.setClippingEnabled(true);

        CompatabiltyTools.setPopupElevation(window,5.0f);
    }

    private void initializeUI()
    {
        placeNameTitle = yarnPlaceInfoWindow.findViewById(R.id.placeTilte);
        placePicture = yarnPlaceInfoWindow.findViewById(R.id.placePicture);
        googleMapsButton = yarnPlaceInfoWindow.findViewById(R.id.googleMapsButton);
        joinChatButton = yarnPlaceInfoWindow.findViewById(R.id.joinChatButton);
        createChatButton = yarnPlaceInfoWindow.findViewById(R.id.createChatButton);
        chatScrollView = yarnPlaceInfoWindow.findViewById(R.id.chatScrollView);

        googleMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnGoogleMapsPressed();
            }
        });
        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnCreateChatPressed();
            }
        });

        placeNameTitle.setText(placeMap.get("name"));
    }

    //endRegion

    //region Button Methods

    private void OnCreateChatPressed()
    {
        chatCreator.showChatCreator(this);
    }

    private void OnGoogleMapsPressed()
    {
        String map = "http://maps.google.co.in/maps?q=" + address.toString();

        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        mapsActivity.startActivity(mapsIntent);
    }

    private void OnJoinChatPressed(Chat chat)
    {
        chat.acceptChat(mapsActivity.localUser);
        ChatRecorder.getInstance().recordChat(chat);
    }
    //endregion

    //region Public Local Methods

    public boolean showInfoWindow() {
        Display display = mapsActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        yarnPlaceInfoWindow.measure(size.x, size.y);

        windowWidth = yarnPlaceInfoWindow.getMeasuredWidth();
        windowHeight = yarnPlaceInfoWindow.getMeasuredHeight();

        if (!window.isShowing()) {
            window.showAtLocation(parentViewGroup, Gravity.NO_GRAVITY, 0, 0);
        }
        updatePopup();

        return true;
    }

    public boolean updatePopup() {

        if (marker != null && window != null) {
            // marker is visible
            if (map.getProjection().getVisibleRegion().latLngBounds.contains(marker.getPosition())) {

                Projection projection = map.getProjection();
                LatLng markerLatLng = marker.getPosition();
                Point p = projection.toScreenLocation(markerLatLng);

                window.update(p.x,p.y,-1,-1);
                return true;
            } else { // marker outside screen
                dismissInfoWindow();
                return false;
            }
        }

        return false;
    }

    public void dismissInfoWindow()
    {
        if(window != null && window.isShowing()) window.dismiss();
    }

    public void addChatToScrollView(final Chat chat) {
        LayoutInflater inflater = (LayoutInflater) mapsActivity
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View element = inflater.inflate(R.layout.info_window_scroll_view_element,
                parentViewGroup,false);

        element.findViewById(R.id.joinChatButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnJoinChatPressed(chat);
            }
        });

        String displayText = chat.chatDate + " " + chat.chatTime;

        element.setContentDescription(chat.chatPlaceID);
        TextView dateTime = element.findViewById(R.id.dateTime);
        dateTime.setText(displayText);

        chatScrollView.addView(element);
    }

    public void removeChatFromScrollView(String removedChatID) {
        for(int i = 0; i < chatScrollView.getChildCount(); i++)
        {
            View child = chatScrollView.getChildAt(i);

            if(child.getContentDescription().toString().equals(removedChatID))
            {
                chatScrollView.removeViewAt(i);
            }
        }
    }

    //endregion

}
