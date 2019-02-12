package com.example.liammc.yarn.core;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.accounting.LocalUser;
import com.example.liammc.yarn.accounting.YarnUser;
import com.example.liammc.yarn.accounting.YarnUserUpdator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {

    private final int CAMERA_PIC_REQUEST = 1;
    private final String TAG = "AccountActivity";

    YarnUserUpdator userUpdator;
    YarnUser localUser;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;

    //UI
    private ImageButton profilePicture;
    private EditText profileNameInput;
    private TextView profileName;
    private ImageView[] stars = new ImageView[5];
    private Button editButton;
    private Button cancelButton;
    private Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        localUser = LocalUser.getInstance().user;
        if(firebaseUser != null){
            userUpdator = new YarnUserUpdator(this,firebaseUser,auth);
        }
        else{
            Log.e(TAG,"Fatal error the user is null");
        }

        setUpUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK)
        {
            Bitmap profilePictureBitmap = (Bitmap) data.getExtras().get("data");

            if(profilePicture != null)
            {
                profilePicture.setImageDrawable(
                        new BitmapDrawable(Resources.getSystem(),profilePictureBitmap));
            }
        }
    }

   @Override
   public void finish(){
        super.finish();
       overridePendingTransition(R.anim.right_to_left,R.anim.left_to_right);
   }

    //region setUp

    private void setUpUI(){

        profilePicture = findViewById(R.id.profilePicture);
        profileNameInput = findViewById(R.id.userNameInput);
        profileName = findViewById(R.id.userName);

        editButton = findViewById(R.id.editButton);
        cancelButton = findViewById(R.id.cancelButton);
        updateButton = findViewById(R.id.updateButton);

        initializeStars();
        setEditable(false);
    }

    //endregion

    //region Button Methods

    public void onEditPressed(View view){
        setEditable(true);
    }

    public void onCancelPressed(View view){
        setEditable(false);
    }

    public void onUpdatePressed(View view){

        profileName.setText(profileNameInput.getText().toString());

        userUpdator.updateUserName(profileName.getText().toString());
        userUpdator.updateUserProfilePicture(((BitmapDrawable)profilePicture
                .getDrawable()).getBitmap());

        setEditable(false);
    }

    public void onLogOutPressed(View view){
        auth.signOut();

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void onProfilePicturedPressed(View view){

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent,CAMERA_PIC_REQUEST);
    }

    //endregion

    //region Utility

    private void initializeStars()
    {
        //get references
        stars[0] = findViewById(R.id.star);
        stars[1] = findViewById(R.id.star1);
        stars[2] = findViewById(R.id.star2);
        stars[3] = findViewById(R.id.star3);
        stars[4] = findViewById(R.id.star4);

        //reset starts
        for(int i = 0; i < stars.length; i++){
            stars[i].setVisibility(View.INVISIBLE);
        }

        //show rating
        for(int i = 0; i < localUser.rating; i++){
            stars[i].setVisibility(View.VISIBLE);
        }
    }

    private void setEditable(boolean edit){

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
        }
    }

    //endregion
}
