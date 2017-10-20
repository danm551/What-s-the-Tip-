package com.danm551.tippinggame;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class QuestionResultsActivity extends AppCompatActivity {
    final private String PREF_GENERAL = "TIPS_PREFS";
    final private String FONT_RIGHTEOUS = "font_righteous_regular.ttf";
    private final Locale locale = Locale.getDefault();
    private final String SAVE_FILE = "hiScores";
    private boolean over, exit = false, newHiScore = false;
    private double finalDifference, totalRounded, realAnswer, userAnswer, difference, userPercent,
            overTotal, underTotal, finalResult, percent;
    private int counter, questionTotal = 5, spinRate = 10;
    private long totalTime = 30000, interval = 1;
    private BackgroundNumberSpinner spinner;
    private Button continueBtn, hiScoresBtn;
    private Context context = this;
    private Intent GameActivityIntent, backToMenu;
    private MediaPlayer audioPlayer;
    private SharedPreferences settings;
    private SpannableString resultsString;
    private SpannableStringBuilder builder;
    private String accuracy, quality, date, orderedScoresString = "";
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_results);

        initObjects();
        getPreferences();
        getExtras();
        setFonts();
        buildResultOutput();
        postResults();
        playSound();
    }

    private void initObjects(){
        resultView = (TextView) findViewById(R.id.text_result);
        hiScoresBtn = (Button) findViewById(R.id.button_hi_scores);
    }

    private void getPreferences(){
        settings = getSharedPreferences(PREF_GENERAL, MODE_PRIVATE);
    }

    public void getExtras(){
        GameActivityIntent = getIntent();
        accuracy = GameActivityIntent.getStringExtra("accuracy");
        finalDifference = GameActivityIntent.getDoubleExtra("finalDifference", 0);
        counter = GameActivityIntent.getIntExtra("counter", 0);
        totalRounded = GameActivityIntent.getDoubleExtra("totalRounded", 0);
        quality = GameActivityIntent.getStringExtra("quality");
        realAnswer = GameActivityIntent.getDoubleExtra("realAnswer", 0);
        userAnswer = GameActivityIntent.getDoubleExtra("userAnswer", 0);
        difference = GameActivityIntent.getDoubleExtra("difference", 0);
        userPercent = GameActivityIntent.getDoubleExtra("userPercent", 0);
        underTotal = GameActivityIntent.getDoubleExtra("underTotal", 0);
        overTotal = GameActivityIntent.getDoubleExtra("overTotal", 0);
        percent = GameActivityIntent.getDoubleExtra("percent", 0);
    }

    public void setFonts(){
        Typeface typeface = Typeface.createFromAsset(getAssets(), FONT_RIGHTEOUS);
        resultView.setTypeface(typeface);
    }

    public void buildResultOutput(){
        if((overTotal-underTotal) > 0){
            over = true;
            finalResult = overTotal-underTotal;
        }
        else{
            over = false;
            finalResult = underTotal-overTotal;
        }

        builder = new SpannableStringBuilder();

        //"Your tip was "
        resultsString = new SpannableString(getResources().getString(R.string.results_string_1));
        builder.append(resultsString);

        switch(accuracy){
            case "low":
                resultsString = new SpannableString(getResources().getString(R.string.results_low));
                resultsString.setSpan(new ForegroundColorSpan(Color.RED), 0, resultsString.length(), 0);
                builder.append(resultsString);
                break;
            case "equal":
                resultsString = new SpannableString(getResources().getString(R.string.results_equal));
                resultsString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, resultsString.length(), 0);
                builder.append(resultsString);
                break;
            case "high":
                resultsString = new SpannableString(getResources().getString(R.string.results_high));
                resultsString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, resultsString.length(), 0);
                builder.append(resultsString);
                break;
        }

        //"Meal : "
        resultsString = new SpannableString(getResources().getString(R.string.results_string_2));
        builder.append(resultsString);

        //totalRounded
        resultsString = new SpannableString(String.format(locale, "%.2f", totalRounded));
        builder.append(resultsString);

        //"\n"
        resultsString = new SpannableString(getResources().getString(R.string.newline));
        builder.append(resultsString);

        //"[quality] service: [lacking][good][excellent]"
        String qualityUppercase = quality.substring(0,1).toUpperCase() + quality.substring(1);
        resultsString = new SpannableString(qualityUppercase + getResources().getString(R.string.results_string_3));
        builder.append(resultsString);

        switch(quality){
            case "lacking":
                resultsString = new SpannableString(String.format(locale, "%.0f", percent*100));
                builder.append(resultsString);
                break;
            case "good":
                resultsString = new SpannableString(String.format(locale, "%.0f", percent*100));
                builder.append(resultsString);
                break;
            case "excellent":
                resultsString = new SpannableString(String.format(locale, "%.0f", percent*100));
                builder.append(resultsString);
                break;
        }

        //%\n
        resultsString = new SpannableString(getResources().getString(R.string.results_string_4));
        builder.append(resultsString);

        //"Tip = [realAnswer]"
        resultsString = new SpannableString(getResources().getString(R.string.results_string_5));
        builder.append(resultsString);

        //realAnswer
        resultsString = new SpannableString(String.format(locale, "%.2f", realAnswer));
        builder.append(resultsString);

        //"Your tip = [userAnswer]"
        resultsString = new SpannableString(getResources().getString(R.string.results_string_6));
        builder.append(resultsString);

        //userAnswer
        resultsString = new SpannableString(String.format(locale, "%.2f", userAnswer));
        builder.append(resultsString);

        //(userPercent)\n\n
        resultsString = new SpannableString(getResources().getString(R.string.results_string_7, userPercent));
        builder.append(resultsString);


        //"Difference = [difference]"
        resultsString = new SpannableString(getResources().getString(R.string.results_string_8));
        builder.append(resultsString);

        if(realAnswer > userAnswer){
            //"-"
            resultsString = new SpannableString(getResources().getString(R.string.minus_sign));
            builder.append(resultsString);

            //$[difference]
            resultsString = new SpannableString(getResources().getString(R.string.results_string_9, difference));
            builder.append(resultsString);
        }
        else{
            //$[difference]
            resultsString = new SpannableString(getResources().getString(R.string.results_string_9, difference));
            builder.append(resultsString);
        }
    }

    /**
     * Reads in existing scores and splits into array
     * Calls for sorting, formatting, and writing to file
     */
    private void setHiScores() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        date = dateFormat.format(calendar.getTime());

        try {
            File file = context.getFileStreamPath(SAVE_FILE);
            StringBuilder tempScores = new StringBuilder();
            String scoresString;
            String[] scoresArray;

            if(file.exists()) {
                FileInputStream fis = openFileInput(SAVE_FILE);
                int content;
                while ((content = fis.read()) != -1) {
                    tempScores.append((char) content);
                }
            }

            scoresString = tempScores.toString();
            scoresString = scoresString + String.format(locale, "#%.2f", finalResult); //add current final score into the mix
            scoresString = scoresString.substring(1); //remove leading whitespace

            scoresArray = scoresString.split("\\#");

            sortScores(scoresArray);
            formatScores(scoresArray);
            writeScores();
        }
        catch(FileNotFoundException e1){
            Toast.makeText(context, "File Not Found", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e2){
            Toast.makeText(context, "IO Exception", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Removes extras from each score string for Double conversion
     * Sorts scores
     * @param scoresArray The array that contains the scores
     */
    private void sortScores(String[] scoresArray){
        for(int i = 0; i < scoresArray.length; i++){
            for(int j = 0; j < scoresArray.length; j++){
                int startIndex1, endIndex1, startIndex2, endIndex2;
                String tempScore1, tempScore2;
                if(scoresArray[i].contains("-")) {
                    startIndex1 = (scoresArray[i].indexOf("-") + 2);
                    endIndex1 = scoresArray[i].indexOf(" ");
                    tempScore1 = scoresArray[i].substring(startIndex1, endIndex1);
                }
                else if(scoresArray[i].contains(" ")){
                    startIndex1 = 1;
                    endIndex1 = scoresArray[i].indexOf(" ");
                    tempScore1 = scoresArray[i].substring(startIndex1, endIndex1);
                }
                else{
                    tempScore1 = scoresArray[i];
                }

                if(scoresArray[j].contains("-")) {
                    startIndex2 = (scoresArray[j].indexOf("-") + 2);
                    endIndex2 = scoresArray[j].indexOf(" ");
                    tempScore2 = scoresArray[j].substring(startIndex2, endIndex2);
                }
                else if(scoresArray[j].contains(" ")){
                    endIndex2 = scoresArray[j].indexOf(" ");
                    tempScore2 = scoresArray[j].substring(1, endIndex2);
                }
                else{
                    tempScore2 = scoresArray[j];
                }

                if(Double.parseDouble(tempScore1) > Double.parseDouble(tempScore2) && i < j ){
                    String temp = scoresArray[i];
                    scoresArray[i] = scoresArray[j];
                    scoresArray[j] = temp;
                }
            }
        }
    }

    /**
     * Stamps scores with date
     * @param scoresArray Contains the sorted scores
     */
    private void formatScores(String[] scoresArray){
        for(int i = 0; i < scoresArray.length; i++){
            if(i < 5){
                //if time stamp is not present, add it
                if(!scoresArray[i].contains("(")) {
                    if(over || (overTotal == underTotal)) {
                        orderedScoresString = orderedScoresString + "#$" + scoresArray[i] + "   (" + date + ")";
                        newHiScore = true;
                    }
                    else{
                        orderedScoresString = orderedScoresString + "#-$" + scoresArray[i] + "   (" + date + ")";
                        newHiScore = true;
                    }
                }
                else{
                    orderedScoresString = orderedScoresString + "#" + scoresArray[i];
                }
            }
        }
    }

    private void writeScores(){
        try {
            FileOutputStream fos = openFileOutput(SAVE_FILE, Context.MODE_PRIVATE);
            fos.write(orderedScoresString.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds additional result elements to end of game result screen
     */
    public void buildFinalResultOutput(){
        builder.clear();

        //"\n\nFinal Results"
        resultsString = new SpannableString(getResources().getString(R.string.results_string_10));
        resultsString.setSpan(new UnderlineSpan(), 0, resultsString.length(), 0);
        builder.append(resultsString);

        //"Overpaid : $%.2f\n"
        resultsString = new SpannableString(getResources().getString(R.string.results_string_11, overTotal));
        builder.append(resultsString);

        //"Underpaid : $%.2f\n"
        resultsString = new SpannableString(getResources().getString(R.string.results_string_12, underTotal));
        builder.append(resultsString);
    }

    public void postResults(){
        resultView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public void playSound(){
        if(settings.getBoolean("sounds", false)) {
            audioPlayer = MediaPlayer.create(context, R.raw.sound_register);
            audioPlayer.start();
        }
    }

    public void nextQuestion(View v){
        if(counter != questionTotal){
            Intent nextQuestionIntent = new Intent(context, GameActivity.class);
            nextQuestionIntent.putExtra("finalDifference", finalDifference)
                    .putExtra("counter", counter)
                    .putExtra("underTotal", underTotal)
                    .putExtra("overTotal", overTotal);
            startActivity(nextQuestionIntent);
            finish();
        }
        else if((counter == questionTotal) && !exit) {
            exit = true;

            setHiScores();

            hiScoresBtn.setVisibility(View.VISIBLE);

            continueBtn = (Button) findViewById(R.id.button_continue);
            continueBtn.setText(getResources().getString(R.string.exitBtn));

            buildFinalResultOutput();

            if (finalDifference < 500){
                spinRate = 10;
            }
            else if(finalDifference > 500 && finalDifference < 1000){
                spinRate = 15;
            }
            else{
                spinRate = 25;
            }

            spinner = new BackgroundNumberSpinner();
            spinner.startTimer();
        }
        else{
            backToMenu = new Intent(context, MainActivity.class);
            startActivity(backToMenu);
            finish();
        }
    }

    public void showHiScores(View v){
        HiScoresFragment hiScoresFragment = new HiScoresFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.defaultResultsFragment, hiScoresFragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        if(fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }
        else {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.endGame))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            backToMenu = new Intent(context, MainActivity.class);
                            startActivity(backToMenu);
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    }

    /**
     * A class that animates the final score after each round
     * Implements a CountDownTimer object as a timing facilitator
     */
    private class BackgroundNumberSpinner{
        int mCounter = 0;
        SpannableString spinnerString;
        SpannableStringBuilder spinnerBuilder = new SpannableStringBuilder();
        CountDownTimer spin = new CountDownTimer(totalTime, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                spinnerBuilder.clear();

                if(mCounter <= finalResult){
                    if(!over){
                        //"-"
                        spinnerString = new SpannableString(getResources().getString(R.string.minus_sign));
                        spinnerBuilder.append(spinnerString);

                    }

                    spinnerString = new SpannableString(String.format(locale, "%d", mCounter += spinRate));
                    spinnerBuilder.append(spinnerString);

                    //"Final score : "
                    spinnerString = new SpannableString(getResources().getString(R.string.results_string_15));
                    spinnerString.setSpan(new ForegroundColorSpan(Color.RED), 0, spinnerString.length(), 0);

                    resultView.setText(spinnerBuilder, TextView.BufferType.SPANNABLE);
                }
                else{
                    onFinish();
                    spin.cancel();
                }
            }

            @Override
            public void onFinish() {
                spinnerBuilder.clear();

                //"Final score : "
                spinnerString = new SpannableString(getResources().getString(R.string.results_string_15));
                spinnerString.setSpan(new ForegroundColorSpan(Color.RED), 0, spinnerString.length(), 0);
                builder.append(spinnerString);

                if(over || (overTotal == underTotal)){
                    spinnerString = new SpannableString(String.format(getResources().getString(R.string.results_string_16), finalResult));
                }
                else{
                    spinnerString = new SpannableString(String.format(getResources().getString(R.string.results_string_17), finalResult));
                }

                builder.append(spinnerString);

                if(newHiScore){
                    spinnerString = new SpannableString(getResources().getString(R.string.newHiScore));
                    spinnerString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, spinnerString.length(), 0);
                    builder.append(spinnerString);
                }

                resultView.setText(builder, TextView.BufferType.SPANNABLE);
            }
        };

        void startTimer(){spin.start();}
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(audioPlayer != null) audioPlayer.release();
    }
}
