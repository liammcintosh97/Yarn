package com.example.liammc.yarn.yarnPlace;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.chats.Chat;
import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.networking.Communicator;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.interfaces.ReadyListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.GoogleMap;
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

public class YarnPlace {
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

    public static final String PLACE_INFO_REF = "Yarn_Place_Info";
    private String TAG = "Yarn Place";
    public InfoWindow infoWindow;
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

    //Misc
    private boolean yarnPlaceUpdaterReady = false;

    public YarnPlace(HashMap<String, String> _placeMap) {
        this.TAG += (" " + _placeMap.get("id"));
        this.placeMap = _placeMap;
        this.placeType = _placeMap.get("type");
    }

    //region Initialisation
    /*This region of the class has all the methods that are required for initializing the Yarn Place.
    When initializing a Yarn Place we first must run the init method which will initialize services
    and get the Yarn Place's photo and address. Once the Yarn place is ready and we what the firebaseUser to
    interact with it we must run the initOnMap method to initialize the marker, UI and info window
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
        yarnPlaceUpdater.getChats(activity);
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

                    DatabaseReference placeInfoRef = placeRef.child(PLACE_INFO_REF);

                    Communicator.setData(placeInfoRef,"place_name",placeName);
                    Communicator.setData(placeInfoRef,"lat",lat);
                    Communicator.setData(placeInfoRef,"lng",lng);
                    Communicator.setData(placeInfoRef,"country",country);
                    Communicator.setData(placeInfoRef,"admin1",admin1);
                    Communicator.setData(placeInfoRef,"admin2",admin2);
                    Communicator.setData(placeInfoRef,"locality",locality);
                    Communicator.setData(placeInfoRef,"street",street);
                    Communicator.setData(placeInfoRef,"postcode",postCode);
                    Communicator.setData(placeInfoRef,"place_type",type);

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
        infoWindow = new InfoWindow(mapsActivity,this);

        double lat = Double.parseDouble(placeMap.get("lat"));
        double lng = Double.parseDouble( placeMap.get("lng"));

        marker = mapsActivity.createMarker(lat,lng);
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

                placeRef = AddressTools.getPlaceDatabaseReference(
                        address.getCountryName(),address.getAdminArea(),
                        placeMap.get("id"));

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

}
