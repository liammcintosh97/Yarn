package com.example.liammc.yarn.core;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.finders.NearbyPlaceFinder;
import com.example.liammc.yarn.interfaces.FinderCallback;
import com.example.liammc.yarn.notifications.Notifier;
import com.example.liammc.yarn.notifications.TimeChangeReceiver;

import java.util.HashMap;
import java.util.List;

public class WhereToActivity extends AppCompatActivity {


    LocalUser localUser;
    TimeChangeReceiver timeChangeReceiver;

    //Finders
    NearbyPlaceFinder nearbyPlaceFinder;

    //UI
    CheckBox barCheckBox;
    CheckBox cafeCheckBox;
    CheckBox restaurantCheckBox;
    CheckBox nightClubCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where_to);
    }

    private void initLocalUser() {
        /*This method initializes the Local firebaseUser*/

        localUser = LocalUser.getInstance(this);
    }


    private void initReceivers(){

        timeChangeReceiver = new TimeChangeReceiver(this);
        registerReceiver(timeChangeReceiver.receiver,TimeChangeReceiver.intentFilter);
    }

    private void initChannels(){
        Notifier.getInstance().createNotificationChannel(this);
    }

    private void initCheckBoxes() {
        /*Initializes the Check Boxes*/

        /*
        //Get button references
        barCheckBox = findViewById(R.id.barCheckBox);
        cafeCheckBox = findViewById(R.id.cafeCheckBox);
        restaurantCheckBox = findViewById(R.id.restaurantCheckBox);
        nightClubCheckBox = findViewById(R.id.nightClubCheckBox);
        */

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
            }
        });
    }

    private void initUpNearByPlaceFinder() {
        /*Initializes the Nearby By Place Finder*/

        final Activity activity =  this;

        nearbyPlaceFinder = new NearbyPlaceFinder(
                getResources().getString(R.string.google_place_key), (int)localUser.searchRadius, new FinderCallback() {
            @Override
            public void onFoundPlaces(String nextPageToken, List<HashMap<String, String>> placeMaps) {
                /*This is called when the Nearby Place Finder finds some places */

                //addYarnPlaces(placeMaps);
                if(nextPageToken!= null) nearbyPlaceFinder.getPlacesNextPage(nextPageToken);
            }

            @Override
            public void onFoundPlace(HashMap<String, String> placeMap){
                /*This is called when the Nearby Place Finder finds a place */
            }

            @Override
            public void onNoPlacesFound(String message) {
                /*This is called when the Nearby Place Finder doesn't find any places */
                Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
            }
        });
    }
}
