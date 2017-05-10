package com.hyunseok.android.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-02-01.
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.Holder> {

    List<Music> datas;
    Context context;
    Intent intent;

    // 생성자
    public MusicAdapter(List<Music> datas, Context context) {
        this.datas = datas;
        this.context = context;
        intent = new Intent(context, PlayerActivity.class);
    }

    // 홀더(한 페이지)에 모양을 저장
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_item, parent, false); // context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) 와 같다.
        return new Holder(view);
    }

    // Holder는 넘겨받은 view 하나의 단위.
    // onBindViewHolder는 Holder를 View할 때의 방식(?) 정할 수 있는 함수
    // 페이지가 바뀔때마다 호출됨. 페이지를 세팅해줌.
    // 시스템이 한 페이지에 표시할 수 있는 만큼 onBindViewHolder를 호출한다.
    @Override
    public void onBindViewHolder(Holder holder, int position) {

        // 1. 데이터를 행 단위로 꺼낸다.
        final Music music = datas.get(position); // 멤버변수가 아닌 지역변수를 참조할땐 상수로 가져와야함.

        // 2. 홀더에 데이터를 세팅한다.
        holder.tv_title.setText(music.title);
        holder.tv_artist.setText(music.artist);
        //holder.imageView.setImageURI(music.album_img); // DataLoader클래스에 getAlbumImageSimple()사용시
        //if(music.bitmap_img != null)
            //holder.imageView.setImageBitmap(music.bitmap_img); // DataLoader클래스에 getAlbumImageBitmap()사용시
        Glide.with(context).load(music.album_img).into(holder.imageView); // Glide사용 속도가 매우빠르다.
        //                      로드할 대상Uri,          입력될 ImageView   // 화면에 표시되는 것만 로드하고 나머진 지우는 식으로 캐시 관리를 해준다.

        holder.position = position;

        // 3. 액션을 정의한다. (리스너 세팅)
        // ImageButton에 클릭리스너를 달아서 동작시킨다.

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        TextView tv_title, tv_artist;
        CardView cardView;
        ImageView imageView;

        int position;

        public Holder(View itemView) {
            super(itemView);

            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            tv_artist = (TextView) itemView.findViewById(R.id.tv_artist);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            cardView = (CardView) itemView.findViewById(R.id.cardView);

            cardView.setOnClickListener(listener);
        }

        private View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        };
    }
}
