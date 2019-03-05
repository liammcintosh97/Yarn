package com.example.liammc.yarn;

import java.util.HashMap;
import java.util.List;

public interface FinderCallback {

    void onFoundPlaces(String nextPageToken,List<HashMap<String, String>> placeMaps);
    void onFoundPlace(HashMap<String,String> placeMap);
    void onNoPlacesFound(String message);

}
