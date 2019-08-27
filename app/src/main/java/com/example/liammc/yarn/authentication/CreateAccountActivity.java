package com.example.liammc.yarn.authentication;

import android.content.Intent;
import android.graphics.Bitmap;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.IntroActivity;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.TermsActivity;
import com.example.liammc.yarn.core.InitializationActivity;
import com.example.liammc.yarn.core.YarnActivity;
import com.example.liammc.yarn.utility.CompatibilityTools;
import com.example.liammc.yarn.utility.DateTools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class CreateAccountActivity extends YarnActivity {
    /*This activity is used when the firebaseUser creates an Account*/

    private static final int CAMERA_PIC_REQUEST = 1;

    private Bitmap profilePictureBitmap;
    private EditText userNameInput;
    private Spinner genderSpinner;
    private DatePicker birthDateInput;

    private String[] genderOptions = new String[]{"Male", "Female", "Rather not say"};

    //TODO create date picker dialog
    //TODO implement an account complete check after signing in
    //TODO disclaimer about profile picture

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        initUI();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        super.onActivityResult(requestCode,resultCode,data);
    }

    //region Private Methods

    private void initUI(){

        //initialize UI
        userNameInput = findViewById(R.id.userNameInput);
        genderSpinner  = findViewById(R.id.genderSpinner);
        birthDateInput =  findViewById(R.id.birthDatePicker);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, genderOptions);
        genderSpinner.setAdapter(adapter);

        CompatibilityTools.setUserNameAutoFill(userNameInput);
    }

    //endregion

    //region Button Methods

    public void onCreateAccountButtonPress(View view) {
        /*Updates the User's Information*/

        String userName = userNameInput.getText().toString();

        int birthYear = birthDateInput.getYear();
        String birthDate = DateTools.parse(birthDateInput.getDayOfMonth(),birthDateInput.getMonth()
                                            ,birthYear);
        String gender =  genderSpinner.getSelectedItem().toString();

        if(userName.equals("")){
            Toast.makeText(this, "Please Enter a name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(profilePictureBitmap == null){
            Toast.makeText(this, "Please Enter a Profile picture", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!validateAge(birthYear)){
            Toast.makeText(this, "Sorry you must be 18+", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!validateGender(birthDate)){
            Toast.makeText(this, "Please enter a gender option", Toast.LENGTH_SHORT).show();
            return;
        }

        //Set the User's Information
        localUser.updator.updateUserName(userName);
        localUser.updator.updateUserProfilePicture(this,profilePictureBitmap);
        localUser.updator.updateBirthDate(birthDate);
        localUser.updator.updateGender(gender);
        localUser.updator.addUserRating(localUser.userID,5);

        //Take the firebaseUser to the TermsActivity
        goToInitializationActivity();
    }

    public void onProfilePictureButtonPress(View view) {
        /*Take the firebaseUser to the external camera activity to return a profile picture*/
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent,CAMERA_PIC_REQUEST);
    }

    //endregion

    //region Private Methods

    private void goToInitializationActivity(){
        Intent intent = new Intent(getBaseContext(), InitializationActivity.class);
        startActivity(intent);
    }

    private boolean validateAge(int birthYear){

       int currentYear = Calendar.getInstance().get(Calendar.YEAR);

       if(currentYear - birthYear < 18) return false;

        return true;
    }

    private boolean validateGender(String gender){

        if(gender == null || gender.equals("")) return false;

        return true;
    }

    //endregion
}
