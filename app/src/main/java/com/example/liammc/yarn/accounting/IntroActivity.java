package com.example.liammc.yarn.accounting;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.example.liammc.yarn.core.MapsActivity;
import com.example.liammc.yarn.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IntroActivity extends FragmentActivity {

    private static final int NUM_PAGES = 4;

    private ViewPager mPager;
    PagerAdapter mPagerAdapter;

    private YarnUserUpdator userUpdator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        userUpdator = new YarnUserUpdator(this,currentUser, auth);
    }

    @Override
    public void onBackPressed()
    {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    public void OnSkipPressed(View view)
    {
        mPager.setCurrentItem(NUM_PAGES);
    }

    public void OnAcceptPressed(View view)
    {
        userUpdator.updateTermsAceptance(true);

        Intent myIntent = new Intent(getBaseContext(),   MapsActivity.class);
        startActivity(myIntent);
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
    {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position){

            if(position == NUM_PAGES - 1)
            {
                return new TermsFragment();
            }
            else {
                return new IntroFragment();
            }
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }
    }
}
