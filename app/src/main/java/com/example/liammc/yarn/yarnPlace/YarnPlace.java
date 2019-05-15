package com.example.liammc.yarn.yarnPlace;

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

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.core.ChatCreator;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.core.Recorder;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.networking.Communicator;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.example.liammc.yarn.interfaces.ReadyListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class YarnPlace
{
    /*A Yarn Place is one of Yarn's main objects that the firebaseUser interacts with. It holds information
    about a particular place within the world. It also holds a list of chat that belong to that Yarn
    place at any given time
     */

    //region Ready Listener
    /*This listener is used to tell the system when the yarn place is ready for firebaseUser interaction*/

    private ReadyListener readyListener;

    public ReadyListener getReadyListener() {
        return readyListener;
    }

    public void setReadyListener(ReadyListener readyListener) {
        this.readyListener = readyListener;
    }
    //endregion

    private static final String PLACE_INFO_REF = "Yarn_Place_Info";
    private String TAG = "Yarn Place";
    public ChatCreator chatCreator;
    public YarnPlaceUpdater yarnPlaceUpdater;
    public DatabaseReference placeRef;

    //Place Data
    public HashMap<String, String>  placeMap;
    public String placeType;
    public Address address;
    public Bitmap placePhoto;
    private ArrayList<Chat> chats= new ArrayList<>();

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
    private boolean yarnPlaceUpdaterReady = false;

    public YarnPlace(HashMap<String, String> _placeMap)
    {
        this.TAG += (" " + _placeMap.get("id"));
        this.placeMap = _placeMap;
        this.placeType = _placeMap.get("type");
    }

    //region Initialisation
    /*This region of the class has all the methods that are required for initializing the Yarn Place.
    When initializing a Yarn Place we first must run the init method which will initialize services
    and get the Yarn Place's photo and address. Once the Yarn place is ready and we what the firebaseUser to
    interact with it we must run the initOnMap method to initialize the marker, UI and Pop Up Window
     */

    public void init(Activity activity,Geocoder geocoder){
        /* This method does the internal initialization for the yarn place including getting
        the place photo, address and initializing services
         */

        Context context = activity.getApplicationContext();

        // Initialize Places.
        Places.initialize(context,context.getResources().getString(R.string.google_place_key));

        // Create a new Places client instance.
        placesClient = Places.createClient(context);

        getPhoto();
        getAddress(activity,geocoder);

        placeRef = AddressTools.getPlaceDatabaseReference(
                address.getCountryName(),address.getAdminArea(),
                placeMap.get("id"));
    }

    public void initYarnPlaceUpdater(Activity activity){
        /*This method initializes the Chat Updator. The ready status of the Chat Updator also
        * determines the ready status of the Yarn Place*/

        yarnPlaceUpdater = new YarnPlaceUpdater(LocalUser.getInstance().userID
                , this, new ReadyListener() {
            @Override
            public void onReady() {
                yarnPlaceUpdaterReady = true;
                if(checkReady())readyListener.onReady();
            }
        });
        yarnPlaceUpdater.getJoinedChats(activity);
    }

    public void initYarnPlaceDatabase() {
        final String placeName = placeMap.get("name");
        final Double lat = marker.getPosition().latitude;
        final Double lng = marker.getPosition().longitude;
        final String country = address.getCountryName();
        final String admin1 = address.getAdminArea();
        final String admin2 = address.getSubAdminArea();
        final String locality = address.getLocality();
        final String street = address.getAddressLine(0);
        final String postCode = address.getPostalCode();
        final String type = placeType;

        //Check if the database has an info node for this place
        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(PLACE_INFO_REF)){
                    Communicator.setData(placeRef,"place_name",placeName);
                    Communicator.setData(placeRef,"lat",lat);
                    Communicator.setData(placeRef,"lng",lng);
                    Communicator.setData(placeRef,"country",country);
                    Communicator.setData(placeRef,"admin1",admin1);
                    Communicator.setData(placeRef,"admin2",admin2);
                    Communicator.setData(placeRef,"locality",locality);
                    Communicator.setData(placeRef,"street",street);
                    Communicator.setData(placeRef,"postcode",postCode);
                    Communicator.setData(placeRef,"place_type",type);

                    Log.d(TAG,"Created the place info node");
                }
                else {
                    Log.d(TAG,"The place info node already exists");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void initOnMap(MapsActivity mapsActivity, GoogleMap map){

        /*This method initializes the Yarn Place on a google maps and enables the firebaseUser to
        interact with it
         */

        parentViewGroup = mapsActivity.findViewById(R.id.map);

        chatCreator = new ChatCreator(mapsActivity,parentViewGroup,
                LocalUser.getInstance().userID);

        double lat = Double.parseDouble(placeMap.get("lat"));
        double lng = Double.parseDouble( placeMap.get("lng"));

        marker = mapsActivity.createMarker(lat,lng);
        initPopUp(mapsActivity);
        initUI(mapsActivity);
    }

    private void initPopUp(MapsActivity mapsActivity) {
        /*This method intializes the popup window for showing information about the yarn place
        to the firebaseUser
         */

        yarnPlaceInfoWindow = inflate(mapsActivity,R.layout.info_window);

        // Initialize a new instance of popup window
        DisplayMetrics dm = new DisplayMetrics();
        mapsActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        window = initWindow(dm.widthPixels,dm.heightPixels,R.style.popup_window_animation_phone);

        CompatibilityTools.setPopupElevation(window,5.0f);
    }

    private PopupWindow initWindow(int w, int h, int styleID) {
        /*This method initializes the popup window object*/

        PopupWindow window = new PopupWindow(yarnPlaceInfoWindow,(int)(w * 0.6), (int)(h * 0.5));
        window.setAnimationStyle(styleID);
        window.update();
        window.setClippingEnabled(true);

        return window;
    }

    private void initUI(final MapsActivity mapsActivity) {
        /*This method gets all the references to the different View objects that this class
        requires. It also links the different buttons up to their respective button methods and
        initializes text.
         */

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

    private void getPhoto(){
        /*This method downloads the place photo from the google Place API. It's one of the
        requirements for the YarnPlace's ready status
         */

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

    private void getAddress(final Activity activity, final Geocoder geocoder){
        /*This method gets the address for the yarn place from the google Places API. It's one of
        the requirements for the YarnPlace's ready status
         */

        // Specify fields. Requests for photos must always have the PHOTO_METADATAS field.
        List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG);

        // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeMap.get("id"), fields).build();

        placesClient.fetchPlace(placeRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                address = AddressTools.getAddressFromLocation(geocoder
                        ,fetchPlaceResponse.getPlace().getLatLng());

                initYarnPlaceUpdater(activity);

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
    /*This region of the class contains all the button methods which are called when the firebaseUser clicks
    on a UI button
     */

    private void OnCreateChatPressed()
    {
        /* This method is called when the firebaseUser clicks on the create chat button. It brings the firebaseUser
        * to another window which allows them to create a chat*/
        chatCreator.show(this);
    }

    private void OnGoogleMapsPressed(MapsActivity mapsActivity) {
        /*This method runs when he firebaseUser clicks on the google maps button. It takes them to the
        google maps application on their phone with the place looked up
         */

        String map = "http://maps.google.co.in/maps?q=" + address.toString();

        Intent mapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        mapsActivity.startActivity(mapsIntent);
    }

    private void OnJoinChatPressed(Context context,Chat chat) {
        /*This method is run when the firebaseUser clicks on one of the chat's join buttons.
         */

        chat.acceptChat(context,LocalUser.getInstance());
        Recorder.getInstance().recordChat(chat);
    }
    //endregion

    //region Getters and Setters
    /*This region of the class has all the getter and setter methods*/

    public void addChat(Chat chat) {
        /*This method adds a chat to the yarn place but only if it doesn't exist already*/
        if(chats.indexOf(chat) != -1) return;

        chats.add(chat);
    }

    public ArrayList<Chat> getChats(){return chats;}

    //endregion

    //region Public  Methods

    public boolean showInfoWindow(MapsActivity mapsActivity, GoogleMap map) {
        /*This method shows the Yarn place info window to the firebaseUser*/

        if(!checkReady()) return false;
        updateScrollView(mapsActivity);

        setPlacePhoto();
        measureWindow(mapsActivity);

        //Show the window
        if (!window.isShowing()) {
            window.showAtLocation(parentViewGroup, Gravity.NO_GRAVITY, 0, 0);
        }

        updateWindowPosition(map);
        return true;
    }

    public boolean updateWindowPosition(GoogleMap map) {
        /*This method updates the position of the window relative to the position of the touched
        marker
         */

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

    public void dismissInfoWindow() {
        /*This method dismisses the Yarn place info window*/

        if(window != null && window.isShowing()) window.dismiss();
    }

    public void showChats(Activity activity, ArrayList<Chat> chats){
        /*This Method loops over all the chats in the Yarn Place and then adds them to the scroll
        view
         */

        for (Chat chat: chats) {
            addToScrollView(activity, chat);
        }
    }

    public void addToScrollView(final Activity activity, final Chat chat) {
        /*This method adds a chat to the chat scroll view on the the Yarn Place Info window

        /*LayoutInflater inflater = (LayoutInflater) mapsActivity
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View element = inflater.inflate(R.layout.info_window_scroll_view_element,
                parentViewGroup,false);*/

        View element = inflate(activity,R.layout.info_window_scroll_view_element);

        setElementButton(element,activity,chat);
        setElementData(element,chat);

        LinearLayout elements  = chatScrollView.findViewById(R.id.elements);
        elements.addView(element);
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

    public void updateScrollView(Activity activity) {
        /*This method updates the chat scroll view*/

        LinearLayout elements  = chatScrollView.findViewById(R.id.elements);
        elements.removeAllViews();
        showChats(activity,chats);
    }

    public boolean checkReady(){
        /*Checks if the Yarn place is ready for firebaseUser interaction. Its ready once the Chat Updator
        is ready, the place photo has been downloaded and the place address has been retrieved
         */

        return readyListener != null && yarnPlaceUpdaterReady && placePhoto != null && address != null;
    }

    public boolean equals(Object o) {
        //Determines if the passed instance is the same as this one

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

    //region Public Static Methods

    public static HashMap<String, String> buildPlaceMap(String id, String name,String type,
                                                        String lat, String lng){
        //Build the place hashMap

        HashMap<String, String > placeMap = new HashMap<>();
        placeMap.put("id",id);
        placeMap.put("name", name);
        placeMap.put("type",type);
        placeMap.put("lat", lat);
        placeMap.put("lng", lng);

        return placeMap;
    }

    //endregion

    //region Private Methods

    private View inflate(Activity activity, int layoutID ) {
        /*Inflates the given layout ID*/

        LayoutInflater inflater = activity.getLayoutInflater();
        return inflater.inflate(layoutID,null);
    }

    private void measureWindow(MapsActivity mapsActivity) {
        /*Measures the width and height of the pop up window*/

        Display display = mapsActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        yarnPlaceInfoWindow.measure(size.x, size.y);

        windowWidth = yarnPlaceInfoWindow.getMeasuredWidth();
        windowHeight = yarnPlaceInfoWindow.getMeasuredHeight();
    }

    private void setPlacePhoto() {
        //Sets the image view to the Place Photo

        ImageView imageView = yarnPlaceInfoWindow.findViewById(R.id.placePicture);
        imageView.setImageBitmap(placePhoto);
    }

    private void setElementData(View element,Chat chat) {
        //Sets the scroll view element data

        String displayText = chat.chatDate + " " + chat.chatTime;

        element.setContentDescription(chat.yarnPlace.placeMap.get("id"));
        TextView dateTime = element.findViewById(R.id.dateTime);
        dateTime.setText(displayText);
    }

    private void setElementButton(View element, final Activity activity, final Chat chat) {
        //Sets the scroll view element's button

        element.findViewById(R.id.joinChatButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnJoinChatPressed(activity,chat);
            }
        });
    }

    //endregion

}
