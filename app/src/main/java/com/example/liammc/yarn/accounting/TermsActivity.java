package com.example.liammc.yarn.accounting;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import com.example.liammc.yarn.R;
import com.example.liammc.yarn.authentication.CreateAccountActivity;
import com.example.liammc.yarn.core.YarnActivity;


public class TermsActivity extends YarnActivity {
    /*This fragment is used as apart of the IntroActivity. Its used to show the terms and conditions
    * of Yarn*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_terms);
    }


    //region Button Methods

    public void onAcceptPressed(View view) {
        localUser.updator.updateTermsAcceptance(true);

        Intent myIntent = new Intent(getBaseContext(),   CreateAccountActivity.class);
        startActivity(myIntent);
    }

    //endregion

}