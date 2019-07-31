package com.example.liammc.yarn.accounting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import android.util.Log;

import com.example.liammc.yarn.interfaces.ReadyListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;


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
    public final UserUpdater updator;

    //Firebase
    protected StorageReference userStorageReference;
    public DatabaseReference userDatabaseReference;

    //User info
    public String userID;
    public String userName;
    public Bitmap profilePicture;
    public String email;
    public Long flags = 0L;
    public String birthDate;
    public int age;
    public String gender;
    protected Iterable<DataSnapshot> ratings = null;
    public long meanRating;
    public Boolean termsAcceptance = null;

    //region Constructors
    public YarnUser(){
        updator = new UserUpdater(this);
    }

    public YarnUser(String _userID) {

        initDatabaseReferences(_userID);
        initUser();
        updator = new UserUpdater(this);
    }
    //endregion

    //region Init
    public void initUser(){
        /*Initializes the firebaseUser's information by getting it from Firebase*/

        getUserName();
        getUserProfilePicture();
        getUserRatings();
        getUserTermAcceptance();
        getBirthDate();
        getGender();
        getFlags();
    }

    public void initDatabaseReferences(String _userID){
        /*Initialize the database references*/

        userID = _userID;

        userStorageReference = FirebaseStorage.getInstance().getReference().child("Users").child(userID);
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
    }
    //endregion

    //region Private Methods

    private void getUserName() {
        /*Gets the firebaseUser's name by attaching a listener to the userDatabase reference. Location is
        * Users/[userID]/userName*/

        userDatabaseReference.child("userName").addValueEventListener(new ValueEventListener() {
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

    public void getUserRatings(){
        userDatabaseReference
                .child("ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Try to cast the meanRating into a double
                ratings = snapshot.getChildren();
                Log.d(TAG,"Got firebaseUser rates");

                meanRating = calculateMeanRating(ratings);

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

    private void getUserTermAcceptance() {
        /*Gets the firebaseUser's term acceptance by attaching a listener to the userDatabase reference.
        Location is Users/[userID]/TermsAcceptance*/

        userDatabaseReference
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

    private void getBirthDate(){
        userDatabaseReference
                .child("birthDate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Try to cast the meanRating into a double
                birthDate = (String) snapshot.getValue();
                age =  calculateAge(birthDate);
                Log.d(TAG,"Got firebaseUser birth date");

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

    private void getGender(){
        userDatabaseReference
                .child("gender").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Try to cast the meanRating into a double
                gender = (String) snapshot.getValue();
                Log.d(TAG,"Got firebaseUser gender");

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

    private void getFlags(){
        userDatabaseReference
                .child("flags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Try to cast the meanRating into a double

                if(snapshot.getValue() == null) return;
                flags = (Long) snapshot.getValue();

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
          meanRating and terms acceptance*/

        boolean ready = readyListener != null &&
                        profilePicture != null &&
                        userName != null &&
                        ratings != null &&
                        birthDate != null &&
                        gender != null &&
                        termsAcceptance != null;

        return ready;
    }

    private long calculateMeanRating(Iterable<DataSnapshot> ratings){

        double total = 0;
        double amount = 0;
        for ( ; ratings.iterator().hasNext() ; ++amount ) ratings.iterator().next();
        long result = 0;

        //Get the total ratings
        for(DataSnapshot rate : ratings){
            total += (int)rate.getValue();
        }
        result = Math.round(total/amount);

        Log.d(TAG,"The user's mean rating is " + result);
        return result;
    }

    private int calculateAge(String birthDate){

        int birthYear =  Integer.valueOf(birthDate.substring(6));
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        return currentYear -  birthYear;
    }
    //endregion

}
