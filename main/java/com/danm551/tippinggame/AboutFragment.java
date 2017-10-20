package com.danm551.tippinggame;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {
    final private String FONT_RIGHTEOUS = "font_righteous_regular.ttf";

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setFonts();
    }

    private void setFonts(){
        TextView aboutTitleView = (TextView) getView().findViewById(R.id.text_about);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), FONT_RIGHTEOUS);
        aboutTitleView.setTypeface(typeface);
    }

}
