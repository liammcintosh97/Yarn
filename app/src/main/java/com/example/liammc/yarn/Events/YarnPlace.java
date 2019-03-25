package com.example.liammc.yarn.Events;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.CompatabiltyTools;
import com.example.liammc.yarn.utility.ReadyListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class YarnPlace
{
    //region Ready Listener

    private ReadyListener readyListener;

    public ReadyListener getReadyListener() {
        return readyListener;
    }

    public void setReadyListener(ReadyListener readyListener) {
        this.readyListener = readyListener;
    }
    //endregion

    public final class PlaceType
    {
        public static final String BAR = "bar";
        public static final String CAFE = "cafe";
        public static final String RESTAURANT = "restaurant";
        public static final String NIGHT_CLUB = "night_club";

        private PlaceType(){}
    }

    private String TAG = "Yarn Place";
    public ChatCreator chatCreator;
    public ChatUpdater chatUpdater;

    //Place Data
    public HashMap<String, String>  placeMap;
    public String placeType;
    public Address address;
    public LatLng latLng;
    public Bitmap placePhoto;
    public ArrayList<Chat> chats= new ArrayList<>();

    //Google
    public Marker marker;
    PlacesClient placesClient;

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

    //Misc
    private boolean chatUpdaterReady = false;

    public YarnPlace(HashMap<String, String> _placeMap)
    {
        this.TAG += (" " + _placeMap.get("id"));
        this.placeMap = _placeMap;
        this.placeType = _placeMap.get("type");
    }

    //region Initialisation

    public void init(Context context,Geocoder geocoder){

        initializePlaces(context);
        getPlacePhoto();
        getPlaceAddress(context,geocoder);
    }

    public void initChatUpdater(Context context){

        chatUpdater = new ChatUpdater(LocalUser.getInstance().user.userID
                , this, new ReadyListener() {
            @Override
            public void onReady() {
                chatUpdaterReady = true;
                if(checkReady())readyListener.onReady();
            }
        });
        chatUpdater.getJoinedChats(context);
    }

    public void initOnMap(MapsActivity mapsActivity, GoogleMap map){


        parentViewGroup = mapsActivity.findViewById(R.id.map);

        chatCreator = new ChatCreator(mapsActivity,parentViewGroup,
                LocalUser.getInstance().user.userID);

        createMarker(map);
        initializePopUp(mapsActivity);
        initializeUI(mapsActivity);
    }

    private void createMarker(GoogleMap map) {
        MarkerOptions markerOptions = new MarkerOptions();

        double lat = Double.parseDouble( placeMap.get("lat"));
        double lng = Double.parseDouble( placeMap.get("lng"));

        latLng = new LatLng( lat, lng);
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        marker = map.addMarker(markerOptions);
    }

    private void initializePopUp(MapsActivity mapsActivity) {
        // Initialize a new instance of LayoutInflater service
        LayoutInflater inflater = mapsActivity.getLayoutInflater();
        yarnPlaceInfoWindow = inflater.inflate(R.layout.info_window,null);

        // Initialize a new instance of popup window
        DisplayMetrics dm = new DisplayMetrics();
        mapsActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        double width = dm.widthPixels;
        double height = dm.heightPixels;

        window = new PopupWindow(yarnPlaceInfoWindow,(int)(width * 0.6), (int)(height * 0.5));
        window.setAnimationStyle(R.style.popup_window_animation_phone);
        window.update();
        window.setClippingEnabled(true);

        CompatabiltyTools.setPopupElevation(window,5.0f);
    }

    private void initializeUI(final MapsActivity mapsActivity) {
        placeNameTitle = yarnPlaceInfoWindow.findViewById(R.id.placeTilte);
        placePicture = yarnPlaceInfoWindow.findViewById(R.id.placePicture);
        googleMapsButton = yarnPlaceInfoWindow.findViewById(R.id.googleMapsButton);
        joinChatButton = yarnPlaceInfoWindow.findViewById(R.id.joinChatButton);
        createChatButton = yarnPlaceInfoWindow.findViewById(R.id.createChatButton);
        chatScrollView = yarnPlaceInfoWindow.findViewById(R.id.chatScrollView);

        googleMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnGoogleMapsPressed(mapsActivity);
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

    private void initializePlaces(Context context){
        // Initialize Places.
        Places.initialize(context
                 ,context.getResources().getString(R.string.google_place_key));

        // Create a new Places client instance.
        placesClient = Places.createClient(context);
    }

    private void getPlacePhoto(){

        // Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);

        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeMap.get("id"), fields).build();

        placesClient.fetchPlace(placeRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {

                final Place place = fetchPlaceResponse.getPlace();

                // Get the photo metadata.
                PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

                // Get the attribution text.
                String attributions = photoMetadata.getAttributions();

                // Create a FetchPhotoRequest.
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .build();

                placesClient.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                    @Override
                    public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                        Bitmap b  = fetchPhotoResponse.getBitmap();

                        placePhoto = Bitmap.createScaledBitmap(b,
                                (int)(b.getWidth() * 0.40),
                                (int)(b.getHeight() * 0.40),true);

                        if(checkReady()) readyListener.onReady();

                        Log.d(TAG," Got Place photo");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            // Handle error with given status code.
                            Log.e(TAG, "Place not found: " + e.getMessage());
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to fetch place: " + e.getMessage());
            }
        });
    }

    private void getPlaceAddress(final Context context,final Geocoder geocoder){

        // Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG);

        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeMap.get("id"), fields).build();

        placesClient.fetchPlace(placeRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                address = AddressTools.getAddressFromLocation(geocoder
                        ,fetchPlaceResponse.getPlace().getLatLng());

                initChatUpdater(context);

                if(checkReady())readyListener.onReady();
                Log.d(TAG,"Got Place address");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Unable to fetch place: " + e.getMessage());
            }
        });
    }

    //endregion

    //region Button Methods

    private void OnCreateChatPressed()
    {
        chatCreator.showChatCreator(this);
    }

    private void OnGoogleMapsPressed(MapsActivity mapsActivity) {
        String map = "http://maps.google.co.in/maps?q=" + address.toString();

        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        mapsActivity.startActivity(mapsIntent);
    }

    private void OnJoinChatPressed(Context context,Chat chat) {
        chat.acceptChat(context,LocalUser.getInstance().user);
        Recorder.getInstance().recordChat(context,chat);
    }
    //endregion

    //region Public  Methods

    public boolean showInfoWindow(MapsActivity mapsActivity, GoogleMap map) {

        addChatsToScrollView(mapsActivity,chats);

        ImageView imageView = yarnPlaceInfoWindow.findViewById(R.id.placePicture);
        imageView.setImageBitmap(placePhoto);

        Display display = mapsActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        yarnPlaceInfoWindow.measure(size.x, size.y);

        windowWidth = yarnPlaceInfoWindow.getMeasuredWidth();
        windowHeight = yarnPlaceInfoWindow.getMeasuredHeight();

        if (!window.isShowing()) {
            window.showAtLocation(parentViewGroup, Gravity.NO_GRAVITY, 0, 0);
        }
        updatePopup(map);

        return true;
    }

    public boolean updatePopup(GoogleMap map) {

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

    public void addChatsToScrollView(final Context context,ArrayList<Chat> chats){

        for (Chat chat: chats) {
            addChatToScrollView(context, chat);
        }
    }

    public void addChatToScrollView(final Context context, final Chat chat) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View element = inflater.inflate(R.layout.info_window_scroll_view_element,
                parentViewGroup,false);

        element.findViewById(R.id.joinChatButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnJoinChatPressed(context,chat);
            }
        });

        String displayText = chat.chatDate + " " + chat.chatTime;

        element.setContentDescription(chat.yarnPlace.placeMap.get("id"));
        TextView dateTime = element.findViewById(R.id.dateTime);
        dateTime.setText(displayText);

        LinearLayout elements  = chatScrollView.findViewById(R.id.elements);
        elements.addView(element);
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

    //region utility

    public static HashMap<String, String> buildPlaceMap(String id, String name,String type,
                                                        String lat, String lng){

        HashMap<String, String > placeMap = new HashMap<>();
        placeMap.put("id",id);
        placeMap.put("name", name);
        placeMap.put("type",type);
        placeMap.put("lat", lat);
        placeMap.put("lng", lng);

        return placeMap;
    }
    public boolean checkReady(){

        return readyListener != null && chatUpdaterReady && placePhoto != null && address != null;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof YarnPlace)) {
            return false;
        }
        YarnPlace cc = (YarnPlace)o;
        return cc.placeMap.get("id").equals(this.placeMap.get("id"));
    }

    //endregion

}
