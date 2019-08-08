package com.example.liammc.yarn.accounting;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.authentication.CreateAccountActivity;
import com.example.liammc.yarn.core.InitializationActivity;
import com.example.liammc.yarn.core.YarnActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class TermsActivity extends YarnActivity {
    /*This fragment is used as apart of the IntroActivity. Its used to show the terms and conditions
    * of Yarn*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_fragment_page);
    }


    //region Button Methods

    public void onAcceptPressed(View view) {
        localUser.updator.updateTermsAcceptance(true);

        Intent myIntent = new Intent(getBaseContext(),   CreateAccountActivity.class);
        startActivity(myIntent);
    }

    //endregion

}