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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class YarnUserUpdator
{
    public final FirebaseUser user;
    public final FirebaseAuth auth;

    private final StorageReference userStorageReferance;
    private final DatabaseReference userDatabaseReference;

    private final String CALLINGTAG;
    private final Activity callingActivity;

    public YarnUserUpdator(Activity _callingActivity, FirebaseUser _user, FirebaseAuth _auth)
    {
        this.callingActivity = _callingActivity;
        this.CALLINGTAG = _callingActivity.getLocalClassName();

        this.user= _user;
        this.auth =_auth;

        this.userStorageReferance = FirebaseStorage.getInstance().getReference().child("Users");
        this.userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void updateUserName(String userName)
    {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(CALLINGTAG, "User profile updated.");
                        }
                    }
                });
    }

    public void updateUserProfilePicture(Bitmap profilePictureBitmap)
    {
        Uri uri = getImageUri(callingActivity,profilePictureBitmap);

        //Upload the profile picture to storage
        userStorageReferance.child(user.getUid()).child("profilePicture").putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Log.d(CALLINGTAG,"uploaded profile picture to storage successfully");

                        //Get the the download URL from the storage reference
                        StorageReference ref = taskSnapshot.getMetadata().getReference();

                        if(ref != null)
                        {
                            ref.getDownloadUrl()
                                    .addOnCompleteListener(new OnCompleteListener<Uri>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task)
                                        {
                                            Log.d(CALLINGTAG,"Got downloadUrl from storage reference successfully");

                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setPhotoUri(task.getResult())
                                                    .build();

                                            user.updateProfile(profileUpdates)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                    {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d(CALLINGTAG, "User profile updated.");
                                                            }
                                                        }
                                                    });

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener()
                                    {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            Log.d(CALLINGTAG,"unable to get downloadUrl from storage reference - " + e);
                                        }
                                    });
                        }
                        else Log.e(CALLINGTAG,"Unable to get storage reference when updating profile picture");
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d(CALLINGTAG,"failed to upload profile picture to storage -" + e);
                    }
                });
    }

    public void updateUserEmail(String email)
    {
        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(CALLINGTAG, "User email address updated.");

                            //Send email verification to new email
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(CALLINGTAG, "Verification email sent.");
                                            }
                                        }
                                    });
                        }
                    }
                });




    }

    public void updateUserPassword(String newPassword)
    {
        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(CALLINGTAG, "User password updated.");
                        }
                    }
                });
    }

    public void updateUserRating(double rating)
    {
        //Write to the User database user name
        userDatabaseReference.child(user.getUid()).child("rating").setValue(rating)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Log.d(CALLINGTAG,"rating write to database was a success");
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d(CALLINGTAG,"rating write to database was a failure -" + e);
                    }
                });
    }

    public void updateTermsAceptance(boolean acceptance)
    {
        //Write to the User database user name
        userDatabaseReference.child(user.getUid()).child("TermsAcceptance").setValue(acceptance)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Log.d(CALLINGTAG,"rating write to database was a success");
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d(CALLINGTAG,"rating write to database was a failure -" + e);
                    }
                });
    }

    public void sendPasswordReset(String emailAddress)
    {
        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(CALLINGTAG, "Email sent.");
                        }
                    }
                });
    }

    public void deleteUser()
    {
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(CALLINGTAG, "User account deleted.");
                        }
                    }
                });

        userDatabaseReference.child(user.getUid()).removeValue();
        userStorageReferance.child(user.getUid()).delete();
    }

    //Utility
    private Uri getImageUri(Context inContext, Bitmap inImage)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

}
