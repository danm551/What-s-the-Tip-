package com.danm551.tippinggame;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;

public class HiScoresFragment extends Fragment {
    final private String FONT_RIGHTEOUS = "font_righteous_regular.ttf";
    final private String SAVE_FILE = "hiScores";

    public HiScoresFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hi_scores, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setListener();
        displayHiScores();
    }

    private void setListener(){
        Button hiScoresBackBtn = (Button) getActivity().findViewById(R.id.button_back);
        hiScoresBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.popBackStackImmediate();
            }
        });
    }

    private void displayHiScores(){
        try {
            String[] scoresArray;
            String scoresString;
            ListView hiScoresListView = (ListView) getView().findViewById(R.id.list_scores);
            TextView hiScoresTitleTextView = (TextView) getActivity().findViewById(R.id.text_title);

            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), FONT_RIGHTEOUS);
            hiScoresTitleTextView.setTypeface(typeface);

            FileInputStream fis = getActivity().openFileInput(SAVE_FILE);

            StringBuilder text = new StringBuilder();
            int content;
            while((content = fis.read()) != -1){
                text.append((char) content);
            }

            if(text.length() > 0) {
                scoresString = text.toString();
                scoresString = scoresString.substring(1);

                scoresArray = scoresString.split("\\#");
            }
            else{
                scoresArray = new String[1];
                scoresArray[0] = getActivity().getResources().getString(R.string.emptyList);
            }

            hiScoresListView.setAdapter(new CustomArrayAdapter(getContext(), R.layout.custom_question_results_listview, scoresArray, 1));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
