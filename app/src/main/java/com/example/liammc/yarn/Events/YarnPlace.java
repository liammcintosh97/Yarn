package com.example.liammc.yarn.Events;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

public class YarnPlace
{
    public HashMap<String, String>  placeMap;
    public Marker marker;
    public List<Chat> chats;

    private GoogleMap map;

    public YarnPlace(GoogleMap _map, HashMap<String, String> _placeMap)
    {
        this.placeMap = _placeMap;
        this.map = _map;

        this.createMarker();
    }

    private void createMarker()
    {
        MarkerOptions markerOptions = new MarkerOptions();

        String placeName = placeMap.get("place_name");
        String vicinity = placeMap.get("vicinity");

        double lat = Double.parseDouble( placeMap.get("lat"));
        double lng = Double.parseDouble( placeMap.get("lng"));

        LatLng latLng = new LatLng( lat, lng);
        markerOptions.position(latLng);
        markerOptions.title(placeName + " : "+ vicinity);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        marker = map.addMarker(markerOptions);
    }
}
