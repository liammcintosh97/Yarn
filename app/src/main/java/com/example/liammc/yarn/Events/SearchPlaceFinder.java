package com.example.liammc.yarn.Events;

import android.support.v4.app.FragmentManager;
import android.util.Log;
import com.example.liammc.yarn.FinderCallback;
import com.example.liammc.yarn.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SearchPlaceFinder {

    private final String TAG = "SearchPlaceFinder";
    public FinderCallback listener;
    AutocompleteSupportFragment searchBar;

    public SearchPlaceFinder(AutocompleteSupportFragment _searchBar,String country, FinderCallback _listener) {
        this.listener = _listener;
        this.setUpSearchBar(_searchBar,country);
    }

    //region Private methods

    private void setUpSearchBar(AutocompleteSupportFragment _searchBar,String country)
    {
        // Initialize the AutocompleteSupportFragment.
        searchBar = _searchBar;

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.TYPES);

        searchBar.setPlaceFields(placeFields);
        searchBar.setCountry(country);
        searchBar.setTypeFilter(TypeFilter.ESTABLISHMENT);

        searchBar.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                HashMap<String,String> placeMap = processPlaceSearch(place);
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

    //region Utility

    private HashMap<String,String> processPlaceSearch(Place place){

        String correctType = translateTypes(place.getTypes());



        HashMap<String, String > placeMap = buildPlaceMap(place.getId(), place.getName(),correctType,
                Double.toString(place.getLatLng().latitude),
                Double.toString(place.getLatLng().longitude));

        if(correctType != null) return placeMap;
        else return null;
    }

    private String translateTypes(List<Place.Type> types) {
        Log.d(TAG,types.toString());

        String correctType = null;

        if(types != null) {
            for (int i = 0; i < types.size(); i++) {

                if(types.get(i).toString()
                        .equals(YarnPlace.PlaceType.CAFE.toUpperCase()))
                {
                    correctType = YarnPlace.PlaceType.CAFE;
                    break;
                }
                else if(types.get(i).toString()
                        .equals(YarnPlace.PlaceType.BAR.toUpperCase()))
                {
                    correctType = YarnPlace.PlaceType.BAR;
                    break;
                }
                else if(types.get(i).toString()
                        .equals(YarnPlace.PlaceType.RESTAURANT.toUpperCase()))
                {
                    correctType = YarnPlace.PlaceType.RESTAURANT;
                    break;
                }
                else if(types.get(i).toString()
                        .equals(YarnPlace.PlaceType.NIGHT_CLUB.toUpperCase()))
                {
                    correctType = YarnPlace.PlaceType.NIGHT_CLUB;
                    break;
                }
                else correctType = null;
            }
        }

        return correctType;
    }

    private HashMap<String, String> buildPlaceMap(String id, String name,String type, String lat, String lng){

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
