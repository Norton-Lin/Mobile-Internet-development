package com.example.mymusicapp.Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;

import com.example.mymusicapp.Model.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Norton_Lin
 * @date 2023.5.23
 * @brief 音乐播放控制类
 */
public class MusicController {
    private int playMode = 0;//播放模式,单曲、列表、随机
    private List<Music> musicList = new ArrayList<>();//播放列表
    private int index = 0;
    private Context context;
    private MediaPlayer mediaPlayer;
    boolean prepare = false;//播放准备状态
    boolean isplaying = false;//是否正在播放

    @SuppressLint("StaticFieldLeak")
    private static MusicController controller;
    private PlayStateListener listener;
    AudioFocusController audioFocusController;
    public interface PlayStateListener {
        void onStart();
        void onPause();
        void onPreparing();
        void onPrepared();
        void onError();
        void onCompletion();
    }
    public MusicController(Context context)
    {
        this.context = context;
        audioFocusController = new AudioFocusController(context);
    }

    /**
     * 获取实例
     * @param context 上下文对象
     * @return MusicController对象
     */
    public static MusicController getInstance(Context context){
        controller = new MusicController(context);
        return controller;
    }

    public static MusicController getController(){
        return controller;
    }


    /**
     * 播放状态初始化
     */
    public void init()
    {
        //媒体播放器设置音频属性
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        playMode = 1;//默认列表
        //监听器设置
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            prepare = true;
            if(listener != null){
                listener.onPrepared();
            }
        });
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if(listener != null)
                listener.onCompletion();
        });
        mediaPlayer.setOnErrorListener((mediaPlayer,i ,i1) -> {
            prepare = false;
            isplaying = false;
            if(listener != null){
                listener.onError();
                listener.onPause();
            }
            return true;
        });

    }

    //创建歌单
    public void initMusicList(@NonNull List<Music> list){
        musicList.clear();
        //musicList.addAll(list);
        index = 0;
        mediaPlayer = new MediaPlayer();
        //todo 设置数据源
        init();
    }
    //开始播放
    public void onStart(){
        if(prepare && !isplaying){
            audioFocusController.releaseFocus();
        }
    }
    //暂停
    public void onPause(){
        if(prepare && isplaying){
            audioFocusController.releaseFocus();
        }
    }
    //播放键操作
    public void click(){
        if(isplaying)
            onPause();
        else
            onStart();
    }
    //设置播放源
    public void setMusicSource(int index){
        mediaPlayer.reset();
        prepare = false;
        isplaying = false;
        onPause();
        if(listener != null){
            listener.onPreparing();
            listener.onPause();
        }
        try {
            mediaPlayer.setDataSource(musicList.get(index).getPlayUrl());
            mediaPlayer.prepareAsync();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void playNext()
    {
        onPause();
        //多首歌可以跳
        if(musicList.size()>1)
        {
            switch(playMode){
                case 1:
                    index = index == musicList.size()-1?0:index+1;
                    break;
                case 2:
                    index = new Random().nextInt(musicList.size());
                    break;
            }
        }
        //todo 设置播放状态
        setMusicSource(index);
    }

    public void playPre()
    {
        onPause();
        //多首歌可以跳
        if(musicList.size()>1)
        {
            switch(playMode){
                case 1:
                    index = index == 0?musicList.size()-1:index-1;
                    break;
                case 2:
                    index = new Random().nextInt(musicList.size());
                    break;
            }
        }
        //todo 设置播放状态
        setMusicSource(index);
    }

    /**
     * 音乐播放进度条跳转
     * @param index
     */
    public void seekTo(int index)
    {
        if(prepare)
            mediaPlayer.seekTo(index);
    }

    /**
     * 添加歌曲
     * @param music
     */
    public void addMusic(Music music){
        if(musicList.contains(music))
            return ;
        onPause();
        ++index;
        musicList.add(index,music);
        setMusicSource(index);
    }

    /**
     * 删除歌曲
     * @param index
     */
    public void deleteMusic(int index){
        if(this.index == index)
        {
            onPause();
            playNext();
            musicList.remove(index);
            --this.index;
        }
        else if(this.index > index)
        {
            --this.index;
            musicList.remove(index);
        }
        else {
            musicList.remove(index);
        }
    }

    /**
     * 播放列表里的指定歌曲
     * @param index
     */
    public void playPos(int index){
        if(index!=this.index)
        {
            onPause();
            this.index = index;
            setMusicSource(index);
        }
    }

    /**
     * 获取歌曲列表
     * @return
     */
    public List<Music> getMusicList(){
        return musicList;
    }

    /**
     * 获取歌曲时长
     * @return
     */
    public int getDuration(){
        if (prepare)
            return mediaPlayer.getDuration();
        return 0;
    }

    /**
     * 获取歌曲当前播放进度
     * @return
     */
    public int getCurrentPos(){
        if(isplaying)
            return mediaPlayer.getCurrentPosition();
        return 0;
    }

    /**
     * 获取播放模式
     * @return
     */
    public int getPlayMode(){
        return this.playMode;
    }

    /**
     * 设置播放模式
     * @param playMode
     */
    public void setPlayMode(int playMode){
        this.playMode = playMode;
    }

    /**
     * 设置监听器
     * @param listener
     */
    public void setListener(PlayStateListener listener){
        this.listener = listener;
    }

}
