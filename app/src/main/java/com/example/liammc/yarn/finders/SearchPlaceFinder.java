package com.example.liammc.yarn.finders;

import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.cardview.widget.CardView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.interfaces.FinderCallback;
import com.example.liammc.yarn.yarnPlace.PlaceType;
import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SearchPlaceFinder{
    /*This class determines the functionality of the google place search bar*/

    public enum FinderType {WIDGET,INTENT}

    private final String TAG = "SearchPlaceFinder";
    public FinderCallback listener;
    PlacesClient placesClient;
    MapsActivity mapsActivity;
    Intent searchIntent;
    LocalUser localUser;

    //UI
    public AutocompleteSupportFragment autocompleteSupportFragment;
    Button searchButton;
    CardView searchBar;


    List<Place.Field> placeFields;
    String country;
    TypeFilter typeFilter =  TypeFilter.ESTABLISHMENT;

    public SearchPlaceFinder(MapsActivity _mapsActivity,FinderType type, FinderCallback _listener) {
        this.listener = _listener;
        this.mapsActivity = _mapsActivity;
        this.localUser = LocalUser.getInstance();

        this.init();
        if(type == FinderType.WIDGET)this.initSearchWidget();
        else if(type == FinderType.INTENT)this.initSearchIntent();
    }


    //region init

    private void init() {
        /*Initializes the search bar*/

        searchButton = mapsActivity.findViewById(R.id.searchButton);
        searchBar = mapsActivity.findViewById(R.id.searchBar);

        //Initialize Places
        if (!Places.isInitialized()) {
            Places.initialize(mapsActivity.getApplicationContext(),
                    mapsActivity.getResources().getString(R.string.google_place_android_key));
        }

        placesClient = Places.createClient(mapsActivity);

        //Get the country from GPS
        if(localUser.checkReady()){
            country =  localUser.lastAddress.getCountryCode();
        }
        //Get country from settings if it's null
        if(country == null )country = mapsActivity.getResources().getConfiguration()
                .locale.getCountry();

        //Set the type fields
        placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.TYPES);
    }

    private void initSearchWidget(){

        searchBar.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);

        // Initialize the AutocompleteSupportFragment.
        autocompleteSupportFragment = (AutocompleteSupportFragment)
                mapsActivity.getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Set the search bar
        autocompleteSupportFragment.setPlaceFields(placeFields);
        autocompleteSupportFragment.setCountry(country);
        autocompleteSupportFragment.setTypeFilter(typeFilter);

        initSelectionListener();
    }

    private void initSearchIntent(){

        searchBar.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);

        searchIntent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, placeFields)
                .setCountry(country)
                .setTypeFilter(typeFilter)
                .build(mapsActivity);
    }

    private void initSelectionListener(){
        /*Initializes the search bar selection listener*/

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                HashMap<String,String> placeMap = parsePlaceSearch(place);
                if(placeMap == null) listener.onNoPlacesFound("Sorry you can't create a chat here");
                else listener.onFoundPlace(placeMap);
            }

            @Override
            public void onError(Status status) {
                listener.onNoPlacesFound("There was an error selecting this place");
            }
        });
    }

    //endregion

    //region Public Methods

    public void search(int requestCode){

        if(searchIntent == null){
            Log.e(TAG,"Search intent is null");
            return;
        }

        mapsActivity.startActivityForResult(searchIntent, requestCode);
    }

    public HashMap<String,String> parsePlaceSearch(Place place){

        /*This method processes the selections and parses it into data that's readable by the
        application
         */

        String correctType = translateTypes(place.getTypes());
        if(correctType == null) return null;

        //Build the place map
        HashMap<String, String > placeMap = YarnPlace.buildPlaceMap(place.getId(), place.getName()
                ,correctType, Double.toString(place.getLatLng().latitude),
                Double.toString(place.getLatLng().longitude));

        return placeMap;
    }

    //endregion

    //region Private Methods

    private String translateTypes(List<Place.Type> types) {
        /*This method translates the types from the place into a single type that's recognised by
        the application
         */

        String correctType = null;

        if(types != null) {
            for (int i = 0; i < types.size(); i++) {

                if(types.get(i).toString()
                        .equals(PlaceType.CAFE.toUpperCase()))
                {
                    correctType = PlaceType.CAFE;
                    break;
                }
                else if(types.get(i).toString()
                        .equals(PlaceType.BAR.toUpperCase()))
                {
                    correctType = PlaceType.BAR;
                    break;
                }
                else if(types.get(i).toString()
                        .equals(PlaceType.RESTAURANT.toUpperCase()))
                {
                    correctType = PlaceType.RESTAURANT;
                    break;
                }
                else if(types.get(i).toString()
                        .equals(PlaceType.NIGHT_CLUB.toUpperCase()))
                {
                    correctType = PlaceType.NIGHT_CLUB;
                    break;
                }
                else correctType = null;
            }
        }

        return correctType;
    }

    //endregion
}
