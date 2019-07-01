package com.example.liammc.yarn.core;

import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.WhereToElement;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.finders.NearbyPlaceFinder;
import com.example.liammc.yarn.interfaces.NearbyPlaceCallback;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.notifications.TimeChangeReceiver;
import com.example.liammc.yarn.yarnPlace.PlaceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WhereToActivity extends AppCompatActivity {

    LocalUser localUser;
    TimeChangeReceiver timeChangeReceiver;
    ArrayList<WhereToElement> elements = new ArrayList<>();

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where_to);

        initLocalUser();
        initReceivers();
        initChannels();
        initUI();
        initFinderCallback();
        initFinders();
    }

    //region Initialisation

    private void initLocalUser() {
        /*This method initializes the Local firebaseUser*/

        localUser = LocalUser.getInstance(this);
    }

    private void initUI(){
        searchButton = findViewById(R.id.searchButton);

        scrollViewElements = findViewById(R.id.elements);
        scrollViewElements.removeAllViews();

        initCheckBoxes();
        clearElements();
    }

    private void initReceivers(){

        timeChangeReceiver = new TimeChangeReceiver(this);

        try {
            registerReceiver(timeChangeReceiver.receiver,TimeChangeReceiver.intentFilter);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void initChannels(){
        Notifier.getInstance().createNotificationChannel(this);
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
                else if (checkReady()) searchButton.setEnabled(true);
            }

            @Override
            public void onNoPlacesFound(String message) {
                /*This is called when the Nearby Place Finder doesn't find any places */
                if (checkReady()){
                    Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
                    searchButton.setEnabled(true);
                }
            }
        };
    }

    private void initFinders() {
        /*Initializes the Nearby By Place Finder*/

        cafeFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.google_place_key), (int)localUser.searchRadius, finderCallback);

        barFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.google_place_key), (int)localUser.searchRadius, finderCallback);

        resturantFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.google_place_key), (int)localUser.searchRadius, finderCallback);

        nightClubFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.google_place_key), (int)localUser.searchRadius, finderCallback);
    }

    //endregion

    //region Buttons Methods

    public void onSearchPressed(View view){
        updateUserTypes();
        clearElements();

        initFinders();

        searchButton.setEnabled(false);

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

        for(HashMap<String,String> map : placeMaps){

            if(!exists(map)) {
                WhereToElement element = new WhereToElement(this, map);
                elements.add(element);
                scrollViewElements.addView(element.elementView);
            }
        }
    }

    private boolean exists(HashMap<String,String> placeMap){

        if(elements == null || elements.size() == 0)return false;

        for(WhereToElement e : elements){
            if(e.placeMap.get("id").equals(placeMap.get("id"))) return true;
        }
        return false;
    }

    private void updateUserTypes(){

        ArrayList<String> types = new ArrayList<>();

        if(cafeCheckBox.isChecked()) types.add(PlaceType.CAFE);
        if(barCheckBox.isChecked()) types.add(PlaceType.BAR);
        if(restaurantCheckBox.isChecked()) types.add(PlaceType.RESTAURANT);
        if(nightClubCheckBox.isChecked()) types.add(PlaceType.NIGHT_CLUB);

        localUser.types = types;
    }

    private boolean removePlace(String placeID){

        WhereToElement elementToRemove = null;

        for(WhereToElement e : elements){
            if(e.placeMap.get("id").equals(placeID)){
                elementToRemove = e;
                elements.remove(e);
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
        elements.clear();
        scrollViewElements.removeAllViews();
    }

    private boolean checkReady(){

        if(cafeFinder.done &&
            resturantFinder.done &&
            barFinder.done &&
            nightClubFinder.done) return true;

        return false;
    }

    //endregion
}
