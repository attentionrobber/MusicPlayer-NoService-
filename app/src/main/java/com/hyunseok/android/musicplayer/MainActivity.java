package com.hyunseok.android.musicplayer;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.hyunseok.android.musicplayer.App.PLAYING;

public class MainActivity extends AppCompatActivity {

    // Player Activity와 MusicAdapter를 공유하기 위해 Static으로 선언.
    // 쓰기는 쉬우나 최선인지는 고민해 볼 필요있음. (Activity가 new되지 않아도 메소드에 모두 올라감) ==>> viewPager아답터를 새로만듦으로써 해결
    //public static MusicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 앱을 껐다가 Player가 실행중이면 PlayerActivity로 이동시킨다.
        if(App.playStatus == PLAYING) {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("posotion", App.position);
            startActivity(intent);
            finish();
        } else {

            // 버전체크해서 마시멜로우보다 낮으면 런타임권한 체크를 하지 않는다.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkPermission();
            } else {
                init();
            }
        }
    }

    /**
     * 초기화 함수(데이터를 로드할 함수)
     */
    private void init() {

        // 데이터를 불러온다.
//        DataLoader loader = new DataLoader(this);
//        loader.load(); //datas = loader.get(); // 생성자함수에서 load()함수를 호출 했을 때.
//        ArrayList<Music> datas = loader.get();
        List<Music> datas = DataLoader.get(this);

        // 1. Recycler View(ViewHolder까지 있음)
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView);
        // 2. Adapter 생성하기
        MusicAdapter adapter = new MusicAdapter(datas, this);
        // 3. Recycler View에 Adapter 세팅하기
        rv.setAdapter(adapter);
        // 4. Recycler View 매니저 등록하기(View의 모양(Grid, 일반, 비대칭Grid)을 결정한다.)
        rv.setLayoutManager(new LinearLayoutManager(this));

    }



    private final int REQ_CODE = 100;

    // 1. 권한 체크
    @TargetApi(Build.VERSION_CODES.M) // Target 지정 Annotation
    private void checkPermission() {
        // 1.1 런타임 권한 체크
        if( checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) { // Permission이 없을 경우. 쓰기만하면 읽기도 자동으로 가능.

            // 1.2 요청할 권한 목록 작성
            String perArr[] = {
                    Manifest.permission.READ_EXTERNAL_STORAGE };

            // 1.3 시스템에 권한 요청.
            requestPermissions(perArr, REQ_CODE);

        } else { // Permission이 있을 경우.
            init();
        }
    }

    // 2. 권한 체크 후 콜백(사용자가 확인 후 시스템이 호출하는 함수)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQ_CODE) {
            // 배열에 넘긴 런타임권한을 체크해서 승인이 된 경우 // 두개의 권한 [0] [1] 모두 그랜트 되었을 경우
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                init();
            } else {
                Message.show(this, "권한을 설정하지 않으면 프로그램이 실행되지 않습니다.");
                // TODO 선택 : 종료, 다시 물어보기
            }
        }
    }
}
