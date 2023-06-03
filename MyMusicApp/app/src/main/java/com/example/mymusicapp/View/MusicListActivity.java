package com.example.mymusicapp.View;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusicapp.Adapter.RecycleViewAdapter;
import com.example.mymusicapp.Controller.MusicController;
import com.example.mymusicapp.Model.Music;
import com.example.mymusicapp.R;

import java.util.List;

public class MusicListActivity extends AppCompatActivity implements View.OnClickListener{
    RecyclerView musicListView;

    MusicController controller;
    RecycleViewAdapter adapter;
    List<Music> musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        init();
    }

    private void init() {
        controller = MusicController.getController();
        musicList = controller.getMusicList();
        adapter = new RecycleViewAdapter();
        adapter.setData(musicList);
        initView();
    }

    private void initView() {
        musicListView = findViewById(R.id.music_list_view);
        musicListView.setLayoutManager(new LinearLayoutManager(this));
        musicListView.setAdapter(adapter);
        adapter.setClickListener(new RecycleViewAdapter.OnItemViewClickListener() {
            @Override
            public void onClick(int position) {
                //单击播放指定歌曲
                controller.setMusicSource(position);
            }

            @Override
            public void onLongClick(int position) {
                //长按删除歌曲，如果不止有一首歌
                if (musicList.size() > 1) {
                    controller.deleteMusic(position);
                    adapter.removeItemView(position);
                } else Toast.makeText(MusicListActivity.this, "只剩一个了", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {

    }
}
