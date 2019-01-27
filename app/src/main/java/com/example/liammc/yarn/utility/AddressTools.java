package com.example.liammc.yarn.utility;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;

public final class AddressTools
{
    public static Address buildAddress(Context callingContext, String country, String admin1,
                                       String admin2, String locality, String street,
                                       String postcode)
    {
        Address address = new Address(callingContext.getResources().getConfiguration().locale);

        address.setCountryCode(country);
        address.setAdminArea(admin1);
        address.setSubAdminArea(admin2);
        address.setLocality(locality);
        address.setAddressLine(0,street);
        address.setPostalCode(postcode);

        return address;
    }

    public static String formatAddress(Address address)
    {
        String country;
        String admin1;
        String admin2;
        String locality;
        String street;
        String postcode;

        country = address.getCountryCode();
        admin1 = address.getAdminArea();
        admin2 = address.getSubAdminArea();
        locality = address.getLocality();
        street = address.getAddressLine(0);
        postcode = address.getPostalCode();

        return country + " " + admin1 + " " + admin2 + " " + locality + " " + street +
                " " + postcode;
    }

    public static Address getAddressFromLocation(Geocoder geocoder, LatLng latLng)
    {
        List<Address> addresses;

        try{
            addresses = geocoder.getFromLocation(latLng.latitude,
                    latLng.longitude, 1);

            return addresses.get(0);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Log.e("YarnPlace","Unable to get addresses from location");
            return null;
        }
    }

    public static DatabaseReference getChatDatabaseReference(String placeID)
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Chats");
        return ref.child(placeID);
    }

}
