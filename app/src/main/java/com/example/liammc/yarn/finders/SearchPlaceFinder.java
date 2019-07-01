package com.example.liammc.yarn.finders;

import android.content.Intent;
import android.util.Log;

import com.example.liammc.yarn.R;
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
import com.google.android.libraries.places.widget.AutocompleteActivity;
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
    AutocompleteSupportFragment searchBar;
    PlacesClient placesClient;
    MapsActivity mapsActivity;
    Intent searchIntent;

    List<Place.Field> placeFields;
    String country;
    TypeFilter typeFilter =  TypeFilter.ESTABLISHMENT;

    public SearchPlaceFinder(MapsActivity _mapsActivity,FinderType type, FinderCallback _listener) {
        this.listener = _listener;
        this.mapsActivity = _mapsActivity;

        this.init();
        if(type == FinderType.WIDGET)this.initSearchWidget();
        else if(type == FinderType.INTENT)this.initSearchIntent();
    }


    //region init

    private void init() {
        /*Initializes the search bar*/

        //Initialize Places
        if (!Places.isInitialized()) {
            Places.initialize(mapsActivity.getApplicationContext(),
                    mapsActivity.getResources().getString(R.string.google_place_android_key));
        }

        placesClient = Places.createClient(mapsActivity);

        //Get the country
        country = mapsActivity.getResources().getConfiguration().locale.getCountry();

        //Set the type fields
        placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.TYPES);
    }

    private void initSearchWidget(){

        // Initialize the AutocompleteSupportFragment.
        searchBar = (AutocompleteSupportFragment)
                mapsActivity.getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Set the search bar
        searchBar.setPlaceFields(placeFields);
        searchBar.setCountry(country);
        searchBar.setTypeFilter(typeFilter);

        initSelectionListener();

    }

    private void initSearchIntent(){

        searchIntent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, placeFields)
                .setCountry(country)
                .setTypeFilter(typeFilter)
                .build(mapsActivity);
    }

    private void initSelectionListener(){
        /*Initializes the search bar selection listener*/

        searchBar.setOnPlaceSelectedListener(new PlaceSelectionListener() {
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
