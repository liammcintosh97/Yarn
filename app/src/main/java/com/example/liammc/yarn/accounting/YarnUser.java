package com.example.liammc.yarn.accounting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.interfaces.ReadyListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class YarnUser {
    /*This class defines a firebaseUser within the application*/

    //region Ready Listener
    /*This listener is used to alert the application when an instance of the class is ready for
    interaction*/

    protected ReadyListener readyListener;

    public ReadyListener getReadyListener() {
        return readyListener;
    }

    public void setReadyListener(ReadyListener readyListener) {
        this.readyListener = readyListener;
    }
    //endregion

    //endregion
    private final String TAG = "YarnUser";

    //Firebase
    protected StorageReference userStorageReference;
    protected DatabaseReference userDatabaseReference;

    //User info
    public String userID;
    public String userName;
    public Bitmap profilePicture;
    public String email;
    public Double rating = null;
    public Boolean termsAcceptance = null;

    //region Constructors
    public YarnUser(){
        initDatabaseReferences();
    }

    public YarnUser(String _userID) {
        initDatabaseReferences();
        initUser(_userID);
    }
    //endregion

    //region Init
    public void initUser(String _userID){
        /*Initializes the firebaseUser's information by getting it from Firebase*/

        userID = _userID;
        getUserName();
        getUserProfilePicture();
        getUserRating();
        getUserTermAcceptance();
    }

    private void initDatabaseReferences(){
        /*Initialize the database references*/

        userStorageReference = FirebaseStorage.getInstance().getReference().child("Users");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }
    //endregion

    //region Private Methods

    private void getUserName() {
        /*Gets the firebaseUser's name by attaching a listener to the userDatabase reference. Location is
        * Users/[userID]/userName*/

        userDatabaseReference
                .child(userID)
                .child("userName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /*Runs when the data at this location changes. Also runs the first time the listener
                is added*/

                //Get the data
                userName = (String) snapshot.getValue();
                Log.d(TAG,"Got username");

                //Check if the firebaseUser is ready after getting the username
                if(checkReady()){
                    readyListener.onReady();
                    readyListener = null;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //There was an error so log it.
                Log.e(TAG,"Unable to get value from database - " + databaseError.getMessage());
            }
        });
    }

    private void getUserProfilePicture() {
        /*Gets the firebaseUser's profile picture by getting the bytes from the firebaseUser storage reference.
        Location is Users/[userID]/profilePicture*/

        final long ONE_MEGABYTE = 1024 * 1024;

        userStorageReference
                .child(userID)
                .child("profilePicture")
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                /*When the application has received all the bytes from storage this method is run*/

                //Decode the downloaded bytes into a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                if(bitmap != null) {
                    profilePicture = bitmap;
                    Log.d(TAG,"Got profile picture");

                    //Check if the firebaseUser is ready after getting the profile picture
                    if(checkReady()){
                        readyListener.onReady();
                        readyListener = null;
                    }
                }
                else {
                    //There was an exception when trying to decode the bytes into a bitmap
                    Log.d(TAG,"Unable to decode byte array");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                /*This runs if the application fails to download the bytes so log the error*/
                Log.d(TAG,"Failed to download profile picture - " + exception.getMessage());
            }
        });
    }

    private void getUserRating() {
        /*Gets the firebaseUser's ratting by attaching a listener to the userDatabase reference. Location is
         * Users/[userID]/rating*/

        userDatabaseReference
                .child(userID)
                .child("rating").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /*Runs when the data at this location changes. Also runs the first time the
                        listener is added*/

                        /*Get the data. The rating can either be a long or a double so the application
                        must cast either or*/
                        try {
                            //Try to cast the rating into a double
                            rating = (double) snapshot.getValue();
                            Log.d(TAG,"Got firebaseUser rating");

                            //Check if the firebaseUser is ready after getting the username
                            if(checkReady()){
                                readyListener.onReady();
                                readyListener = null;
                            }
                        }
                        catch (ClassCastException e) {
                            /*Their was an exception when casting to a double so try and cast to a
                            long*/
                            Long l = (long)snapshot.getValue();
                            rating = l.doubleValue();
                            Log.d(TAG,"Got firebaseUser rating");

                            //Check if the firebaseUser is ready after getting the username
                            if(checkReady()){
                                readyListener.onReady();
                                readyListener = null;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //There was an error so log it.
                        Log.e(TAG,"Unable to get value from database");
                    }
                });
    }

    private void getUserTermAcceptance() {
        /*Gets the firebaseUser's term acceptance by attaching a listener to the userDatabase reference.
        Location is Users/[userID]/TermsAcceptance*/

        userDatabaseReference
                .child(userID)
                .child("TermsAcceptance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                /*Runs when the data at this location changes. Also runs the first time the listener
                is added*/

                //Get the data
                 termsAcceptance = (boolean)snapshot.getValue();
                Log.d(TAG,"Got terms acceptance");

                //Check if the firebaseUser is ready after getting the username
                if(checkReady()){
                    readyListener.onReady();
                    readyListener = null;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //There was an error so log it.
                Log.e(TAG,"Unable to get value from database");
            }
        });
    }

    private boolean checkReady(){
        /*Checks is the firebaseUser is ready. The firebaseUser is considered ready when they have a picture, name,
          rating and terms acceptance*/

        boolean ready = readyListener != null &&
                        profilePicture != null &&
                        userName != null &&
                        rating != null &&
                        termsAcceptance != null;

        return ready;
    }

    //endregion

}
