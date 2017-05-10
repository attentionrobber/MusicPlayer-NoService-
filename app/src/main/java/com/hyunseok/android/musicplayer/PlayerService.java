package com.hyunseok.android.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import static com.hyunseok.android.musicplayer.App.ACTION_PAUSE;
import static com.hyunseok.android.musicplayer.App.ACTION_PLAY;
import static com.hyunseok.android.musicplayer.App.ACTION_RESTART;
import static com.hyunseok.android.musicplayer.App.PAUSE;
import static com.hyunseok.android.musicplayer.App.PLAYING;
import static com.hyunseok.android.musicplayer.App.playStatus;
import static com.hyunseok.android.musicplayer.App.player;

public class PlayerService extends Service {
    public PlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_PLAY:
                    playStart();
                    break;
                case ACTION_PAUSE:
                    playPause();
                    break;
                case ACTION_RESTART:
                    playRestart();
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void playStart() {
        player.start(); // 미디어 실행
        playStatus = PLAYING;
    }

    private void playPause() {
        player.pause();
        playStatus = PAUSE;
    }

    private void playRestart() {
        player.start();
        playStatus = PLAYING;
    }
}
