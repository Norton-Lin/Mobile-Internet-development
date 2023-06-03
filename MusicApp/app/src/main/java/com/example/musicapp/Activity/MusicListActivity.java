package com.example.musicapp.Activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.Apater.RecycleViewAdapter;
import com.example.musicapp.Controller.MusicController;
import com.example.musicapp.Model.Music;
import com.example.musicapp.R;
import java.util.List;

/**
 * @author Norton
 * @date 2023.5.28
 * @brief 音乐列表窗口
 */
public class MusicListActivity extends BaseActivity {
    RecyclerView music_list_view;

    MusicController manager;
    RecycleViewAdapter adapter;
    List<Music> musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        init();
    }

    private void init() {
        manager = MusicController.getManager();
        musicList = manager.getAllSongs();
        adapter = new RecycleViewAdapter();
        adapter.setData(musicList);
        initView();
    }

    private void initView() {
        music_list_view = findViewById(R.id.music_list_view);
        music_list_view.setLayoutManager(new LinearLayoutManager(this));
        music_list_view.setAdapter(adapter);
        adapter.setItemViewClickListener(new RecycleViewAdapter.OnItemViewClickListener() {
            @Override
            public void onClick(int position) {
                // 单击播放指定歌曲
                manager.setPlaySongByPosition(position);
            }

            @Override
            public void onLongClick(int position) {
                // 长按删除歌曲，如果不止有一首歌
                if (musicList.size() > 1) {
                    manager.removeSong(position);
                    adapter.removeItemView(position);
                } else
                    Toast.makeText(MusicListActivity.this, "只剩一个了", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
