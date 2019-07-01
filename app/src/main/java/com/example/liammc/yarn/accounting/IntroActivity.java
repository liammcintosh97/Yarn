package com.example.liammc.yarn.accounting;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.example.liammc.yarn.core.InitializationActivity;
import com.example.liammc.yarn.R;
import com.google.firebase.auth.FirebaseAuth;

public class IntroActivity extends FragmentActivity {
    /*This Activity is the intro into Yarn for the user after they first sign up*/

    private static final int NUM_PAGES = 4;

    private ViewPager mPager;
    PagerAdapter mPagerAdapter;

    private LocalUser localUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        initPager();
        initLocalUser();
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the firebaseUser is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    //region Init

    private void initPager(){
        /*Initializes the pager and adapter*/

        mPager = findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    private void initLocalUser(){
        /*Initializes the Local User*/

        localUser = LocalUser.getInstance();
        localUser.initUserAuth(FirebaseAuth.getInstance());
    }

    //endregion

    //region Buttons

    public void onSkipPressed(View view) {
        mPager.setCurrentItem(NUM_PAGES);
    }

    public void onAcceptPressed(View view) {
        localUser.updator.updateTermsAcceptance(true);

        Intent myIntent = new Intent(getBaseContext(),   InitializationActivity.class);
        startActivity(myIntent);
    }

    //endregion

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        /*This internal class is used to slide the pages in the activity*/

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position){
            /*Determines the pages of slider by returning particular fragments*/

            if(position == NUM_PAGES - 1) {
                //The user is on the last page so show the TermsFragment
                return new TermsFragment();
            }
            else {
                //The user is on any other page so show the IntroFragment
                return new IntroFragment();
            }
        }

        @Override
        public int getCount() {
            //Get the number of pages
            return NUM_PAGES;
        }
    }
}
