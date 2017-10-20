package com.danm551.tippinggame;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {
    final private String FONT_RIGHTEOUS = "font_righteous_regular.ttf";
    private Button resetHiScores, resetSettings;
    private CheckBox soundCheckBox, ambientSoundCheckBox;
    private Drawable drawableUnclicked, drawableClicked;
    private RadioGroup difficultyRadioGroup;
    private RadioButton difficultyRadioBtn1,  difficultyRadioBtn2,  difficultyRadioBtn3;
    private SettingsListener callback;
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    private TextView difficultyTitle;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Interface that allows background music and sound to be turned off/on in real-time
     * Implemented by: MainActivity
     */
    interface SettingsListener{
        void ambientSoundOff();
        void ambientSoundOn();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //Assures the interface listener is attached to the activity container
        try{
            callback = (SettingsListener) getActivity();
        }
        catch(ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getViews();
        setSettings();
        setListeners();
        setFonts();
    }

    private void getViews(){
        resetHiScores = (Button) getActivity().findViewById(R.id.button_reset_hi_scores);
        resetSettings = (Button) getActivity().findViewById(R.id.button_reset_settings);
        difficultyTitle = (TextView) getActivity().findViewById(R.id.text_difficulty_title);
        difficultyRadioGroup = (RadioGroup) getActivity().findViewById(R.id.radio_difficulty);
        difficultyRadioBtn1 = (RadioButton) getActivity().findViewById(R.id.radio_option_easy);
        difficultyRadioBtn2 = (RadioButton) getActivity().findViewById(R.id.radio_option_normal);
        difficultyRadioBtn3 = (RadioButton) getActivity().findViewById(R.id.radio_option_hard);
        drawableUnclicked = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_button_unclicked, null);
        drawableClicked = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_button_clicked, null);
    }

    private void setSettings(){
        soundCheckBox = (CheckBox) getView().findViewById(R.id.check_sound);
        ambientSoundCheckBox = (CheckBox) getView().findViewById(R.id.check_ambient_sound);

        settings = getActivity().getSharedPreferences("TIP_PREFS", MODE_PRIVATE);

        if(settings.getBoolean("ambientSound", false)) ambientSoundCheckBox.setChecked(true);


        if(settings.getBoolean("sounds", false)) soundCheckBox.setChecked(true);


        String difficulty = settings.getString("difficulty", "normal");
        switch(difficulty){
            case "easy":
                difficultyRadioBtn1.setBackground(drawableClicked);
                break;
            case "normal":
                difficultyRadioBtn2.setBackground(drawableClicked);
                break;
            case "hard":
                difficultyRadioBtn3.setBackground(drawableClicked);
                break;
            default:
                difficultyRadioBtn2.setBackground(drawableClicked);
                break;
        }
    }

    private void setListeners(){
        ambientSoundCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefEditor = settings.edit();
                if(settings.getBoolean("ambientSound", false)){
                    prefEditor.putBoolean("ambientSound", false);
                    callback.ambientSoundOff();
                }
                else{
                    prefEditor.putBoolean("ambientSound", true);
                    callback.ambientSoundOn();
                }

                prefEditor.apply();
            }
        });

        soundCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefEditor = settings.edit();
                if(settings.getBoolean("sounds", false)){
                    prefEditor.putBoolean("sounds", false);
                }
                else{
                    prefEditor.putBoolean("sounds", true);
                }

                prefEditor.apply();
            }
        });

        resetHiScores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setMessage("Are you sure you want to reset the hi scores?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    PrintWriter pw = new PrintWriter(getActivity().getFileStreamPath("hiScores"));
                                    pw.close();

                                    Toast.makeText(getContext(), getResources().getString(R.string.toastHiScoresErased), Toast.LENGTH_SHORT).show();
                                }
                                catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        resetSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setMessage("Are you sure you want to reset the settings?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefEditor = settings.edit();
                                prefEditor.clear();
                                prefEditor.putBoolean("ambientSound", true)
                                        .putBoolean("sounds", true)
                                        .putString("difficulty", "normal")
                                        .apply();

                                ambientSoundCheckBox.setChecked(true);
                                soundCheckBox.setChecked(true);
                                difficultyRadioBtn1.setBackground(drawableUnclicked);
                                difficultyRadioBtn2.setBackground(drawableClicked);
                                difficultyRadioBtn3.setBackground(drawableUnclicked);

                                callback.ambientSoundOn();

                                Toast.makeText(getContext(), getResources().getString(R.string.toastSettingsReset), Toast.LENGTH_SHORT).show();

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        difficultyRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                prefEditor = settings.edit();
                if(checkedId == difficultyRadioBtn1.getId()){
                    difficultyRadioBtn1.setBackground(drawableClicked);
                    difficultyRadioBtn2.setBackground(drawableUnclicked);
                    difficultyRadioBtn3.setBackground(drawableUnclicked);

                    prefEditor.putString("difficulty", "easy")
                            .apply();
                }
                else if(checkedId == difficultyRadioBtn2.getId()){
                    difficultyRadioBtn1.setBackground(drawableUnclicked);
                    difficultyRadioBtn2.setBackground(drawableClicked);
                    difficultyRadioBtn3.setBackground(drawableUnclicked);

                    prefEditor.putString("difficulty", "normal")
                            .apply();
                }
                else{
                    difficultyRadioBtn1.setBackground(drawableUnclicked);
                    difficultyRadioBtn2.setBackground(drawableUnclicked);
                    difficultyRadioBtn3.setBackground(drawableClicked);

                    prefEditor.putString("difficulty", "hard")
                            .apply();
                }

            }
        });
    }

    private void setFonts(){
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), FONT_RIGHTEOUS);

        soundCheckBox.setTypeface(typeface);
        ambientSoundCheckBox.setTypeface(typeface);
        difficultyTitle.setTypeface(typeface);
        difficultyRadioBtn1.setTypeface(typeface);
        difficultyRadioBtn2.setTypeface(typeface);
        difficultyRadioBtn3.setTypeface(typeface);
        resetHiScores.setTypeface(typeface);
    }
}
