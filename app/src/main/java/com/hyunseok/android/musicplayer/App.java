package com.hyunseok.android.musicplayer;

import android.media.MediaPlayer;

/**
 *
 * Created by Administrator on 2017-02-24.
 */

public class App {

    public static int ABC;

    public static MediaPlayer player = null;

    // 액션 플래그
    public static final String ACTION_PLAY = "com.hyunseok.android.musicplayer.Action.Play";
    public static final String ACTION_PAUSE = "com.hyunseok.android.musicplayer.Action.Pause";
    public static final String ACTION_RESTART = "com.hyunseok.android.musicplayer.Action.Restart";


    // 플레이어 상태플래그
    public static final int PLAYING = 0;
    public static final int PAUSE = 1;
    public static final int STOP = 2;

    public static int playStatus = STOP;

    public static int position = 0; // 현재 음악 위치
}
