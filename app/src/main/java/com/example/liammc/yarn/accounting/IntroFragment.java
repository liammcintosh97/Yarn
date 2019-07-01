package com.example.liammc.yarn.accounting;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.liammc.yarn.R;

public class IntroFragment extends Fragment {
    /*This fragment is used as apart of the IntroActivity. Its used to show the user how Yarn
    * functions as a service*/

    public IntroFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.intro_fragment_page, container, false);

        return rootView;
    }


}
