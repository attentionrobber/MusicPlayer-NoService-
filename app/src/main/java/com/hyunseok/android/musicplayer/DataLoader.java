package com.hyunseok.android.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-02-01.
 */

public class DataLoader {

    // 1. 데이터 컨텐츠 URI정의
    // 데이터URI(음악) : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private final static Uri URI_MUSIC = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    // 2. 데이터에서 가져올 데이터 컬럼명을 projections에 담는다.
    // 데이터 컬럼명은 Content URI의 패키지에 들어있다. MediaStore.Audio.Media. 에서 찾으면 됨.
    private final static String PROJ[] = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST };

    private static List<Music> datas = new ArrayList<>(); // SOLID 를 적용 선언부를 ArrayList -> List로 바꿈

    // 일반적으로 데이터(ArrayList<Music> datas)를 public static으로 사용하기보다는
    // get()함수를 public static으로 사용해서 다른 클래스끼리 공유한다.
    // datas를 두 개의 Activity에서 공유하기 위해 static으로 변경. ==>> 생성자 함수가 필요가없어짐.
    public static List<Music> get(Context context) {
        if(datas == null || datas.size() == 0) {
            load(context);
        }
        return datas;
    }

    public static void load(Context context) {

        // 1. 데이터에 접근하기 위해 Content Resolver를 불러온다.
        ContentResolver resolver = context.getContentResolver();

        // 3. Content Resolver로 쿼리한 데이터를 cursor에 담게된다.
        Cursor cursor = resolver.query(URI_MUSIC, PROJ, null, null, null);

        if(cursor != null) {
            // 4. cursor에 담긴 데이터를 반복문을 돌면서 꺼내고 datas에 담아준다.
            while( cursor.moveToNext() ) {

                Music music = new Music();

                // 5. 커서의 컬럼 인덱스를 가져온 후 컬럼인덱스에 해당하는 proj을 세팅
                music.id = getValue(cursor, PROJ[0]);
                music.album_id = getValue(cursor, PROJ[1]);
                music.title = getValue(cursor, PROJ[2]);
                music.artist = getValue(cursor, PROJ[3]);

                music.album_img = Uri.parse("content://media/external/audio/albumart/" + music.album_id);
                //music.album_img = getAlbumImageSimple(music.album_id); // URI로 직접 이미지를 로드한다. (이미지 못불러오는 경우 있음)
                //music.bitmap_img = getAlbumImageBitmap(music.album_id, context); // Bitmap으로 처리해서 이미지를 로드한다. (매우느림)
                music.uri = getMusicUri(music.id);

                datas.add(music);
            }
            // 6. 사용 후 close를 해주지 않으면 메모리 누수가 발생할 수 있다.
            cursor.close();
        }
    }

    private static String getValue(Cursor cursor, String columName) {
        int idx = cursor.getColumnIndex(columName);
        return cursor.getString(idx);
    }

    // 음악id로 Uri를 가져오는 함수
    private static Uri getMusicUri(String music_id) {

        Uri content_uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        return Uri.withAppendedPath(content_uri, music_id); // Uri 주소를 합쳐주는 함수
    }

    /**
     * 가장 간단하게 앨범이미지를 가져오는 방법
     * 문제점 : 실제 앨범데이터만 있어서 이미지를 불러오지 못하는 경우가 있다.
     * @param album_id
     * @return
     */
    @Deprecated
    private Uri getAlbumImageSimple(String album_id) {
        return Uri.parse("content://media/external/audio/albumart/" + album_id);
    }

    /**
     * Content Resolver를 통해 가져옴
     * @param album_id
     * @return
     */
    @Deprecated
    private static Bitmap getAlbumImageBitmap(String album_id, Context context) {
        // 1. 앨범아이디로 URI생성
        Uri uri = Uri.parse("content://media/external/audio/albumart/" + album_id);
        // 2. ContentResolver 가져오기
        ContentResolver resolver = context.getContentResolver();

        try {
            // 3. resolver에서 Stream열기
            InputStream is = resolver.openInputStream(uri);
            // 4. BitmapFactory를 통해 이미지 데이터를 가져온다.
            Bitmap img = BitmapFactory.decodeStream(is); // Bitmap이미지를 Stream의 형태로 가져오면 Decode를 해줘야한다.

            return img;
        } catch (FileNotFoundException e) {
            Logger.print(e.toString(), "getAlbumImageBitmap");
            e.printStackTrace(); // 동일 스레드가 아닌 다른 스레드에서 실행되므로 시스템에 영향을 미치지 않는다. Sysout은 시스템 성능에 영향을미침.
        }
        return null;
    }
}
