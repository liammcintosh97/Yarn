package com.example.liammc.yarn.accounting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.liammc.yarn.R;


public class TermsFragment extends Fragment {
    /*This fragment is used as apart of the IntroActivity. Its used to show the terms and conditions
    * of Yarn*/

    public TermsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.terms_fragment_page, container, false);

        return rootView;
    }

}