package com.hyunseok.android.musicplayer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static android.drm.DrmStore.Action.PLAY;
import static com.hyunseok.android.musicplayer.App.ACTION_PAUSE;
import static com.hyunseok.android.musicplayer.App.ACTION_PLAY;
import static com.hyunseok.android.musicplayer.App.ACTION_RESTART;
import static com.hyunseok.android.musicplayer.App.PAUSE;
import static com.hyunseok.android.musicplayer.App.PLAYING;
import static com.hyunseok.android.musicplayer.App.STOP;
import static com.hyunseok.android.musicplayer.App.playStatus;
import static com.hyunseok.android.musicplayer.App.player;
import static com.hyunseok.android.musicplayer.App.position;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener{

    ViewPager viewPager_Player;
    ImageButton imgbtn_play, imgbtn_prev, imgbtn_next;

    List<Music> datas; // ArrayList
    PlayerAdapter adapter;

    //MediaPlayer player;
    SeekBar seekBar;
    TextView tv_duration;

    Intent service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        service = new Intent(this, PlayerService.class);

        //playStatus = STOP;

        setVolumeControlStream(AudioManager.STREAM_MUSIC); // 볼륨 컨트롤을 Ringtone이 아닌 Media로 바꿈.

        imgbtn_prev = (ImageButton) findViewById(R.id.imgbtn_prev);
        imgbtn_play = (ImageButton) findViewById(R.id.imgbtn_play);
        imgbtn_next = (ImageButton) findViewById(R.id.imgbtn_next);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        tv_duration = (TextView) findViewById(R.id.tv_duration);

        imgbtn_next.setOnClickListener(this);
        imgbtn_play.setOnClickListener(this);
        imgbtn_prev.setOnClickListener(this);


        // 0. 데이터 가져오기
        datas = DataLoader.get(this);
        // 1. ViewPager 가져오기
        viewPager_Player = (ViewPager) findViewById(R.id.viewPager_Player);
        // 2. ViewPager용 아답터 생성
        adapter = new PlayerAdapter(datas, this);
        // 3. ViewPager 아답터 연결
        viewPager_Player.setAdapter(adapter); // 위젯에 특성에 맞게 아답터가 있다. 위젯이 달라지면 사용 안되므로 새로운 커스텀아답터를 만들어준다.
                                                // ViewPlager의 아답터는 PagerAdapter
        // 4. ViewPager, SeekBar 체인지 리스너 연결
        viewPager_Player.addOnPageChangeListener(viewPagerListener);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        // PageTransformer 연결
        viewPager_Player.setPageTransformer(false, pageTransformer);

        // 5. RecyclerView위에 있는 CardView에서 한 Card아이템을 클릭시 특정 페이지 호출
        Intent intent = getIntent();
        if(intent != null) {
            Bundle bundle =  intent.getExtras();
            position = bundle.getInt("position");

            // 첫 페이지일 경우만 init() 호출
            // 첫 페이지가 아닐 경우 위의 setCurrentItem에 의해서 ViewPager의 onPageSlected가 호출되므로 init이 두번됨
            if(position == 0) {
                init(); // 음악 기본 정보를 설정해준다.
            } else {
                viewPager_Player.setCurrentItem(position);
            }
        }
    }

    /**
     * 음악 초기 세팅(player, seeBar, setText)
     */
    private void init() {
        if( (player != null) && (playStatus != PLAYING) ) {
            playStatus = STOP;
            imgbtn_play.setImageResource(android.R.drawable.ic_media_play);
            player.release();
        }

        playerInit();
        controllerInit();

        if(App.playStatus != PLAYING) {
            play();
        } else {

        }
    }
    private void playerInit() {

        Uri music_uri = datas.get(position).uri;

        if(player != null) { // viewPager로 이동할 경우 Player에 세팅된 값을 해제한 후 로직을 실행한다.
            playStatus = STOP;
            imgbtn_play.setImageResource(android.R.drawable.ic_media_play);
            player.release(); // 이전에 실행되던 플레이어를 STOP상태, release해주고 다시 실행
        }

        player = MediaPlayer.create(this, music_uri); // MediaPlayer를 사용하기 위해선 당연히 시스템자원이 필요하므로 this는 필수
        player.setLooping(false); // 반복 여부

        // 미디어 플레이어에 완료 체크 리스너를 등록한다.
        // 음악이 끝까지 재생됐을 때
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });
    }
    private void controllerInit() {
        seekBar.setMax(player.getDuration()); // seekBar의 최대 길이 설정
        seekBar.setProgress(0);
        //tv_duration.setText(player.getDuration()/1000 + "");
    }

    private void prev() {
        if( position > 0 ) {
            viewPager_Player.setCurrentItem(position - 1);
        }
    }

    private void play() {

        switch(playStatus) {
            case STOP:
                playStart();
                break;
            case PLAYING:
                playPause();
                break;
            case PAUSE:
                playRestart();
                break;
        }
    }

    private void playStart() {

        playStatus = PLAYING;
        service.setAction(ACTION_PLAY); // 서비스쪽으로 명령어를 보내준다. putExtra와 비슷한 개념
        startService(service);

        //player.start(); // 미디어 실행
        //playStatus = PLAYING;
        imgbtn_play.setImageResource(android.R.drawable.ic_media_pause);

        // SeekBar와 남은시간(TextView)을 실행하는 쓰레드
        Thread thread = new TimerThread();
        thread.start();
    }
    private void playPause() {
        //player.pause();
        playStatus = PAUSE;
        service.setAction(ACTION_PAUSE);
        startService(service);
        imgbtn_play.setImageResource(android.R.drawable.ic_media_play);
    }
    private void playRestart() {
        //player.start();
        playStatus = PLAYING;
        service.setAction(ACTION_RESTART);
        startService(service);
        imgbtn_play.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void next() {
        if( position < datas.size() ) {
            viewPager_Player.setCurrentItem(position + 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(player != null) {
//            player.release(); // close() 비슷한 개념
//        }
//        playStatus = STOP;
    }

    /**
     * 버튼의 클릭리스너
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.imgbtn_prev :
                prev();
                break;
            case R.id.imgbtn_play :
                play();
                break;
            case R.id.imgbtn_next :
                next();
                break;
        }
    }

    /**
     * ViewPager의 ChangeListener
     */
    ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            //Logger.print("pagechangelis", "1111");
            App.position = position;
            init();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * SeekBar의 ChangeListener
     */
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if( (player != null) && (fromUser == true) ) {
                player.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {


        }
    };

    ViewPager.PageTransformer pageTransformer = new ViewPager.PageTransformer() {
        @Override
        public void transformPage(View page, float position) {
            float normalizedposotion = Math.abs( 1 - Math.abs(position) );

            page.setAlpha(normalizedposotion);
            page.setScaleX(normalizedposotion/2 + 0.5f);
            page.setScaleY(normalizedposotion/2 + 0.5f);
            page.setRotationY(position * 80);
        }
    };
    /**
     *
     */
    class SeekBarUpdate extends Thread {

        @Override
        public void run() {
            //super.run(); // 부모객체 것을 실행시키는 것

            while(playStatus < STOP) { // PAUSE 상태에서도 체크해야하기 때문에
                if(player != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(player.getCurrentPosition());
                        }
                    });
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * SeekBar와 남은시간(TextView)을 실행하는 쓰레드
     */
    class TimerThread extends Thread {
        @Override
        public void run() {
            while(playStatus < STOP) {
                if(player != null) {
                    runOnUiThread(new Runnable() { // 이 부분은 메인쓰레드에서 동작하도록 Runnable instance를 메인쓰레드에 던져준다.
                        public void run() {
                            int remain_time = (player.getDuration() - player.getCurrentPosition())/1000;
                            int min = remain_time / 60;
                            int sec = remain_time % 60;
                            String str_m = String.format("%02d", min);
                            String str_s = String.format("%02d", sec);

                            try {
                                seekBar.setProgress(player.getCurrentPosition()); // 프로그레스바 진행 표시
                                tv_duration.setText(str_m + ":" + str_s);
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                    });
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) { e.printStackTrace(); }
            }
        }
    }
}
