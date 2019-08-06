package com.example.liammc.yarn.core;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.yarnSupport.EmailUpdator;
import com.example.liammc.yarn.yarnSupport.PasswordUpdater;
import com.example.liammc.yarn.yarnSupport.SupportWindow;

public class AccountActivity extends YarnActivity {
    /*This activity is where the firebaseUser interacts with their account and settings*/

    private final int CAMERA_PIC_REQUEST = 1;
    private final String TAG = "AccountActivity";

    //UI
    private PasswordUpdater passwordUpdater;
    private EmailUpdator emailUpdator;
    private SupportWindow supportWindow;
    private ImageButton profilePicture;
    private EditText profileNameInput;
    private TextView profileName;
    private TextView genderValue;
    private TextView ageValue;
    private ImageView[] stars = new ImageView[5];
    private Button editButton;
    private Button cancelButton;
    private Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*Runs when an Activity returns a result*/

        //Checks if the result is from a camera
        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK)
        {
            //Get the profile picture bitmap from the data
            Bitmap profilePictureBitmap = (Bitmap) data.getExtras().get("data");

            //Set the profile picture with the return bitmap
            if(profilePicture != null) {
                profilePicture.setImageDrawable(
                        new BitmapDrawable(Resources.getSystem(),profilePictureBitmap));
            }
        }
    }

    //region init

    private void initUI(){
        /*Initializes the Account Activity's UI*/

        profilePicture = findViewById(R.id.profilePicture);
        profileNameInput = findViewById(R.id.userNameInput);
        profileName = findViewById(R.id.userName);
        genderValue =  findViewById(R.id.genderValue);
        ageValue = findViewById(R.id.ageValue);

        editButton = findViewById(R.id.editButton);
        cancelButton = findViewById(R.id.cancelButton);
        updateButton = findViewById(R.id.updateButton);

        ViewGroup main =  findViewById(R.id.main);

        passwordUpdater =  new PasswordUpdater(this,main);
        emailUpdator =  new EmailUpdator(this,main);
        supportWindow =  new SupportWindow(this,main);

        initStars();
        setEditable(false);
    }

    private void initStars() {
        /*Initializes the stars*/

        //Get star references
        stars[0] = findViewById(R.id.star);
        stars[1] = findViewById(R.id.star1);
        stars[2] = findViewById(R.id.star2);
        stars[3] = findViewById(R.id.star3);
        stars[4] = findViewById(R.id.star4);

        //Set all the stars to invisible
        for(int i = 0; i < stars.length; i++){
            stars[i].setVisibility(View.INVISIBLE);
        }

        //Show the firebaseUser's meanRating by setting the visibility of the stars
        for(int i = 0; i < localUser.meanRating; i++){
            stars[i].setVisibility(View.VISIBLE);
        }
    }

    //endregion

    //region Button Methods

    public void onEditPressed(View view){
        /*When the firebaseUser presses the edit button set the UI to be editable*/
        setEditable(true);
    }

    public void onCancelPressed(View view){
        /*When the firebaseUser presses the cancel button set the UI to not be editable*/
        setEditable(false);
    }

    public void onUpdatePressed(View view){
        /*When the firebaseUser presses updateInfoWindow it gets the Input and sets the User's info*/

        profileName.setText(profileNameInput.getText().toString());

        localUser.updator.updateUserName(profileName.getText().toString());
        localUser.updator.updateUserProfilePicture(this,((BitmapDrawable)profilePicture
                .getDrawable()).getBitmap());

        setEditable(false);
    }

    public void onLogOutPressed(View view){
        /*When the User presses the Log Out button log out the firebaseUser and take them to the Main
        Activity*/

        localUser.firebaseAuth.signOut();

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void onProfilePicturedPressed(View view){
        /*When the firebaseUser presses the Profile Picture button start the Camera Activity to return the
        picture*/

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent,CAMERA_PIC_REQUEST);
    }

    public void onUpdatePasswordButton(View view){
        passwordUpdater.show(Gravity.CENTER);
    }

    public void onUpdateEmailButton(View view){
        emailUpdator.show(Gravity.CENTER);
    }

    public void onSupportButtonPress(View view){ supportWindow.show(Gravity.CENTER); }

    //endregion

    //region Private Methods

    private void setEditable(boolean edit){
        /*Sets the UI to be editable depending on what boolean is passed*/


        if(edit){
            editButton.setVisibility(View.GONE);
            cancelButton.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.VISIBLE);

            profileName.setVisibility(View.GONE);
            profileNameInput.setVisibility(View.VISIBLE);

            profilePicture.setClickable(true);
        }
        else{
            editButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);

            profileName.setVisibility(View.VISIBLE);
            profileNameInput.setVisibility(View.GONE);

            profilePicture.setClickable(false);

            //reset UI
            profileName.setText(localUser.userName);
            profileNameInput.setText(localUser.userName);
            profilePicture.setImageDrawable(
                    new BitmapDrawable(Resources.getSystem(),localUser.profilePicture));
            genderValue.setText(localUser.gender);
            ageValue.setText(String.valueOf(localUser.age));
        }
    }

    //endregion
}
