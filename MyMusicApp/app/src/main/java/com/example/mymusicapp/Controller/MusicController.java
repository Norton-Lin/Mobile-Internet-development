package com.example.mymusicapp.Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

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
    private int index = -1;
    private final Context context;
    private MediaPlayer mediaPlayer;
    boolean isPrepared = false;//播放准备状态
    boolean isPlaying = false;//是否正在播放

    boolean isFirstPlay = false; //首次播放

    @SuppressLint("StaticFieldLeak")
    private static MusicController controller;
    private PlayStateListener listener;
    private AudioFocusController audioFocusController;
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
            isPrepared = true;
            if(listener != null){
                listener.onPrepared();
            }
        });
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if(listener != null)
                listener.onCompletion();
        });
        mediaPlayer.setOnErrorListener((mediaPlayer,i ,i1) -> {
            isPrepared = false;
            isPlaying = false;
            if(listener != null){
                listener.onError();
                listener.onPause();
            }
            return true;
        });
        audioFocusController.setListener(focus ->{
            switch(focus){
                case AudioManager.AUDIOFOCUS_LOSS://永久失去音频焦点，不会再获取到焦点
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://暂时失去音频焦点
                    //暂停播放
                    isPlaying = false;
                    mediaPlayer.pause();
                    if (listener != null)
                        listener.onPause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK://与其他应用共焦点
                    Toast.makeText(context, "与其他应用共享音频焦点", Toast.LENGTH_SHORT).show();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN://获取到焦点
                    //开始播放
                    isPlaying = true;
                    mediaPlayer.start();
                    if (listener != null) {
                        listener.onStart();
                    }
                    break;
            }
        });

    }

    //创建歌单
    public void initMusicList(@NonNull List<Music> list){
        musicList.clear();
        musicList.addAll(list);
        index = 0;
        mediaPlayer = new MediaPlayer();
        //todo 设置数据源
        init();
    }
    //开始播放
    public void start(){
        if(isPrepared && !isPlaying){
            audioFocusController.releaseFocus();
        }
    }
    //暂停
    public void pause(){
        if(isPrepared && isPlaying){
            audioFocusController.releaseFocus();
        }
    }
    //播放键操作
    public void click(){
        if(isPlaying)
            pause();
        else
            start();
    }
    //设置播放源
    public void setMusicSource(int index){
        mediaPlayer.reset();
        isPrepared = false;
        isPlaying = false;
        pause();
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
        pause();
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
        isFirstPlay = true;
        setMusicSource(index);
    }

    public void playPre()
    {
        pause();
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
        isFirstPlay = true;
        setMusicSource(index);
    }

    /**
     * 音乐播放进度条跳转
     * @param index
     */
    public void seekTo(int index)
    {
        if(isPrepared)
            mediaPlayer.seekTo(index);
    }

    /**
     * 添加歌曲
     * @param music
     */
    public void addMusic(Music music){
        if(musicList.contains(music))
            return ;
        pause();
        ++index;
        isFirstPlay = true;
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
            pause();
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
            pause();
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
     * 获取当前播放歌曲
     */
    public Music getCurrentMusic(){

        Log.d("mediaPlayerManager", musicList.get(index).toString());
        return musicList.get(index);
    }

    /**
     * 获取歌曲时长
     * @return
     */
    public int getDuration(){
        if (isPrepared)
            return mediaPlayer.getDuration();
        return 0;
    }

    /**
     * 获取歌曲当前播放进度
     * @return
     */
    public int getCurrentPos(){
        if(isPlaying)
            return mediaPlayer.getCurrentPosition();
        return 0;
    }

    /**
     * 是否是第一次播放
     * @return
     */
    public boolean getIsFirstPlay(){
        return isFirstPlay;
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

    /**
     * 释放资源
     */
    public void release(){
        if(mediaPlayer != null)
        {
            if(isPlaying)
                audioFocusController.releaseFocus();
            controller.release();
        }
        controller = null;
    }

}
