package com.example.mymusicapp.View;

import static com.example.mymusicapp.Util.Utils.getTime;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.mymusicapp.Model.Music;
import com.example.mymusicapp.Controller.*;
import com.example.mymusicapp.Model.MusicDetail;
import com.example.mymusicapp.Network.NetRequest;
import com.example.mymusicapp.R;
import com.example.mymusicapp.Service.DownloadService;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Norton_Lin 2020211472
 * @date 2023.5.28
 * @brief MainActivity
 *        播放器主页面
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar main_bar;// 首页顶层工具栏
    private ImageView album_cover;// 专辑封面
    private TextView song_name;// 歌名
    private TextView singer_name;// 歌手名
    private SeekBar progress_horizontal;
    private TextView cur_time;
    private TextView end_time;
    private ImageButton music_loop;
    private ImageButton pre_music;
    private ImageButton play_music;
    private ImageButton next_music;
    private ImageButton music_list;
    private List<Music> musicList;// 歌曲列表
    private MusicController musicController;// 管理器
    private ObjectAnimator animator;// 动画
    private boolean isAnimator = false;//有无动画
    private Intent intent;// 消息对象
    private ServiceConnection connection;
    private boolean musicPlayingStatus = false;// 音乐播放状态
    private boolean musicPlayingMode = false;// 音乐播放模式（循环、列表）
    private int mode = 0;
    private long time = 0;// 中断时的播放时间
    private DownloadService.DownloadBinder downloadBinder;

    private void initView() {
        this.main_bar = findViewById(R.id.main_bar);
        this.album_cover = findViewById(R.id.album_cover);
        this.song_name = findViewById(R.id.song_name);
        this.singer_name = findViewById(R.id.singer_name);
        this.progress_horizontal = findViewById(R.id.progress_horizontal);
        this.cur_time = findViewById(R.id.cur_time);
        this.end_time = findViewById(R.id.end_time);
        this.music_loop = findViewById(R.id.music_loop);
        this.pre_music = findViewById(R.id.pre_music);
        this.play_music = findViewById(R.id.play_music);
        this.next_music = findViewById(R.id.next_music);
        this.music_list = findViewById(R.id.music_list);

        this.animator = ObjectAnimator.ofFloat(this.album_cover, "rotation", 0f, 360f);
        this.animator.setDuration(10000);
        animator.setRepeatCount(-1); // 无限循环
        animator.setRepeatMode(ObjectAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
    }
    private void initController()
    {
        setSupportActionBar(main_bar);
        musicController.setListener(new MusicController.PlayStateListener() {
            @Override
            public void onStart() {
                musicPlayingMode = true;
                if(isAnimator)
                    animator.resume();
                //todo 切换按钮图案

            }

            @Override
            public void onPause() {
                musicPlayingStatus = false;
                animator.pause();
                //todo 切换按钮图案
            }

            @Override
            public void onPreparing() {
                setView();
            }

            @Override
            public void onPrepared() {
                setDetail();
                if(musicController.getIsFirstPlay())
                    musicController.start();
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "播放失败，可能是VIP歌曲", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompletion() {
                musicController.playNext();
            }
        });
        musicController.initMusicList(musicList);
        progress_horizontal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cur_time.setText(i);//todo 改成时间格式
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(musicPlayingStatus)
                    musicController.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(!musicPlayingStatus)
                {
                    musicController.start();
                    musicController.seekTo(seekBar.getProgress());
                }
            }
        });
        //新开一个进程
        new Thread(() -> {
            while (musicPlayingMode) {
                if (musicPlayingStatus) {
                    //把当前的播放进度设置到seekBar上
                    progress_horizontal.setProgress(musicController.getCurrentPos());
                }
                //歇0.5s
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        this.music_loop.setOnClickListener(this);
        this.pre_music.setOnClickListener(this);
        this.play_music.setOnClickListener(this);
        this.next_music.setOnClickListener(this);
        this.music_list.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置状态栏黑色字体
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //设置xml文件
        setContentView(R.layout.activity_main);
        this.initView();
        //需要一个写存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        else initService();

    }

    /**
     * 下载服务初始化
     */
    public void initService(){
        musicController = MusicController.getInstance(this);
        intent = new Intent(this, DownloadService.class);
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                downloadBinder = (DownloadService.DownloadBinder)iBinder;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
        initMusicList();
        initController();

    }

    public void initMusicList() {
        musicList = new ArrayList<>();
        Music data1 = new Music();
        data1.setId("29719172");
        data1.setSinger("One Direction");
        data1.setPicUrl("http://p2.music.126.net/h97UzOq8a9YzuwjKxZxZOQ==/109951165969533755.jpg");
        data1.setName("18");
        musicList.add(data1);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Log.i("test","test");
        switch (view.getId()){
            case R.id.music_loop:
                musicController.setPlayMode(mode);
                switch (mode){
                    case 0:
                        //todo 切换模式图片
                    case 1:
                        //todo 切换模式图片
                    case 2:
                        //todo 切换模式图片
                }
                //todo模式切换
                break;
            case R.id.pre_music:
                musicController.playPre();
                break;
            case R.id.play_music:
                musicController.click();
                break;
            case R.id.next_music:
                musicController.playNext();
                break;
            case R.id.music_list:
                startActivity(new Intent(this,MusicListActivity.class));
                break;
            default:
                Log.i("test","test");
        }
        Log.i("test","test");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
                //设置所有按钮都不可用
                music_loop.setEnabled(false);
                main_bar.setEnabled(false);
                next_music.setEnabled(false);
                pre_music.setEnabled(false);
                play_music.setEnabled(false);
                music_list.setEnabled(false);
                progress_horizontal.setEnabled(false);
            } else initService();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_main_search:
                //搜索
                startActivity(new Intent(MainActivity.this, MusicSearchActivity.class));
                break;
            case R.id.menu_main_download:
                startDownloadService(musicController.getCurrentMusic());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onStop(){
        super.onStop();
        animator.pause();
        isAnimator = false;
    }

    @Override
    public void onRestart(){
        super.onRestart();
        if(musicPlayingStatus)
            animator.resume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        musicPlayingMode = false;
        musicController.release();
        unbindService(connection);
        stopService(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == event.KEYCODE_BACK)
        {
            if ((System.currentTimeMillis() - time) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            } else {
                this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setView(){
        song_name.setText(musicController.getCurrentMusic().getName());
        singer_name.setText(musicController.getCurrentMusic().getSinger());
        Glide.with(this).load(musicController.getCurrentMusic().getPicUrl())
                .circleCrop()
                .into(album_cover);
        end_time.setText(getTime(0));
        cur_time.setText(getTime(0));
        progress_horizontal.setProgress(0);
        animator.start();
    }

    public void setDetail(){
        end_time.setText(getTime(musicController.getDuration()));
        progress_horizontal.setMax(musicController.getDuration());
    }

    public void startDownloadService(Music music){
        String id = music.getId();
        String filename = music.getName()+"_"+music.getSinger();
        NetRequest request = RetrofitController.getRetrofit().create(NetRequest.class);
        Call<MusicDetail> call = request.getMusicDetail(id, "[" + id + "]", 3200000);
        call.enqueue(new Callback<MusicDetail>() {
            @Override
            public void onResponse(@NonNull Call<MusicDetail> call, @NonNull Response<MusicDetail> response) {
                if (response.code() == HttpsURLConnection.HTTP_OK && response.body() != null) {
                    //如果code==200且响应体不为空
                    //获取到下载链接
                    //传入下载链接和文件名字
                    String downloadUrl = response.body().data.get(0).url;
                    if (downloadUrl != null)
                        downloadBinder.start(downloadUrl, filename);
                    else Toast.makeText(MainActivity.this, "没有获取到下载链接", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(MainActivity.this, "内容为空", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<MusicDetail> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "查询失败，再试试？", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
