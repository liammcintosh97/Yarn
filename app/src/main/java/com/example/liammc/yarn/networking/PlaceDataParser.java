package com.example.liammc.yarn.networking;

import android.util.Log;

import com.example.liammc.yarn.yarnPlace.YarnPlace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceDataParser {
    /*This class is used for passing Place JSON data and then returning a list of placeMaps*/

    String[] invalidPlaceTypes = {"gas_station","supermarket","store","shopping_mall","pharmacy"
    ,"meal_takeaway","meal_delivery","lodging","liquor_store",};

    public List<HashMap<String, String>> parse(String jsonData, String type) {
        /*This method is used when we need to parse some JSONData and return a List of PlaceMaps*/

        JSONArray jsonArray = null;
        JSONObject jsonObject;

        Log.d("json data", jsonData);

        try {
            //Read the JSON data and get it's results
            jsonObject = new JSONObject(jsonData);
            if(!jsonObject.getString("status").equals("ZERO_RESULTS")){
                jsonArray = jsonObject.getJSONArray("results");
            }
            else return null;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Get the Places from the JSONArray and return the results
        if(jsonArray != null)return getJsonPlaces(jsonArray,type);
        else return null;
    }

    private List<HashMap<String, String>>getJsonPlaces(JSONArray jsonArray, String type) {
        /*This method gets all the Place Maps from the JSONArray containing the Place data*/

        List<HashMap<String, String>> placelist = new ArrayList<>();

        //Loop over the JSON array and extract the data
        for(int i = 0; i<jsonArray.length();i++)
        {
            try {
                //Get the Object at the index
                JSONObject jsonObject =(JSONObject) jsonArray.get(i);

                //Extract the data and put it in the result list
                HashMap<String, String> placeMap = getJsonPlace(jsonObject,type);
                if(placeMap != null) placelist.add(placeMap);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placelist;
    }

    private HashMap<String, String> getJsonPlace(JSONObject googlePlaceJson, String type) {
        /*This method build a Place Map from a JSON object containing the data*/

        /*Create a new Google Place Map*/
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String id = "";
        String name = "--NA--";
        String lat= "";
        String lng="";

        Log.d("DataParser","jsonobject ="+googlePlaceJson.toString());

        try {
            //Check if the name isn't null
            if (!googlePlaceJson.isNull("name")) {
                name = googlePlaceJson.getString("name");
            }
            //Check if the place ID isn't null
            if(!googlePlaceJson.isNull("place_id"))
            {
                id = googlePlaceJson.getString("place_id");
            }
            //Check is the place types isn't null
            if(!googlePlaceJson.isNull("types")){
                if(!validPlace(googlePlaceJson.getJSONArray("types"))){
                    Log.d("DataParser","The place is of an invalid type");
                    return null;
                }
            }

            //Gets the Latitude and Longitude
            lat = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            lng = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            //Builds the Place Map
            googlePlaceMap = YarnPlace.buildPlaceMap(id,name,type,lat,lng);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        //return the result
        return googlePlaceMap;
    }

    private boolean validPlace(JSONArray types){

        for(int i = 0; i < types.length();i++){

            for(int y = 0 ; y < invalidPlaceTypes.length; y++){
                try{
                    if(types.getString(i).equals(invalidPlaceTypes[y]))return false;
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }

        return true;
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
