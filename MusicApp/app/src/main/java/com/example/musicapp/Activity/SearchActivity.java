package com.example.musicapp.Activity;

import static com.example.musicapp.Util.Utils.getUrl;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.Apater.RecycleViewAdapter;
import com.example.musicapp.Controller.MusicController;
import com.example.musicapp.Controller.RetrofitController;
import com.example.musicapp.Model.Music;
import com.example.musicapp.Model.MusicSearch;
import com.example.musicapp.NetWork.NetworkRequest;
import com.example.musicapp.R;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Norton
 * @date 2023.5.28
 * @brief 音乐搜索窗口
 */
public class SearchActivity extends BaseActivity {
    Toolbar search_bar;
    Button bt_play_all;
    RecyclerView search_result_list;

    MusicController manager;
    List<Music> songs;
    RecycleViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
    }

    private void init() {
        manager = MusicController.getManager();
        songs = new ArrayList<>();
        adapter = new RecycleViewAdapter();
        initView();
    }

    private void initView() {
        search_bar = findViewById(R.id.search_bar);
        bt_play_all = findViewById(R.id.bt_play_all);
        search_result_list = findViewById(R.id.search_result_list);
        search_result_list.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(search_bar);

        bt_play_all.setOnClickListener(this);

        adapter.setItemViewClickListener(new RecycleViewAdapter.OnItemViewClickListener() {
            @Override
            public void onClick(int position) {
                // 单击播放，并结束此活动
                manager.addSong(songs.get(position));
                SearchActivity.this.finish();
            }

            @Override
            public void onLongClick(int position) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.bt_play_all) {
            if (songs.isEmpty()) {
                Toast.makeText(this, "没有结果", Toast.LENGTH_SHORT).show();
            } else {
                manager.addAllSongs(songs);
                this.finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载menu，加载searchView
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus(); // 收起键盘
                searchView.setQuery("", false);
                searchView.onActionViewCollapsed(); // 收起搜索框
                searchMusic(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void searchMusic(String query) {
        NetworkRequest request = RetrofitController.getRetrofit().create(NetworkRequest.class);
        Call<MusicSearch> searchResult = request.getSearchResult(getUrl(query, 0));
        searchResult.enqueue(new retrofit2.Callback<MusicSearch>() {
            @Override
            public void onResponse(@NonNull Call<MusicSearch> call, @NonNull Response<MusicSearch> response) {
                if (response.code() == HttpURLConnection.HTTP_OK && response.body() != null) {
                    // 如果code==200 且响应体不为空
                    MusicSearch result = response.body();
                    songs.clear();
                    for (int i = 0; i < result.result.songs.size(); i++) {
                        // 处理结果，将MusicSearchResult类中的有用信息封装成MusicData类
                        Music data = new Music();
                        data.setId(String.valueOf(result.result.songs.get(i).id));
                        data.setName(result.result.songs.get(i).name);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < result.result.songs.get(i).ar.size(); j++) {
                            // 拼接歌手名字，中间 / 分开
                            stringBuilder.append(result.result.songs.get(i).ar.get(j).name);
                            stringBuilder.append("/");
                        }
                        // 删去最后的 / 符号
                        String artist = stringBuilder.substring(0, stringBuilder.length() - 1);
                        data.setSinger(artist);
                        data.setPicUrl(result.result.songs.get(i).al.picUrl);
                        songs.add(data);
                    }
                    // 设置列表数据
                    setData(songs);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MusicSearch> call, @NonNull Throwable t) {
                Toast.makeText(SearchActivity.this, "error\n请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setData(List<Music> songsList) {
        runOnUiThread(() -> {
            if (songsList.isEmpty()) {
                Toast.makeText(this, "没有结果", Toast.LENGTH_SHORT).show();
                return;
            }
            adapter.setData(songsList);
            search_result_list.setAdapter(adapter);
        });
    }
}
