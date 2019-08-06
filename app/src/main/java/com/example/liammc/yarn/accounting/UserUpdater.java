package com.example.liammc.yarn.accounting;

import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


public class UserUpdater {
    /*This class is used to updateInfoWindow the Local User*/

    private final String TAG = "UserUpdater";
    private YarnUser user;

    public UserUpdater(YarnUser _user){this.user = _user;}

    //region Public Methods

    public void addUserRating(String otherUserID, int rating){
        //Write to the User database firebaseUser name
        user.userDatabaseReference.child("ratings").child(otherUserID).setValue(rating)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"meanRating write to database was a success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"meanRating write to database was a failure -" + e);
                    }
                });
    }

    //endregion

}
