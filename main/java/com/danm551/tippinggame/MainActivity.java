package com.danm551.tippinggame;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SettingsFragment.SettingsListener{
    final private String FRAGMENT_TYPE_RULES = "RULES";
    final private String FRAGMENT_TYPE_ABOUT = "ABOUT";
    final private String FRAGMENT_TYPE_SETTINGS = "SETTINGS";
    final private String FRAGMENT_TYPE_CREDITS = "CREDITS";
    final private String PREF_GENERAL = "TIPS_PREFS";
    final private String MUSIC_MAIN_MENU = "music_main_menu";
    final private String FONT_RIGHTEOUS = "font_righteous_regular.ttf";
    final private Context context = this;
    private boolean actionBarTitleTransition = false, bgmPref, sfxPref;
    private ActionBarDrawerToggle drawerToggle;
    private BGMPlayer bgmPlayer;
    private Fragment drawerFragment;
    private ListView drawerListView;
    private SharedPreferences.Editor prefEditor;
    private String tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPreferences();
        setupDrawer();
        setListeners();
        setFonts();
        startMusic();
    }

    @Override
    public void onResume(){
        super.onResume();

        if(bgmPlayer != null) bgmPlayer.resume();
    }

    @Override
    public void onPause(){
        super.onPause();

        if(bgmPlayer != null) bgmPlayer.pause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(bgmPlayer != null) bgmPlayer.release();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if(fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        } else{
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.action_exit))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    }

    @Override
    public void ambientSoundOff() {
        bgmPlayer.stop();
    }

    @Override
    public void ambientSoundOn() {
        if(bgmPlayer != null) bgmPlayer.release();

        startMusic();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void setPreferences(){
        SharedPreferences settings = getSharedPreferences(PREF_GENERAL, MODE_PRIVATE);
        prefEditor = settings.edit();

        if(!settings.contains("ambientSound")){
            prefEditor.putBoolean("ambientSound", true);
            prefEditor.apply();
        }

        if(!settings.contains("sounds")){
            prefEditor.putBoolean("sounds", true);
            prefEditor.apply();
        }

        if(!settings.contains("difficulty")){
            prefEditor.putString("difficulty", "normal");
            prefEditor.apply();
        }

        bgmPref = settings.getBoolean("ambientSound", false);
        sfxPref = settings.getBoolean("sounds", false);
    }

    /**
     * Builds the navigation drawer
     */
    private void setupDrawer(){
        String[] drawerOptions = getResources().getStringArray(R.array.drawerOptions);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerListView = (ListView) findViewById(R.id.list_drawer);

        drawerListView.setAdapter(new CustomArrayAdapter(context,
                R.layout.custom_question_results_listview,
                drawerOptions,
                0));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);//should be safe to delete. Test
        getSupportActionBar().setTitle(getResources().getString(R.string.settingDrawerTitle));

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawerOpen, R.string.drawerClose){
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                if(getSupportActionBar().getTitle() == "" && !actionBarTitleTransition){
                    getSupportActionBar().setTitle(getString(R.string.settingDrawerTitle));
                    actionBarTitleTransition = true;
                }
                else if(getSupportActionBar().getTitle() == getString(R.string.settingDrawerTitle) && !actionBarTitleTransition) {
                    getSupportActionBar().setTitle("");
                    actionBarTitleTransition = true;
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();

                actionBarTitleTransition = false;
            }


            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                invalidateOptionsMenu();

                actionBarTitleTransition = false;

                FragmentManager fm = getSupportFragmentManager();
                while(fm.getBackStackEntryCount() > 0) fm.popBackStackImmediate();
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void setListeners(){
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int mId = (int) id;
                switch(mId){
                    case 0:
                        buildDrawerFragment(FRAGMENT_TYPE_RULES);
                        break;
                    case 1:
                        buildDrawerFragment(FRAGMENT_TYPE_SETTINGS);
                        break;
                    case 2:
                        buildDrawerFragment(FRAGMENT_TYPE_ABOUT);
                        break;
                    case 3:
                        buildDrawerFragment(FRAGMENT_TYPE_CREDITS);
                        break;
                }

            }
        });
    }

    private void setFonts(){
        Typeface typeface = Typeface.createFromAsset(getAssets(), FONT_RIGHTEOUS);

        TextView titleText = (TextView) findViewById(R.id.text_title);
        Button playButton = (Button) findViewById(R.id.button_play);
        Button mainActivityHiScoresBtn = (Button) findViewById(R.id.button_hi_scores);

        titleText.setTypeface(typeface);
        playButton.setTypeface(typeface);
        mainActivityHiScoresBtn.setTypeface(typeface);
    }

    private void startMusic(){
        if(bgmPref) {
            bgmPlayer = new BGMPlayer(context, MUSIC_MAIN_MENU);
            bgmPlayer.play();
        }
    }

    public void startGame(View v){
        playClickSound();
        Intent intent = new Intent(context, GameActivity.class);
        startActivity(intent);
        finish();
    }

    private void playClickSound(){
        if(sfxPref) {
            MediaPlayer audioPlayer = MediaPlayer.create(context, R.raw.sound_click);
            audioPlayer.start();
        }
    }

    /**
     * Controls navigation drawer fragment replacement
     */
    private void buildDrawerFragment(String code){
        FragmentManager fm = getSupportFragmentManager();
        if(fm.getBackStackEntryCount() == 0) {
            switch (code) {
                case FRAGMENT_TYPE_RULES:
                    drawerFragment = new RulesFragment();
                    tag = FRAGMENT_TYPE_RULES;
                    break;
                case FRAGMENT_TYPE_ABOUT:
                    drawerFragment = new AboutFragment();
                    tag = FRAGMENT_TYPE_ABOUT;
                    break;
                case FRAGMENT_TYPE_SETTINGS:
                    drawerFragment = new SettingsFragment();
                    tag = FRAGMENT_TYPE_SETTINGS;
                    break;
                case FRAGMENT_TYPE_CREDITS:
                    drawerFragment = new CreditsFragment();
                    tag = FRAGMENT_TYPE_CREDITS;
                    break;
                }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame_drawer_placeholder, drawerFragment, tag)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public void showHiScores(View v){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.defaultMainActivityFrame, new HiScoresFragment())
            .addToBackStack(null)
            .commit();
    }
}
