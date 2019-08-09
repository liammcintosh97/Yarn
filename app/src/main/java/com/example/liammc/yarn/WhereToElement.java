package com.example.liammc.yarn;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.core.WhereToActivity;
import com.example.liammc.yarn.utility.AddressTools;
import com.example.liammc.yarn.utility.MathTools;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;

public class WhereToElement {

    private LocalUser localUser;
    private WhereToActivity whereToActivity;
    public HashMap<String,String> placeMap;

    //UI
    public View elementView;
    private TextView placeNameTextView;
    private TextView distanceTextView;
    private TextView chatsTextView;
    private Button goToButton;

    public WhereToElement(WhereToActivity _whereToActivity, HashMap<String,String> _placeMap){

        this.whereToActivity = _whereToActivity;
        this.placeMap = _placeMap;

        this.initLocalUser();
        this.initUI();
    }

    //region Initialization

    private void initUI(){

        elementView =  inflate(R.layout.element_where_to);

        goToButton = elementView.findViewById(R.id.goButton);
        goToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGoToPressed();
            }
        });

        placeNameTextView = elementView.findViewById(R.id.placeName);

        distanceTextView = elementView.findViewById(R.id.distanceValue);

        chatsTextView = elementView.findViewById(R.id.chatsValue);

        update();
    }

    private void initLocalUser(){
        localUser =  LocalUser.getInstance();
    }
    //endregion

    //region Public Methods

    public void update(){
        placeNameTextView.setText(placeMap.get("name"));
        String dis = getDistance() + "m";
        distanceTextView.setText(dis);

        getChatCount();
    }

    //endregion

    //region Buttons Methods

    private void onGoToPressed(){
        Intent data = new Intent();

        data.putExtra("placeMap",placeMap);
        whereToActivity.setResult(MapsActivity.RESULT_OK, data);
        whereToActivity.finish();
    }

    //endregion

    //region private Methods

    private View inflate(int layoutID ) {
        /*Inflates the given layout ID*/

        LayoutInflater inflater = whereToActivity.getLayoutInflater();
        return inflater.inflate(layoutID,null);
    }

    private long getDistance(){

        String lat = placeMap.get("lat");
        String lng  = placeMap.get("lng");

        double placeLat =  Double.valueOf(lat);
        double placeLng =  Double.valueOf(lng);

        return (long)MathTools.latLngDistance(localUser.lastLatLng.latitude, localUser.lastLatLng.longitude,
                placeLat,placeLng);
    }

    private void getChatCount(){

        Geocoder geocoder = new Geocoder(whereToActivity, Locale.getDefault());

        LatLng placeLatLng = new LatLng(Double.valueOf(placeMap.get("lat"))
                ,Double.valueOf(placeMap.get("lng")));

        Address address = AddressTools.getAddressFromLocation(geocoder
                ,placeLatLng);

        DatabaseReference placeRef = AddressTools.getPlaceDatabaseReference(address.getCountryName()
                ,address.getAdminArea(),placeMap.get("id"));

        placeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String chatCount = String.valueOf(dataSnapshot.getChildrenCount());
                chatsTextView.setText(chatCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //endregion
}
