package com.example.liammc.yarn.interfaces;

import com.example.liammc.yarn.finders.NearbyPlaceFinder;

import java.util.HashMap;
import java.util.List;

public interface NearbyPlaceCallback {

    void onFoundPlaces(NearbyPlaceFinder finder,String nextPageToken, List<HashMap<String, String>> placeMaps);
    void onNoPlacesFound(String message);

}
