package com.danm551.tippinggame;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

class BGMPlayer{
    private Context context;
    private MediaPlayer bgmPlayer;
    private String songSelection;
    private boolean pause = false;

    BGMPlayer(Context c, String s){
        context = c;
        bgmPlayer = new MediaPlayer();
        songSelection = s;
    }

    /**
     * Prepares bgm file using prepareAsync, then calls for play when ready
     */
    void play(){
        Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    Uri sound = Uri.parse(String.format("android.resource://%s/%s/%s", context.getPackageName(), "raw", songSelection));
                    bgmPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    bgmPlayer.setDataSource(context, sound);
                    bgmPlayer.setLooping(true);
                    bgmPlayer.setVolume(0.25F, 0.25F);
                    bgmPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                    if(!bgmPlayer.isPlaying()) {
                        bgmPlayer.prepareAsync();
                    }
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }

        };

        thread.start();
    }

    void pause(){
        pause = true;
        bgmPlayer.pause();
    }

    void stop(){
        if(bgmPlayer.isPlaying()) bgmPlayer.stop();
    }


    void resume(){
        if(pause) {
            pause = false;
            bgmPlayer.start();
        }
    }

    void release(){
        bgmPlayer.stop();
        bgmPlayer.release();
        bgmPlayer = null;
    }
}
