package com.danm551.tippinggame;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    final private Context context = this;
    final private Locale locale = Locale.getDefault();
    private boolean paused = false, getSecondConcluded = false;
    private double percent, realAnswer, totalRounded, difference, finalDifferenceRounded, finalDifferenceRaw = 0, userPercent, overTotal = 0,
            underTotal = 0, userAnswer = 0, realAnswerConverted, userAnswerConverted;
    private int questionCounter, seconds, percentTemp;
    private long totalTime, interval = 1000;
    private BackgroundCountDownTimer timer;
    private BGMPlayer bgmPlayer;
    private EditText textBoxView;
    private Intent intent;
    private Random percentRand, totalRand;
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    private String songSelection, quality;
    private TextView questionView, receiptView, timerView;
    private TimerStateReceiver receiver;
    private Typeface font;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initObjects();
        getPrefs();
        setPrefs();
        setFonts();
        startMusic();
        showInstructions();
    }

    /**
     * Regulates timer activity on resume
     * Registers the broadcast receiver (TimerStateReceiver)
    */
    @Override
    public void onResume(){
        super.onResume();

        if(getSecondConcluded) getSecondConcluded = false;

        if(bgmPlayer != null) bgmPlayer.resume();

        if(paused){
            paused = false;
            timer = new BackgroundCountDownTimer();
            timer.startTimer();
        }

        IntentFilter broadcastFilter = new IntentFilter(BackgroundTimer.SYNC_KEYWORD);
        receiver = new TimerStateReceiver();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        broadcastManager.registerReceiver(receiver, broadcastFilter);
    }

    @Override
    public void onPause(){
        super.onPause();

        paused = true;
        if(bgmPlayer != null) bgmPlayer.pause();
        timer.stopTimer();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(bgmPlayer != null) bgmPlayer.release();

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        broadcastManager.unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.endGame))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent backToMenu = new Intent(context, MainActivity.class);
                        startActivity(backToMenu);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void initObjects(){
        questionView = (TextView) findViewById(R.id.text_question);
        receiptView = (TextView) findViewById(R.id.text_receipt);
        textBoxView = (EditText) findViewById(R.id.edit_input);
        timerView = (TextView) findViewById(R.id.text_timer);
    }

    private void getPrefs(){
        settings = getSharedPreferences("TIP_PREFS", MODE_PRIVATE);
    }

    private void setPrefs(){
        String difficulty = settings.getString("difficulty", "normal");
        switch(difficulty){
            case "easy":
                seconds = 30;
                totalTime = 30 * 1000;
                break;
            case "hard":
                seconds = 10;
                totalTime = 10 * 1000;
                break;
            default:
                seconds = 20;
                totalTime = 20 * 1000;
                break;
        }
    }

    public void setFonts(){
        font = Typeface.createFromAsset(getAssets(), "font_righteous_regular.ttf");

        questionView.setTypeface(font);
        timerView.setTypeface(font);
        receiptView.setTypeface(font);
    }

    private void startMusic(){
        if(settings.getBoolean("ambientSound", false)){
            songSelection = "music_restaurant";
            bgmPlayer = new BGMPlayer(context, songSelection);
            bgmPlayer.play();
        }
    }

    private void showInstructions(){
        Intent previousIntent = getIntent();

        if(!settings.contains("rulesDialog") && previousIntent.getExtras() == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final AlertDialog dialog = builder.create();

            LayoutInflater inflater = this.getLayoutInflater();

            View view = inflater.inflate(R.layout.dialog_rules, null);

            TextView rulesDialogTitle = (TextView) view.findViewById(R.id.rulesDialogTitle);
            TextView rulesDialogList = (TextView) view.findViewById(R.id.rulesDialogList);
            Button rulesDialogButton = (Button) view.findViewById(R.id.rulesDialogButton);
            Button rulesDialogCheckbox = (Button) view.findViewById(R.id.rulesDialogCheckbox);
            rulesDialogTitle.setTypeface(font);
            rulesDialogList.setTypeface(font);
            rulesDialogButton.setTypeface(font);
            rulesDialogCheckbox.setTypeface(font);

            rulesDialogCheckbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prefEditor = settings.edit();
                    prefEditor.putBoolean("rulesDialog", true)
                        .apply();
                }
            });

            rulesDialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    beginGame();
                }
            });

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    beginGame();
                }
            });

            dialog.setView(view);
            dialog.show();
        }
        else{
            beginGame();
        }
    }

    /**
     * Method that starts the game
     * Sets extras for rounds 2+
     * Gets question
     */
    public void beginGame() {
        Intent previousIntent = getIntent();
        Bundle extras = previousIntent.getExtras();

        if(extras != null){
            boolean isDifferenceSet = extras.containsKey("finalDifference");
            boolean isCounterSet = extras.containsKey("counter");

            if(isDifferenceSet) finalDifferenceRaw = previousIntent.getDoubleExtra("finalDifference", 0);
            if(isCounterSet) questionCounter = previousIntent.getIntExtra("counter", 0);

            overTotal = previousIntent.getDoubleExtra("overTotal", 0);
            underTotal = previousIntent.getDoubleExtra("underTotal", 0);
        }

        getQuestion();
    }

    /**
     * Builds the question and starts the timer
     */
    public void getQuestion() {
        final int tierMin = 1, tierMax = 3, percentLackingMin = 15, percentLackingMax = 19,
                percentGoodMin = 20, percentGoodMax = 24, percentExcellentMin = 25,
                percentExcellentMax = 29;
        int seed;
        final float totalMax = 1000, totalMin = 2;
        double totalRaw;
        SpannableString spannableString;

        questionCounter++;

        percentRand = new Random();
        totalRand = new Random();

        seed = percentRand.nextInt(tierMax - tierMin + 1) + tierMin; //range 1-3
        totalRaw = totalRand.nextFloat() * (totalMax - totalMin + 1) + totalMin;
        totalRounded = Math.round(totalRaw*100.0)/100.0;

        switch(seed){
            case 1:
                percentTemp = percentRand.nextInt(percentLackingMax - percentLackingMin + 1) + percentLackingMin;
                quality = "lacking";
                break;
            case 2:
                percentTemp = percentRand.nextInt(percentGoodMax - percentGoodMin + 1) + percentGoodMin;
                quality = "good";
                break;
            case 3:
                percentTemp = percentRand.nextInt(percentExcellentMax - percentExcellentMin + 1) + percentExcellentMin;
                quality = "excellent";
                break;
            default: percentTemp = 100;
                break;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();

        spannableString = new SpannableString(getString(R.string.roundNumber, questionCounter));
        builder.append(spannableString);

        SpannableString questionPrefix = new SpannableString(getString(R.string.question_prefix));
        builder.append(questionPrefix);

        SpannableString questionQuality;

        String qualityString;
        switch(quality){
            case "lacking":
                qualityString = String.format(locale, "%s (%d%%)", getString(R.string.quality_lacking), percentTemp);
                questionQuality = new SpannableString(qualityString);
                questionQuality.setSpan(new ForegroundColorSpan(Color.RED), 0, questionQuality.length(), 0);
                builder.append(questionQuality);
                break;
            case "good":
                qualityString = String.format(locale, "%s (%d%%)", getString(R.string.quality_good), percentTemp);
                questionQuality = new SpannableString(qualityString);
                questionQuality.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, questionQuality.length(), 0);
                builder.append(questionQuality);
                break;
            case "excellent":
                qualityString = String.format(locale, "%s (%d%%)", getString(R.string.quality_excellent), percentTemp);
                questionQuality = new SpannableString(qualityString);
                questionQuality.setSpan(new ForegroundColorSpan(Color.GREEN), 0, questionQuality.length(), 0);
                builder.append(questionQuality);
                break;
        }

        SpannableString questionSuffix = new SpannableString(getString(R.string.question_suffix));
        builder.append(questionSuffix);

        questionView.setText(builder, TextView.BufferType.SPANNABLE);

        builder.clear(); //needed?

        receiptView.setText(getString(R.string.billTotal, totalRounded));

        percent = (percentTemp / 100.0);
        realAnswer = totalRounded * percent;

        timerView.setText(String.format(locale, "%d", seconds));

        timer = new BackgroundCountDownTimer();
        timer.startTimer();
    }

    /**
     * Calculates the result as a product of the user entering an answer
     * Call for QuestionResults activity
     */
    public void getResults(){
        timer.stopTimer();

        Editable userAnswerEditable;
        userAnswerEditable = textBoxView.getText();
        String userAnswerString;
        userAnswerString = userAnswerEditable.toString();
        double userAnswerDouble = Double.parseDouble(userAnswerString);
        userAnswerString = String.format(locale, "%.2f", userAnswerDouble);
        String realAnswerString  = String.format(locale, "%.2f", realAnswer);

        realAnswerConverted = Double.parseDouble(realAnswerString);
        userAnswerConverted = Double.parseDouble(userAnswerString);

        try{
            intent = new Intent(context, QuestionResultsActivity.class);
            double userAnswer = Double.parseDouble(userAnswerString);

            if(userAnswerConverted == realAnswerConverted){
                userPercent = (percent * 100);
                difference = 0;
                intent.putExtra("accuracy", "equal");
            }
            else if(realAnswer > userAnswer){
                userPercent = (userAnswer/totalRounded) * 100;
                difference = realAnswer - userAnswer;
                finalDifferenceRaw = finalDifferenceRaw + (realAnswer - userAnswer);
                underTotal = underTotal + difference;
                intent.putExtra("accuracy", "low");
            }
            else{
                userPercent = (userAnswer/totalRounded) * 100;
                difference = userAnswer - realAnswer;
                finalDifferenceRaw = finalDifferenceRaw + (userAnswer - realAnswer);
                overTotal = overTotal + difference;
                intent.putExtra("accuracy", "high");
            }

            finalDifferenceRounded = Math.round(finalDifferenceRaw*100.0)/100.0;
            intent.putExtra("finalDifference", finalDifferenceRounded)
                    .putExtra("difference", difference)
                    .putExtra("totalRounded", totalRounded)
                    .putExtra("quality", quality)
                    .putExtra("userAnswer", userAnswerConverted)
                    .putExtra("realAnswer", realAnswerConverted)
                    .putExtra("counter", questionCounter)
                    .putExtra("userPercent", userPercent)
                    .putExtra("underTotal", underTotal)
                    .putExtra("overTotal", overTotal)
                    .putExtra("percent", percent);
            startActivity(intent);
            finish();
        }
        catch(NumberFormatException nf){
            nf.printStackTrace();
        }
    }

    /**
     * Calculates the result as a product of the timer expiring
     * Call for QuestionResults activity
     */
    public void timesUp(){
        timer.stopTimer();

        intent = new Intent(context, QuestionResultsActivity.class);

        String realAnswerString  = String.format(locale, "%.2f", realAnswer);
        realAnswerConverted = Double.parseDouble(realAnswerString);

        difference = realAnswer - userAnswer;
        finalDifferenceRaw = finalDifferenceRaw + (realAnswer - userAnswer);
        intent.putExtra("resultString", String.format(locale, "You tipped %.2f less than you should have!",
                difference));
        underTotal = underTotal + difference;

        finalDifferenceRounded = Math.round(finalDifferenceRaw*100.0)/100.0;
        intent.putExtra("finalDifference", finalDifferenceRounded)
                .putExtra("totalRounded", totalRounded)
                .putExtra("counter", questionCounter)
                .putExtra("accuracy", "low")
                .putExtra("difference", difference)
                .putExtra("quality", quality)
                .putExtra("userAnswer", userAnswer)
                .putExtra("realAnswer", realAnswerConverted)
                .putExtra("userPercent", userPercent)
                .putExtra("underTotal", underTotal)
                .putExtra("overTotal", overTotal)
                .putExtra("percent", percent);
        startActivity(intent);
        finish();
    }

    /**
     * Answer validation
     * Rejects empty string and any answer above 999
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        playClickSound();

        if(keyCode == KeyEvent.KEYCODE_ENTER){
            Editable userAnswerEditable = textBoxView.getText();
            String userAnswer = userAnswerEditable.toString();
            if(!userAnswer.matches("")) {
                if (Double.parseDouble(userAnswer) > 999) {
                    Toast.makeText(context, getString(R.string.toastInputTooLarge), Toast.LENGTH_SHORT).show();
                } else {
                    getResults();
                }
            }
            else{
                Toast.makeText(context, getString(R.string.toastInputEmpty), Toast.LENGTH_SHORT).show();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public void playClickSound(){
        if(settings.getBoolean("sounds", false)) {
            MediaPlayer audioPlayer = MediaPlayer.create(context, R.raw.sound_click);
            audioPlayer.start();
        }
    }

    /**
     * Broadcast receiver class
     * Requests a second interval (getASecond) and updates timer when received
     * Calls for results if time is up (timesUp)
     */
    private class TimerStateReceiver extends BroadcastReceiver{
        private TimerStateReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            timerView.setText(String.format(locale, "%d", --seconds));

            if(seconds > 0 && !paused){
                //do nothing
            } else if(seconds == 0 && !paused){
                timesUp();
            }
            else{
                getSecondConcluded = true;
            }
        }
    }

    /**
     * Class that handles the in-game timer
     * Puts background thread to sleep for one second
     * Broadcasts a callback to receiver
     */
    public class BackgroundTimer extends IntentService {
        static public final String SYNC_KEYWORD = "com.danm551.tippinggame.sync";

        public BackgroundTimer() {
            super(BackgroundTimer.class.getName());
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            try{
                Thread.sleep(1000);
            }
            catch(InterruptedException e1){
                e1.printStackTrace();
            }

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(SYNC_KEYWORD);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }
    }

    private class BackgroundCountDownTimer{
        CountDownTimer cdt = new CountDownTimer(totalTime + 2000, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(seconds != 0){
                    timerView.setText(String.format(locale, "%d", seconds--));
                }
                else if(seconds == 0){
                    timesUp();
                }
            }

            @Override
            public void onFinish() {}
        };

        void startTimer(){cdt.start();}

        void stopTimer(){
            seconds += 1;
            totalTime = seconds * 1000;
            cdt.cancel();
        }
    }
}