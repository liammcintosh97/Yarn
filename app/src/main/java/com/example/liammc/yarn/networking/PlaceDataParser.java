package com.example.liammc.yarn.networking;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceDataParser
{
    public List<HashMap<String, String>> parse(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        Log.d("json data", jsonData);

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonArray != null)return getJsonPlaces(jsonArray);
        else return null;
    }

    private List<HashMap<String, String>>getJsonPlaces(JSONArray jsonArray)
    {
        int count = jsonArray.length();
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for(int i = 0; i<count;i++)
        {
            try {
                placeMap = getJsonPlace((JSONObject) jsonArray.get(i));
                placelist.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placelist;
    }

    private HashMap<String, String> getJsonPlace(JSONObject googlePlaceJson)
    {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String id = "";
        String placeName = "--NA--";
        //String address = "--NA--";
        String vicinity= "--NA--";
        String latitude= "";
        String longitude="";
        String reference="";

        Log.d("DataParser","jsonobject ="+googlePlaceJson.toString());

        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");
            }
            if(!googlePlaceJson.isNull("place_id"))
            {
                id = googlePlaceJson.getString("place_id");
            }

            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            reference = googlePlaceJson.getString("reference");

            //address = buildAddress(googlePlaceJson);

            googlePlaceMap.put("id",id);
            googlePlaceMap.put("name", placeName);
            //googlePlaceMap.put("address",address);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;
    }

    /*
    private String buildAddress(JSONObject googlePlaceJson)
    {
        String address;

        String country = "NA";
        String admin1 = "NA";
        String admin2 = "NA";
        String locality = "NA";
        String route = "NA";
        String number = "NA";

        if(!googlePlaceJson.isNull("address_components"))
        {
            try
            {
                JSONArray jsonAddress = googlePlaceJson.getJSONArray("address_components");
                JSONObject component;
                JSONArray type;

                for(int i= 0; i < jsonAddress.length(); i++)
                {
                    component = jsonAddress.getJSONObject(i);
                    type = component.getJSONArray("type");

                    switch (type.getString(0))
                    {
                        case("country") : country = component.getString("short_name");
                        case("administrative_area_level_1") : admin1 = component.getString("short_name");
                        case("administrative_area_level_2") : admin2 = component.getString("short_name");
                        case("locality") : locality = component.getString("short_name");
                        case("route") : route = component.getString("short_name");
                        case("street_number") : number = component.getString("short_name");
                    }
                }

                address = country + " " + admin1 + " " + admin2 + " " + locality + " " + route + " " + number;
                return address;
            }
            catch(org.json.JSONException e)
            {
                e.printStackTrace();
                return null;
            }

        }
        else{
            Log.d("DataParser",
                    "Couldn't build address because address components are null");
            return null;
        }
    }*/
}
