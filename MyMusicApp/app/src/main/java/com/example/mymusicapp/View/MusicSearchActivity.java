package com.example.mymusicapp.View;

import static com.example.mymusicapp.Util.Utils.getUrl;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymusicapp.Adapter.RecycleViewAdapter;
import com.example.mymusicapp.Controller.MusicController;
import com.example.mymusicapp.Controller.RetrofitController;
import com.example.mymusicapp.Model.Music;
import com.example.mymusicapp.Model.MusicResult;
import com.example.mymusicapp.Network.NetRequest;
import com.example.mymusicapp.R;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MusicSearchActivity extends AppCompatActivity implements View.OnClickListener{
    Toolbar searchBar;
    RecyclerView resultList;

    MusicController controller;
    List<Music> musicList;
    RecycleViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_search);
        init();
    }

    private void init() {
        controller = MusicController.getController();
        musicList = new ArrayList<>();
        adapter = new RecycleViewAdapter();
        initView();
    }

    private void initView() {
        searchBar = findViewById(R.id.search_bar);
        resultList = findViewById(R.id.result_list);
        resultList.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(searchBar);

        adapter.setClickListener(new RecycleViewAdapter.OnItemViewClickListener() {
            @Override
            public void onClick(int position) {
                //单击播放，并结束此活动
                controller.addMusic(musicList.get(position));
                MusicSearchActivity.this.finish();
            }

            @Override
            public void onLongClick(int position) {

            }
        });
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //加载menu，加载searchView
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();    //收起键盘
                searchView.setQuery("", false);
                searchView.onActionViewCollapsed(); //收起搜索框
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
        NetRequest request = RetrofitController.getRetrofit().create(NetRequest.class);
        Call<MusicResult> searchResult = request.getSearchResult(getUrl(query, 0));
        searchResult.enqueue(new retrofit2.Callback<MusicResult>() {
            @Override
            public void onResponse(@NonNull Call<MusicResult> call, @NonNull Response<MusicResult> response) {
                if (response.code() == HttpURLConnection.HTTP_OK && response.body() != null) {
                    //如果code==200 且响应体不为空
                    MusicResult result = response.body();
                    musicList.clear();
                    for (int i = 0; i < result.result.songs.size(); i++) {
                        //处理结果，将MusicSearchResult类中的有用信息封装成MusicData类
                        Music data = new Music();
                        data.setId(String.valueOf(result.result.songs.get(i).id));
                        data.setName(result.result.songs.get(i).name);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < result.result.songs.get(i).ar.size(); j++) {
                            //拼接歌手名字，中间 / 分开
                            stringBuilder.append(result.result.songs.get(i).ar.get(j).name);
                            stringBuilder.append("/");
                        }
                        //删去最后的 / 符号
                        String artist = stringBuilder.substring(0, stringBuilder.length() - 1);
                        data.setSinger(artist);
                        data.setPicUrl(result.result.songs.get(i).al.picUrl);
                        musicList.add(data);
                    }
                    //设置列表数据
                    setData(musicList);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MusicResult> call, @NonNull Throwable t) {
                Toast.makeText(MusicSearchActivity.this, "error\n请重试", Toast.LENGTH_SHORT).show();
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
            resultList.setAdapter(adapter);
        });
    }
}
