package com.example.musicapp.Activity;

import static com.example.musicapp.Util.Utils.timeTransform;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.musicapp.Controller.MusicController;
import com.example.musicapp.Controller.RetrofitController;
import com.example.musicapp.Dao.Database;
import com.example.musicapp.Model.Music;
import com.example.musicapp.Model.MusicList;
import com.example.musicapp.NetWork.NetworkRequest;
import com.example.musicapp.R;
import com.example.musicapp.Service.DownloadService;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Norton-Lin
 * @date 2023.5.28
 * @brief 主窗口界面
 */
public class MainActivity extends BaseActivity {
    private Toolbar main_bar; // 工具栏
    private ImageView album_cover; // 专辑图片
    private TextView song_name; // 歌曲名字
    private TextView singer; // 歌手名字
    private SeekBar seek_bar; // 拖动条
    private TextView cur_time; // 当前播放进度显示
    private TextView end_time; // 总时间显示
    private ImageButton play_mode; // 循环模式按钮
    private ImageButton play_pre; // 下一首按钮
    private ImageButton play_button; // 开始或暂停按钮
    private ImageButton play_next; // 下一首按钮
    private ImageButton music_list; // 歌曲列表按钮

    private List<Music> musicList; // 歌曲列表
    private MusicController controller; // 管理者
    private ObjectAnimator animator; // 动画
    private Intent intent;
    private ServiceConnection connection;
    private DownloadService.DownloadBinder downloadBinder;

    private boolean isPlaying = false; // 是否正在播放
    private boolean isCircling = true; // 是否循环
    private boolean isAnimation = true; // 是否需要开启动画
    private long lastActionTime = 0; // 上次按下返回的时间
    private int playMode = MusicController.LIST_MUSIC_LOOP;// 循环模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVIew();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        } else
            init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show();
                // 设置所有按钮都不可用
                seek_bar.setEnabled(false);
                main_bar.setEnabled(false);
                play_next.setEnabled(false);
                play_pre.setEnabled(false);
                play_mode.setEnabled(false);
                music_list.setEnabled(false);
                play_button.setEnabled(false);
            } else
                init();
        }
    }

    /**
     * 基本信息初始化
     */
    private void init() {
        controller = MusicController.getInstance(this);
        // 启动活动并绑定
        intent = new Intent(this, DownloadService.class);
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                downloadBinder = (DownloadService.DownloadBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
        initMusicList();
        initListener();
    }

    /**
     * 界面信息绑定
     */
    private void initVIew() {
        main_bar = findViewById(R.id.main_bar);
        album_cover = findViewById(R.id.album_cover);
        song_name = findViewById(R.id.song_name);
        singer = findViewById(R.id.singer);
        seek_bar = findViewById(R.id.seek_bar);
        cur_time = findViewById(R.id.cur_time);
        end_time = findViewById(R.id.end_time);
        play_mode = findViewById(R.id.play_mode);
        play_pre = findViewById(R.id.play_pre);
        play_button = findViewById(R.id.play_button);
        play_next = findViewById(R.id.play_next);
        music_list = findViewById(R.id.music_list);

        animator = ObjectAnimator.ofFloat(album_cover, "rotation", 0.0f, 360.0f);
        animator.setDuration(16 * 1000);
        animator.setRepeatCount(Animation.INFINITE); // 无限循环
        animator.setRepeatMode(ObjectAnimator.RESTART); // 循环模式
        animator.setInterpolator(new LinearInterpolator()); // 匀速
    }

    /**
     * 初始化音乐列表
     */
    private void initMusicList() {

        // 默认歌曲
        musicList = new ArrayList<>();
        try {
            Database database = new Database(this);
            database.initMusicList(this.musicList);
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化监听器
     */
    private void initListener() {
        setSupportActionBar(main_bar);
        controller.setMode(playMode);
        controller.setProcessListener(new ProcessListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onStart() {
                // 开始播放
                isPlaying = true;
                if (isAnimation)
                    animator.resume();
                play_button.setBackground(getDrawable(R.drawable.pause));
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onPause() {
                // 暂停
                isPlaying = false;
                animator.pause();
                play_button.setBackground(getDrawable(R.drawable.start));
            }

            @Override
            public void onPreparing() {
                // 准备中
                setBasicInfo();
            }

            @Override
            public void onPrepared() {
                // 准备好
                setReinforceInfo();
                if (controller.isNotFirstPlay())
                    controller.start();
            }

            @Override
            public void onError() {
                // 出错
                Toast.makeText(MainActivity.this, "播放失败，可能是VIP歌曲", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCompletion() {
                // 播放完毕
                controller.playNext(playMode);
            }
        });
        controller.init(musicList);

        seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 显示拖动的位置
                cur_time.setText(timeTransform(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 触摸到，暂停播放
                if (isPlaying)
                    controller.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 松开，开始播放
                if (!isPlaying) {
                    controller.start();
                    controller.seekTo(seekBar.getProgress());
                }
            }
        });

        new Thread(() -> {
            while (isCircling) {
                if (isPlaying) {
                    // 把当前的播放进度设置到seekBar上
                    seek_bar.setProgress(controller.getCurrentPosition());
                }
                // 歇0.5s
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        play_button.setOnClickListener(this);
        play_next.setOnClickListener(this);
        play_pre.setOnClickListener(this);
        play_mode.setOnClickListener(this);
        music_list.setOnClickListener(this);
    }

    @SuppressLint({ "NonConstantResourceId", "UseCompatLoadingForDrawables" })
    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.music_list: {
                // 歌单列表，打开新的活动
                startActivity(new Intent(this, MusicListActivity.class));
            }
                break;
            case R.id.play_button: {
                // 开始或暂停按钮
                controller.click();
            }
                break;
            case R.id.play_next: {
                // 下一首按钮
                controller.playNext(playMode);
            }
                break;
            case R.id.play_pre: {
                // 上一首按钮
                controller.playPre();
            }
                break;
            case R.id.play_mode: {
                // 播放模式按钮
                // 现根据当前模式设置点击后的图片和变更后的模式
                // 最后在将playMode和播放器的mode同步
                switch (playMode) {
                    case MusicController.LIST_MUSIC_LOOP:
                        controller.setMode(MusicController.SINGLE_MUSIC_LOOP);
                        play_mode.setBackground(getDrawable(R.drawable.signle_loop));
                        break;
                    case MusicController.SINGLE_MUSIC_LOOP:
                        controller.setMode(MusicController.RANDOM_MUSIC);
                        play_mode.setBackground(getDrawable(R.drawable.random));
                        break;
                    case MusicController.RANDOM_MUSIC:
                        controller.setMode(MusicController.LIST_MUSIC_LOOP);
                        play_mode.setBackground(getDrawable(R.drawable.list_loop));
                        break;
                }
                playMode = controller.getMode();
            }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 记载选项菜单
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_search:
                // 搜索按钮
                // 打开查询活动
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                break;
            case R.id.menu_main_download:
                // 下载按钮
                // 开启下载服务
                Music music = controller.getCurrentMusic();
                try {
                    Database database = new Database(this);
                    database.addMusic(music);
                    database.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startDownLoadService(music);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 暂停动画
        // 需要动画=false
        animator.pause();
        isAnimation = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // 如果正在播放就开始动画
        isAnimation = true;
        if (isPlaying)
            animator.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放资源
        isCircling = false;
        controller.release();
        unbindService(connection);
        stopService(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - lastActionTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                lastActionTime = System.currentTimeMillis();
            } else {
                this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置播放基本信息
     */
    private void setBasicInfo() {
        song_name.setText(controller.getCurrentMusic().getName());
        singer.setText(controller.getCurrentMusic().getSinger());
        Glide.with(this)
                .load(controller.getCurrentMusic().getPicUrl())
                .circleCrop()
                .into(album_cover);
        end_time.setText(timeTransform(0));
        cur_time.setText(timeTransform(0));
        seek_bar.setProgress(0);
        animator.start();
    }

    /**
     * 设置播放补充信息（歌曲时长）
     */
    private void setReinforceInfo() {
        end_time.setText(timeTransform(controller.getDuration()));
        seek_bar.setMax(controller.getDuration());
    }

    /**
     * 启动下载服务
     * @param music
     */
    private void startDownLoadService(Music music) {
        // 开启下载服务
        String id = music.getId(); // 歌曲id
        String fileName = music.getName() + "-" + music.getSinger(); // 用歌曲名和歌手拼接成文件名
        NetworkRequest request = RetrofitController.getRetrofit().create(NetworkRequest.class);
        Call<MusicList> call = request.getMusicDetail(id, "[" + id + "]", 3200000);
        call.enqueue(new Callback<MusicList>() {
            @Override
            public void onResponse(@NonNull Call<MusicList> call, @NonNull Response<MusicList> response) {
                if (response.code() == HttpsURLConnection.HTTP_OK && response.body() != null) {
                    // 如果code==200且响应体不为空
                    // 获取到下载链接
                    // 传入下载链接和文件名字
                    String downloadUrl = response.body().data.get(0).url;
                    if (downloadUrl != null)
                        downloadBinder.start(downloadUrl, fileName);
                    else
                        Toast.makeText(MainActivity.this, "没有获取到下载链接", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, "内容为空", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<MusicList> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "查询失败，再试试？", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
