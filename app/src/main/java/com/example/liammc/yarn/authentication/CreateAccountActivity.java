package com.example.liammc.yarn.authentication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.IntroActivity;
import com.example.liammc.yarn.accounting.YarnUserUpdator;
import com.example.liammc.yarn.utility.CompatabiltyTools;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateAccountActivity extends AppCompatActivity {

    private static final int CAMERA_PIC_REQUEST = 1;
    private YarnUserUpdator userUpdator;

    private Bitmap profilePictureBitmap;
    private EditText userNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        userUpdator = new YarnUserUpdator(this,currentUser, auth);

        userNameInput = findViewById(R.id.userNameInput);

        CompatabiltyTools.setUserNameAutoFill(userNameInput);
    }

    //Android Callbacks
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK)
        {
            profilePictureBitmap = (Bitmap) data.getExtras().get("data");

            if(profilePictureBitmap != null)
            {
                ImageButton imageButton = findViewById(R.id.profilePictureButton); //sets imageview as the bitmap
                imageButton.setImageBitmap(profilePictureBitmap);
            }
        }
    }

    //region Button Methods

    public void onCreateAccountButtonPress(View view)
    {
        EditText userNameinput = findViewById(R.id.userNameInput);

        String userName = userNameinput.getText().toString();

        userUpdator.updateUserName(userName);
        userUpdator.updateUserProfilePicture(profilePictureBitmap);
        userUpdator.updateUserRating(5);

        Intent intent = new Intent(getBaseContext(), IntroActivity.class);
        startActivity(intent);
    }

    public void onProfilePictureButtonPress(View view)
    {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent,CAMERA_PIC_REQUEST);
    }

    //endregion
}
