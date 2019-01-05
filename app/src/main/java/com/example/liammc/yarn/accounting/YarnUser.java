package com.example.liammc.yarn.accounting;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class YarnUser
{
    private final FirebaseUser firebaseUser;
    private final StorageReference userStorageReferance;
    private final DatabaseReference userDatabaseReference;
    private final String CALLINGTAG;

    //User info
    public String userName;
    public Bitmap profilePicture;
    public String email;
    public double rating;
    public boolean termsAcceptance;

    public YarnUser(Activity _callingActivity, FirebaseUser _firebaseUser)
    {
        this.firebaseUser = _firebaseUser;

        this.CALLINGTAG = _callingActivity.getLocalClassName();

        this.userStorageReferance = FirebaseStorage.getInstance().getReference().child("Users");
        this.userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        this.getUserName();
        this.getUserProfilePicture();
        this.getEmail();
        this.getUserRating();
        this.getUserTermAcceptance();
    }

    private void getUserName()
    {
        userName = firebaseUser.getDisplayName();
    }

    private void getUserProfilePicture()
    {
        final long ONE_MEGABYTE = 1024 * 1024;
        userStorageReferance
                .child(firebaseUser.getUid())
                .child("profilePicture")
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes)
            {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                if(bitmap != null)
                {
                    profilePicture = bitmap;
                }
                else
                {
                    Log.d(CALLINGTAG,"Unable to decode byte array");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(CALLINGTAG,"Failed to download profile picture");
            }
        });
    }

    private void getEmail()
    {
        email = firebaseUser.getEmail();
    }

    private void getUserRating()
    {
        userDatabaseReference
                .child(firebaseUser.getUid())
                .child("rating").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        rating = (double)snapshot.getValue();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                        Log.e(CALLINGTAG,"Unable to get value from database");
                    }
                });
    }

    private void getUserTermAcceptance()
    {
        userDatabaseReference
                .child(firebaseUser.getUid())
                .child("TermsAcceptance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                 termsAcceptance = (boolean)snapshot.getValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Log.e(CALLINGTAG,"Unable to get value from database");
            }
        });
    }

}
