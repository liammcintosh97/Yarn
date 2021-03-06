package com.example.liammc.yarn.utility;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;

public final class AddressTools
{
    /*This class is used for building and formatting addresses both street and database*/

    public static Address buildAddress(Context callingContext, String country, String admin1,
                                       String admin2, String locality, String street,
                                       String postcode) {
        /*Builds an address object using the passed strings*/

        Address address = new Address(callingContext.getResources().getConfiguration().locale);

        address.setCountryCode(country);
        address.setAdminArea(admin1);
        address.setSubAdminArea(admin2);
        address.setLocality(locality);
        address.setAddressLine(0,street);
        address.setPostalCode(postcode);

        return address;
    }

    public static String formatAddress(Address address) {
        /*returns a formatted string from an address object*/

        String country;
        String admin1;
        String admin2;
        String locality;
        String street;
        String postcode;

        country = address.getCountryName();
        admin1 = address.getAdminArea();
        admin2 = address.getSubAdminArea();
        locality = address.getLocality();
        street = address.getAddressLine(0);
        postcode = address.getPostalCode();

        return country + " " + admin1 + " " + admin2 + " " + locality + " " + street +
                " " + postcode;
    }

    public static Address getAddressFromLocation(Geocoder geocoder, LatLng latLng) {
        /*Returns an address object from a LatLng*/

        List<Address> addresses;

        try{
            Log.d("AddressTools","Getting address from LatLng = "
                    + latLng.latitude + ", " + latLng.longitude);

            addresses = geocoder.getFromLocation(latLng.latitude,
                    latLng.longitude, 1);

            return addresses.get(0);
        }
        catch(IOException e)
        {
            Log.e("AddressTools","Unable to get addresses from location \n" + e.toString());
            return null;
        }
    }

    public static DatabaseReference getAdminDatabaseReference(String country,String admin1) {
        /*Returns a DatabaseReference at the admin1 level*/

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(country).child(admin1);
        return ref;
    }

    public static DatabaseReference getPlaceDatabaseReference(String country,String admin1,
                                                             String placeID) {
        /*Returns a DatabaseReference at the place level*/

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(country).child(admin1).child(placeID);
        return ref;
    }

    public static DatabaseReference getChatDatabaseReference(String country,String admin1,
                                                             String placeID, String chatID) {
        /*Returns a DatabaseReference at the chat level*/

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(country).child(admin1).child(placeID).child(chatID);
        return ref;
    }

    public static DatabaseReference getPlaceInfoDatabaseReference(String country, String admin1,
                                                                  String placeID, String yarnInfoKey) {
        /*Returns a DatabaseReference at the Yarn Place Info level*/

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(country).child(admin1).child(placeID).child(yarnInfoKey);
        return ref;
    }

}
