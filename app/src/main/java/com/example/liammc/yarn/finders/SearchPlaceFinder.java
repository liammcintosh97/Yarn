package com.example.liammc.yarn.finders;

import com.example.liammc.yarn.interfaces.FinderCallback;
import com.example.liammc.yarn.yarnPlace.PlaceType;
import com.example.liammc.yarn.yarnPlace.YarnPlace;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SearchPlaceFinder {
    /*This class determines the functionality of the google place search bar*/

    private final String TAG = "SearchPlaceFinder";
    public FinderCallback listener;
    AutocompleteSupportFragment searchBar;

    public SearchPlaceFinder(AutocompleteSupportFragment _searchBar,String country, FinderCallback _listener) {
        this.listener = _listener;
        this.init(_searchBar,country);
    }

    //region init

    private void init(AutocompleteSupportFragment _searchBar, String country) {
        /*Initializes the search bar*/

        // Initialize the AutocompleteSupportFragment.
        searchBar = _searchBar;

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.TYPES);

        searchBar.setPlaceFields(placeFields);
        searchBar.setCountry(country);
        searchBar.setTypeFilter(TypeFilter.ESTABLISHMENT);

        initSelectionListener();
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

    //region Private Methods

    private HashMap<String,String> parsePlaceSearch(Place place){

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
