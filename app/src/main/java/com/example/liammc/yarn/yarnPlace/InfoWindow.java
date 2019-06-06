package com.example.liammc.yarn.yarnPlace;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class InfoWindow {
    /*This class is the popup window that shows the user information about the Yarn Place that they
    have clicked on
     */

    private final String TAG = "InfoWindow";
    private YarnPlace yarnPlace;
    public MapsActivity mapsActivity;
    public ChatCreator chatCreator;
    private ArrayList<InfoElement> infoElements = new ArrayList<>();

    //Window
    public PopupWindow window;
    public int windowWidth;
    public int windowHeight;

    //UI
    private ViewGroup parentViewGroup;
    private View infoWindow;
    private TextView placeNameTitle;
    private ImageView placePicture;
    private Button googleMapsButton;
    private Button createChatButton;
    public ScrollView chatScrollView;
    public LinearLayout chatScrollViewElements;

    public InfoWindow(MapsActivity mapsActivity, YarnPlace _yarnPlace){
        this.mapsActivity = mapsActivity;
        this.yarnPlace = _yarnPlace;
        this.parentViewGroup = mapsActivity.findViewById(R.id.map);

        this.chatCreator =  new ChatCreator(mapsActivity, LocalUser.getInstance().userID,yarnPlace);

        this.init();
    }

    //region init

    private void init() {
        /*This method intializes the popup window for showing information about the yarn place
        to the firebaseUser
         */

        initWindow();
        initUI();
    }

    private void initWindow() {
        /*This method initializes the popup window object*/

        infoWindow = inflate(R.layout.info_window);

        // Initialize a new instance of popup window
        DisplayMetrics dm = new DisplayMetrics();
        mapsActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        window = new PopupWindow(infoWindow,(int)(dm.widthPixels* 0.6),
                (int)(dm.heightPixels * 0.5));
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.update();
        window.setClippingEnabled(true);

        CompatibilityTools.setPopupElevation(window,0.5f);
    }

    private void initUI() {
        /*This method gets all the references to the different View objects that this class
        requires. It also links the different buttons up to their respective button methods and
        initializes text.
         */

        placeNameTitle = infoWindow.findViewById(R.id.placeTilte);
        placePicture = infoWindow.findViewById(R.id.placePicture);
        googleMapsButton = infoWindow.findViewById(R.id.googleMapsButton);
        createChatButton = infoWindow.findViewById(R.id.createChatButton);
        chatScrollView = infoWindow.findViewById(R.id.chatScrollView);
        chatScrollViewElements = chatScrollView.findViewById(R.id.elements);


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

        placeNameTitle.setText(yarnPlace.placeMap.get("name"));
    }

    //endregion

    //region Button Methods
    /*This region of the class contains all the button methods which are called when the firebaseUser clicks
    on a UI button
     */

    private void OnCreateChatPressed() {
        /* This method is called when the firebaseUser clicks on the create chat button. It brings the firebaseUser
         * to another window which allows them to create a chat*/

        chatCreator.show();
    }

    private void OnGoogleMapsPressed() {
        /*This method runs when he firebaseUser clicks on the google maps button. It takes them to the
        google maps application on their phone with the place looked up
         */

        String map = "http://maps.google.co.in/maps?q=" + yarnPlace.address.toString();

        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        mapsActivity.startActivity(mapsIntent);
    }

    //endregion

    //region Public Methods

    public boolean show(GoogleMap map) {
        /*This method shows the info window to the firebase User*/
        if(!yarnPlace.checkReady()) return false;
        update();

        setPlacePhoto();
        measureWindow();

        //Show the window
        if (!window.isShowing()) {
            window.showAtLocation(parentViewGroup, Gravity.NO_GRAVITY, 0, 0);
        }

        updatePosition(map);
        return true;
    }

    public boolean updatePosition(GoogleMap map) {
        /*This method updates the position of the window relative to the position of the touched
        marker
         */

        if (yarnPlace.marker != null && window != null) {
            // marker is visible
            if (map.getProjection().getVisibleRegion().latLngBounds.contains(yarnPlace.marker.getPosition())) {

                Projection projection = map.getProjection();
                LatLng markerLatLng = yarnPlace.marker.getPosition();
                Point p = projection.toScreenLocation(markerLatLng);

                window.update(p.x,p.y,-1,-1);
                return true;
            } else { // marker outside screen
                dismiss();
                return false;
            }
        }

        return false;
    }

    public void dismiss() {
        /*This method dismisses the info window*/

        if(window != null && window.isShowing()) window.dismiss();
    }

    public void update(){
        /*This Method loops over all the chats in the Yarn Place and then adds them to the scroll
        view
         */

        //TODO Only show chats that are before the current time
        /*
        //Get the current time and format it so that is can be compared with the chat time
        Date currentDateTime = Calendar.getInstance().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDateTime);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int mins = cal.get(Calendar.MINUTE);

        Date currentTime = DateTools.stringTohMM(hours + ":" + mins);

        for (Chat chat: chats) {

            Date chatTime =  DateTools.stringTohMM(chat.chatTime);

            Log.d(TAG,"Chat time is " + chatTime.toString() + " and the current time is "
                    + currentTime.toString());
            if(chatTime.after(currentDateTime)) addToScrollView(chat);
        }*/

        chatScrollViewElements.removeAllViews();
        infoElements.clear();

        for (Chat chat: yarnPlace.getChats()) {

            InfoElement e = new InfoElement(this,chat);

            infoElements.add(e);
            chatScrollViewElements.addView(e.elementView);
        }
    }

    public void removeChatFromScrollView(String removedChatID) {
        /*This method removes the chat from the chat scroll view*/

        LinearLayout elements  = chatScrollView.findViewById(R.id.elements);

        for(int i = 0; i < elements.getChildCount(); i++)
        {
            View child = elements.getChildAt(i);

            if(child.getContentDescription().toString().equals(removedChatID))
            {
                elements.removeViewAt(i);
            }
        }
    }


    //endregion

    //region Private Methods

    private View inflate(int layoutID ) {
        /*Inflates the given layout ID*/

        LayoutInflater inflater = mapsActivity.getLayoutInflater();
        return inflater.inflate(layoutID,null);
    }

    private void measureWindow() {
        /*Measures the width and height of the pop up window*/

        Display display = mapsActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        infoWindow.measure(size.x, size.y);

        windowWidth = infoWindow.getMeasuredWidth();
        windowHeight = infoWindow.getMeasuredHeight();
    }

    private void setPlacePhoto() {
        //Sets the image view to the Place Photo

        ImageView imageView = infoWindow.findViewById(R.id.placePicture);
        imageView.setImageBitmap(yarnPlace.placePhoto);
    }


    //endregion
}
