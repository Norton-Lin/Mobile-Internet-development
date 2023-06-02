package com.example.mymusicapp.View;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mymusicapp.Model.Music;
import com.example.mymusicapp.Controller.*;
import com.example.mymusicapp.R;

import java.util.List;

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
    private long time = 0;// 中断时的播放时间

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

            }

            @Override
            public void onPrepared() {

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
                    musicController.onPause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(!musicPlayingStatus)
                {
                    musicController.onStart();
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

        Log.i("info","test");
        //startService(intent);
        //bindService(intent, connection, BIND_AUTO_CREATE);
        this.initController();
        Log.i("info","test");
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
        //todo 启动服务并绑定
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Log.i("test","test");
        switch (view.getId()){
            case R.id.music_loop:
                musicController.setPlayMode(musicPlayingMode);
                switch (musicPlayingMode){
                    case 0:

                    case 1:
                    case 2:
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
}
