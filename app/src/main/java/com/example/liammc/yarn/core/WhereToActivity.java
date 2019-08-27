package com.example.liammc.yarn.core;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.finders.NearbyPlaceFinder;
import com.example.liammc.yarn.interfaces.NearbyPlaceCallback;
import com.example.liammc.yarn.userInterface.LoadingSymbol;
import com.example.liammc.yarn.yarnPlace.PlaceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WhereToActivity extends YarnActivity {

    WhereToElement[] elements = null;

    //Finders
    NearbyPlaceCallback finderCallback;
    NearbyPlaceFinder barFinder;
    NearbyPlaceFinder cafeFinder;
    NearbyPlaceFinder resturantFinder;
    NearbyPlaceFinder nightClubFinder;

    //UI
    Button searchButton;
    LinearLayout scrollViewElements;
    CheckBox barCheckBox;
    CheckBox cafeCheckBox;
    CheckBox restaurantCheckBox;
    CheckBox nightClubCheckBox;
    LoadingSymbol loadingSymbol;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where_to);


        initUI();
        initFinderCallback();
        initFinders();
    }

    //region Initialisation

    private void initUI(){
        searchButton = findViewById(R.id.searchButton);
        loadingSymbol = new LoadingSymbol(this);

        scrollViewElements = findViewById(R.id.elements);
        scrollViewElements.removeAllViews();

        initCheckBoxes();
        initTypes();
        clearElements();
    }


    private void initCheckBoxes() {
        /*Initializes the Check Boxes*/

        //Get button references
        barCheckBox = findViewById(R.id.barsCheckBox);
        cafeCheckBox = findViewById(R.id.cafeCheckBox);
        restaurantCheckBox = findViewById(R.id.resturantsCheckBox);
        nightClubCheckBox = findViewById(R.id.nightClubsCheckBox);

        //Set button values
        barCheckBox.setChecked(false);
        restaurantCheckBox.setChecked(false);
        nightClubCheckBox.setChecked(false);

        cafeCheckBox.setChecked(true);

        //Set button listeners
        barCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //If the buttons are all unchecked check the cafe one;
                if(!cafeCheckBox.isChecked() && !barCheckBox.isChecked()
                        && !restaurantCheckBox.isChecked() && !nightClubCheckBox.isChecked()){
                    cafeCheckBox.setChecked(true);
                }

                updateUserTypes();
            }
        });

        cafeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //If the buttons are all unchecked check the cafe one;
                if(!cafeCheckBox.isChecked() && !barCheckBox.isChecked()
                        && !restaurantCheckBox.isChecked() && !nightClubCheckBox.isChecked()){
                    cafeCheckBox.setChecked(true);
                }

                updateUserTypes();
            }
        });

        restaurantCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //If the buttons are all unchecked check the cafe one;
                if(!cafeCheckBox.isChecked() && !barCheckBox.isChecked()
                        && !restaurantCheckBox.isChecked() && !nightClubCheckBox.isChecked()){
                    cafeCheckBox.setChecked(true);
                }

                updateUserTypes();
            }
        });

        nightClubCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //If the buttons are all unchecked check the cafe one;
                if(!cafeCheckBox.isChecked() && !barCheckBox.isChecked()
                        && !restaurantCheckBox.isChecked() && !nightClubCheckBox.isChecked()){
                    cafeCheckBox.setChecked(true);
                }

                updateUserTypes();
            }
        });
    }

    private void initFinderCallback(){

        final Activity activity =  this;

        finderCallback = new NearbyPlaceCallback() {
            @Override
            public void onFoundPlaces(NearbyPlaceFinder finder,
                                      String nextPageToken, List<HashMap<String, String>> placeMaps) {
                /*This is called when the Nearby Place Finder finds some places */

                addYarnPlaces(placeMaps);
                if (nextPageToken != null) finder.getPlacesNextPage(nextPageToken);
                else if (checkReady()) showResults();
            }

            @Override
            public void onNoPlacesFound(String message) {
                /*This is called when the Nearby Place Finder doesn't find any places */
                if (checkReady()){
                    Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
                    showResults();
                }
            }
        };
    }

    private void initFinders() {
        /*Initializes the Nearby By Place Finder*/

        cafeFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.Yarn_web_API_key), (int)localUser.searchRadius, finderCallback);

        barFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.Yarn_web_API_key), (int)localUser.searchRadius, finderCallback);

        resturantFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.Yarn_web_API_key), (int)localUser.searchRadius, finderCallback);

        nightClubFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.Yarn_web_API_key), (int)localUser.searchRadius, finderCallback);
    }

    private void initTypes(){

        String[] userTypes = localUser.types;

        for(int i = 0; i < userTypes.length; i++){

            if(userTypes[i].equals(PlaceType.CAFE)) cafeCheckBox.setChecked(true);
            else if(userTypes[i].equals(PlaceType.RESTAURANT)) restaurantCheckBox.setChecked(true);
            else if(userTypes[i].equals(PlaceType.BAR)) barCheckBox.setChecked(true);
            else if(userTypes[i].equals(PlaceType.NIGHT_CLUB)) nightClubCheckBox.setChecked(true);
        }
    }

    //endregion

    //region Buttons Methods

    public void onSearchPressed(View view){
        updateUserTypes();
        clearElements();

        initFinders();

        showLoading();

        if(cafeCheckBox.isChecked()){
            cafeFinder.getNearbyPlaces(PlaceType.CAFE);
        }
        if(barCheckBox.isChecked()){
            barFinder.getNearbyPlaces(PlaceType.BAR);
        }
        if(restaurantCheckBox.isChecked()){
            resturantFinder.getNearbyPlaces(PlaceType.RESTAURANT);
        }
        if(nightClubCheckBox.isChecked()){
            nightClubFinder.getNearbyPlaces(PlaceType.NIGHT_CLUB);
        }
    }

    //endregion

    //region Private Methods

    private void addYarnPlaces(List<HashMap<String, String>> placeMaps){

        elements = new WhereToElement[placeMaps.size()];

        for(int i = 0; i < placeMaps.size(); i++){

            HashMap<String,String> map =  placeMaps.get(i);

            if(!exists(map)) {
                WhereToElement element = new WhereToElement(this, map);
                elements[i] =  element;
            }
        }

        sort();
        addViews();
    }

    private boolean exists(HashMap<String,String> placeMap){

        if(elements == null || elements.length == 0)return false;

        for(WhereToElement e : elements){
            if(e != null &&
                    e.placeMap.get("id").equals(placeMap.get("id"))) return true;
        }
        return false;
    }

    private void sort(){

        for (int i = 0; i < elements.length; i++) {

            for (int j = i + 1; j < elements.length; j++) {

                if (elements[i].distance > elements[j].distance) {

                    WhereToElement temp = elements[i];
                    elements[i] = elements[j];
                    elements[j] = temp;

                }
            }
        }
    }

    private void addViews(){
        for(WhereToElement e : elements){
            scrollViewElements.removeView(e.elementView);
            scrollViewElements.addView(e.elementView);
        }
    }

    private void updateUserTypes(){

        String[] newUserTypes = new String[4];

        if(cafeCheckBox.isChecked()) newUserTypes[0] = PlaceType.CAFE;
        if(barCheckBox.isChecked()) newUserTypes[1] = PlaceType.BAR;
        if(restaurantCheckBox.isChecked()) newUserTypes[2] = PlaceType.RESTAURANT;
        if(nightClubCheckBox.isChecked()) newUserTypes[3] = PlaceType.NIGHT_CLUB;

        localUser.types =  newUserTypes;
    }

    private boolean removePlace(String placeID){

        WhereToElement elementToRemove = null;

        for(int i = 0; i < elements.length; i++){

            if(elements[i].placeMap.get("id").equals(placeID)){

                elementToRemove = elements[i];
                elements[i] = null;
                break;
            }
        }

        if(elementToRemove != null){
            scrollViewElements.removeView(elementToRemove.elementView);
            return true;
        }
        else return false;
    }

    private void clearElements(){
        elements = null;
        scrollViewElements.removeAllViews();
    }

    private boolean checkReady(){

        if(cafeFinder.done &&
            resturantFinder.done &&
            barFinder.done &&
            nightClubFinder.done) return true;

        return false;
    }

    private void showLoading(){
        searchButton.setClickable(false);
        searchButton.setEnabled(false);
        scrollViewElements.setVisibility(View.INVISIBLE);
        loadingSymbol.start();
    }

    private void showResults(){
        searchButton.setClickable(true);
        searchButton.setEnabled(true);
        scrollViewElements.setVisibility(View.VISIBLE);
        loadingSymbol.stop();
    }
    //endregion
}
