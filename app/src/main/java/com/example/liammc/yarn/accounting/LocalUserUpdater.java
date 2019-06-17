package com.example.liammc.yarn.accounting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class LocalUserUpdater extends UserUpdater {

    private final String TAG = "LocalUserUpdater";

    private final LocalUser localUser;

    public LocalUserUpdater(LocalUser _localUser){
        super( _localUser);
        this.localUser = _localUser;
    }

    //region Public Methods

    public void updateUserName(String userName) {
        /*This method updates the firebaseUser's name in Firebase Authentication and in the database*/

        //Creates a profile update request
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build();

        //Updates the profile in Firebase Authentication
        localUser.firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Runs when the update completes

                        if (task.isSuccessful()) {
                            //Log when the update is successful
                            Log.d(TAG, "User name updated.");
                        }
                        else{
                            //Log when the update is a failure
                            Log.e(TAG, "User name update failed - " + task.getException());
                        }
                    }
                });

        //Updates the firebaseUser name in the Firebase database
        localUser.userDatabaseReference.child("userName").setValue(userName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"firebaseUser name write to database was a success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"firebaseUser name write to database was a failure -" + e);
                    }
                });
    }

    public void updateUserProfilePicture(Activity activity, Bitmap profilePictureBitmap) {
        /*This method updates the firebaseUser's profile picture with the passed bitmap. To complete this
         * complex process first the bitmap must be compressed and the URI must be extracted. Then
         * The URI must be uploaded to Firebase storage. Once that is completed the firebaseUser profile is
         * updated with the URL that's linked to the stored image*/

        Uri uri = getImageUri(activity,profilePictureBitmap);

        //Upload the profile picture to storage
        localUser.userStorageReference.child("profilePicture").putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Runs when uploading the picture to storage is a success*/
                        Log.d(TAG,"uploaded profile picture to storage successfully");

                        //Get the the download URL from the storage reference
                        StorageReference ref = taskSnapshot.getMetadata().getReference();

                        if(ref != null) {
                            //Get the reference's download URL
                            ref.getDownloadUrl()
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            //Runs when getting the download URL is complete
                                            Log.d(TAG,"Got downloadUrl from storage reference successfully");

                                            if(task.isSuccessful()){
                                                //The task is successful

                                                //Build a profile change request
                                                UserProfileChangeRequest profileUpdates =
                                                        new UserProfileChangeRequest.Builder()
                                                                .setPhotoUri(task.getResult())
                                                                .build();

                                                //Update the profile picture with the URL
                                                localUser.firebaseUser.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "User " +
                                                                            "profile updated.");
                                                                }
                                                            }
                                                        });
                                            }
                                            else{
                                                //The task wasn't successful so log the error
                                                Log.e(TAG,"Error getting download URL from" +
                                                        "stored image - " + task.getException());
                                            }
                                        }
                                    });

                        }
                        else{
                            //Couldn't get the storage reference
                            Log.e(TAG,"Unable to get storage reference when updating " +
                                    "profile picture");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Uploading the image failed so log the error
                        Log.d(TAG,"failed to upload profile picture to storage -" + e);
                    }
                });


    }

    public void updateUserEmail(String email) {
        /*Update the email in Firebase Authentication and in the Firebase database reference*/

        //Update the email in Firebase Authentication
        localUser.firebaseUser.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            //Email update was successful
                            Log.d(TAG, "User email address updated.");

                            //Send email verification to new email
                            localUser.firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //Verification email sent
                                                Log.d(TAG, "Verification email sent.");
                                            }
                                            else {
                                                Log.e(TAG,"Error when sending verification " +
                                                        "email - " + task.getException());
                                            }
                                        }
                                    });
                        }
                        else{
                            Log.e(TAG,"Error when updating email - " + task.getException());
                        }
                    }
                });

        //Update the email in Firebase Database
        localUser.userDatabaseReference.child(localUser.userID).child("email").setValue(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"email write to database was a success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"email write to database was a failure -" + e);
                    }
                });

    }

    public void updateUserPassword(String newPassword) {
        //Updates the firebaseUser password

        localUser.firebaseUser.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User password updated.");
                        }
                        else Log.e(TAG,"User password update failed - " + task.getException());
                    }
                });

    }

    public void updateTermsAcceptance(boolean acceptance) {
        /*Updates the firebaseUser's term acceptance*/

        //Write to the User database firebaseUser name
        localUser.userDatabaseReference.child("TermsAcceptance").setValue(acceptance)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"terms write to database was a success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"terms write to database was a failure -" + e);
                    }
                });
    }

    public void sendPasswordReset(String emailAddress) {
        //Sends a password reset email

        localUser.firebaseAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password reset email sent.");
                        }
                        else Log.e(TAG,"Password reset email failed to send - "
                                + task.getException());
                    }
                });
    }

    public void deleteUser() {
        //Deletes the from Firebase Authentication, database and storage

        //Remove from Firebase Authentication
        localUser.firebaseUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account deleted.");
                        }
                    }
                });

        //Remove from database and storage
        localUser.userDatabaseReference.child(localUser.firebaseUser.getUid()).removeValue();
        localUser.userStorageReference.child(localUser.firebaseUser.getUid()).delete();

    }

    //endregion

    //region Private Methods

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        //Get the URI from a bitmap image

        //Open the bytes stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        //Compress the image
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        //Get the path from the image located on the device
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,
                "Title", null);
        return Uri.parse(path);
    }

    //endregion
}
