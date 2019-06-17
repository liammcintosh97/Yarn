package com.example.liammc.yarn.authentication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.IntroActivity;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateAccountActivity extends AppCompatActivity {
    /*This activity is used when the firebaseUser creates an Account*/

    private static final int CAMERA_PIC_REQUEST = 1;
    private LocalUser localUser;

    private Bitmap profilePictureBitmap;
    private EditText userNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //Gte the Firebase firebaseUser and auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        //Get the Local User
        localUser = LocalUser.getInstance();
        localUser.initUserAuth(auth);
        localUser.initDatabaseReferences(currentUser.getUid());

        //initialize UI
        userNameInput = findViewById(R.id.userNameInput);
        CompatibilityTools.setUserNameAutoFill(userNameInput);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*Runs when an activity runs and returns a result*/

        //Check if the returned result is from the camera activity
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK)
        {
            //Get the profile picture from the data
            profilePictureBitmap = (Bitmap) data.getExtras().get("data");

            //Set the image button to the profile picture
            if(profilePictureBitmap != null) {
                ImageButton imageButton = findViewById(R.id.profilePictureButton);
                imageButton.setImageBitmap(profilePictureBitmap);
            }
            else{
                Toast.makeText(this, "Couldn't get profile picture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //region Button Methods

    public void onCreateAccountButtonPress(View view) {
        /*Updates the User's Information*/

        //Get the firebaseUser name
        EditText userNameInput = findViewById(R.id.userNameInput);
        String userName = userNameInput.getText().toString();

        if(userName.equals("")){
            Toast.makeText(this, "Please Enter a name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(profilePictureBitmap == null){
            Toast.makeText(this, "Please Enter a Profile picture", Toast.LENGTH_SHORT).show();
            return;
        }


        //Set the User's Information
        localUser.updator.updateUserName(userName);
        localUser.updator.updateUserProfilePicture(this,profilePictureBitmap);
        localUser.updator.addUserRating(localUser.userID,5);

        //Take the firebaseUser to the IntroActivity
        Intent intent = new Intent(getBaseContext(), IntroActivity.class);
        startActivity(intent);
    }

    public void onProfilePictureButtonPress(View view) {
        /*Take the firebaseUser to the external camera activity to return a profile picture*/
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent,CAMERA_PIC_REQUEST);
    }

    //endregion
}
