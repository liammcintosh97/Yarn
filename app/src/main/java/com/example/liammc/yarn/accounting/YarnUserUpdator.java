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
    public final String userID;

    private final StorageReference userStorageReferance;
    private final DatabaseReference userDatabaseReference;

    private final String CALLINGTAG;
    private final Activity callingActivity;

    public YarnUserUpdator(Activity _callingActivity, FirebaseUser _user, FirebaseAuth _auth)
    {
        //This is the contsructor for the local Yarn users

        this.callingActivity = _callingActivity;
        this.CALLINGTAG = _callingActivity.getLocalClassName();

        this.user= _user;
        this.auth =_auth;
        this.userID = _user.getUid();

        this.userStorageReferance = FirebaseStorage.getInstance().getReference().child("Users");
        this.userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public YarnUserUpdator(Activity _callingActivity, String userID)
    {
        //This is the constructor for networked Yarn users

        this.callingActivity = _callingActivity;
        this.CALLINGTAG = _callingActivity.getLocalClassName();

        this.user = null;
        this.auth = null;
        this.userID = userID;

        this.userStorageReferance = FirebaseStorage.getInstance().getReference().child("Users");
        this.userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void updateUserName(String userName)
    {
        if(user != null)
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

            userDatabaseReference.child(userID).child("userName").setValue(userName)
                    .addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            Log.d(CALLINGTAG,"user name write to database was a success");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Log.d(CALLINGTAG,"user name write to database was a failure -" + e);
                        }
                    });
        }

    }

    public void updateUserProfilePicture(Bitmap profilePictureBitmap)
    {
        if(user != null)
        {
            Uri uri = getImageUri(callingActivity,profilePictureBitmap);

            //Upload the profile picture to storage
            userStorageReferance.child(userID).child("profilePicture").putFile(uri)
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

    }

    public void updateUserEmail(String email)
    {
        if(user != null)
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

            userDatabaseReference.child(userID).child("email").setValue(email)
                    .addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            Log.d(CALLINGTAG,"email write to database was a success");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Log.d(CALLINGTAG,"email write to database was a failure -" + e);
                        }
                    });
        }
    }

    public void updateUserPassword(String newPassword)
    {
        if(user != null)
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
                        Log.d(CALLINGTAG,"terms write to database was a success");
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d(CALLINGTAG,"terms write to database was a failure -" + e);
                    }
                });
    }

    public void sendPasswordReset(String emailAddress)
    {
        if(auth != null)
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
        else
            {
                Log.d(CALLINGTAG,"Your don't have access to this user");
            }

    }

    public void deleteUser()
    {
        if(user != null)
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
        else
            {
                Log.d(CALLINGTAG,"Your don't have access to this user");
            }
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
