package com.example.musicapp.Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.musicapp.Activity.ProcessListener;
import com.example.musicapp.Model.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * @author Norton-Lin
 * @date 2023.6.1
 * @brief 音乐播放控制器
 */
public class MusicController {

    public static final int SINGLE_MUSIC_LOOP = 1; // 单曲循环
    public static final int LIST_MUSIC_LOOP = 2; // 列表循环
    public static final int RANDOM_MUSIC = 3; // 随机播放

    private int mode;
    private final List<Music> musicList = new ArrayList<>();
    private int index = -1;
    private boolean prepared;
    private boolean notFirstPlay = false;
    private boolean isPlaying = false;
    private final Context context;

    @SuppressLint("StaticFieldLeak")
    private static MusicController controller;
    private MediaPlayer mediaPlayer;
    private ProcessListener listener;
    private final AudioFocusController audioFocusController;

    public MusicController(Context context) {
        this.context = context;
        audioFocusController = new AudioFocusController(context);
    }

    public static MusicController getInstance(Context context) {
        controller = new MusicController(context);
        return controller;
    }

    public static MusicController getController() {
        return controller;
    }

    /**
     * 歌单初始化
     * @param list
     */
    public void init(@NonNull List<Music> list) {
        musicList.clear();
        musicList.addAll(list);
        index = 0;
        mediaPlayer = new MediaPlayer();
        setDataSource(index);
        init();
    }

    /**
     * 开始播放
     */
    public void start() {
        if (prepared && !isPlaying) {
            audioFocusController.getFocus();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (prepared && isPlaying) {
            audioFocusController.releaseFocus();
        }
    }

    /**
     * 播放按钮触发
     */
    public void click() {
        if (isPlaying)
            pause();
        else
            start();
    }

    /**
     * 设置播放源
     * @param position
     */
    private void setDataSource(int position) {
        mediaPlayer.reset(); // 必须重置
        prepared = false;
        isPlaying = false;
        pause();
        if (listener != null) {
            listener.onPreparing();
            listener.onPause();
        }
        try {
            mediaPlayer.setDataSource(musicList.get(position).getMusicUrl());
            mediaPlayer.prepareAsync();// 异步准备
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放下一首
     * @param mode
     */
    public void playNext(int mode) {
        pause();
        if (musicList.size() > 1)
            // 根据播放模式计算index的值
            switch (mode) {
                case SINGLE_MUSIC_LOOP:
                    break;
                case LIST_MUSIC_LOOP:
                    if (index == musicList.size() - 1) {
                        index = 0;
                    } else
                        index++;
                    break;
                case RANDOM_MUSIC:
                    index = new Random().nextInt(musicList.size() - 1);
                    break;
            }
        notFirstPlay = true;
        setDataSource(index);
    }

    /**
     * 播放上一首
     */
    public void playPre() {
        pause();
        if (musicList.size() > 1)
            // 根据播放模式计算index的值
            switch (mode) {
                case SINGLE_MUSIC_LOOP:
                    break;
                case LIST_MUSIC_LOOP:
                    if (index == 0)
                        index = musicList.size() - 1;
                    else
                        index--;
                    break;
                case RANDOM_MUSIC:
                    index = new Random().nextInt(musicList.size() - 1);
                    break;
            }
        notFirstPlay = true;
        setDataSource(index);
    }

    /**
     * 指定播放
     * @param position
     */
    public void seekTo(int position) {
        if (prepared) {
            mediaPlayer.seekTo(position);
        }
    }

    /**
     * 添加歌曲
     * @param music
     */
    public void addMusic(Music music) {
        if (!musicList.contains(music)) {
            pause();
            index++;
            notFirstPlay = true;
            musicList.add(index, music);
            setDataSource(index);
        }
    }

    /**
     * 删除歌曲
     * @param position
     */
    public void removeMusic(int position) {
        if (position == index) {
            // 如果删除当前播放的歌曲，顺序播放下一首歌
            pause();
            playNext(LIST_MUSIC_LOOP);
            musicList.remove(position);
            index--;
            return;
        }
        if (position < index) {
            // 先删除的歌位置靠上 index--
            index--;
            musicList.remove(position);
            return;
        }
        musicList.remove(position);
    }

    /**
     * 设置播放歌曲位置
     * @param position
     */
    public void setPlayPos(int position) {
        if (position != index) {
            pause();
            index = position;
            setDataSource(position);
        }
    }

    /**
     * 获取所有歌曲
     * @return
     */
    public List<Music> getAllMusic() {
        return musicList;
    }

    /**
     * 获取当前播放歌曲
     * @return
     */
    public Music getCurrentMusic() {
        Log.d("mediaPlayerManager", musicList.get(index).toString());
        return musicList.get(index);
    }

    /**
     * 获取歌曲时长 ms
     * @return
     */
    public int getDuration() {
        if (prepared) {
            return mediaPlayer.getDuration();
        } else
            return 0;
    }

    /**
     * 获取当前播放位置
     * @return
     */
    public int getCurrentPosition() {
        if (isPlaying) {
            return mediaPlayer.getCurrentPosition();
        } else
            return 0;
    }

    /**
     * 是否首次播放，用于初始化问题
     * @return
     */
    public boolean isNotFirstPlay() {
        return notFirstPlay;
    }

    /**
     * 设置播放模式
     * @param mode
     */
    public void setMode(int mode) {
        if (mode<=3&&mode>=1) {
            this.mode = mode;
        }
    }

    /**
     * 获取播放模式
     * @return
     */
    public int getMode() {
        return mode;
    }

    /**
     * 释放媒体资源
     */
    public void release() {
        if (mediaPlayer != null) {
            if (isPlaying)
                audioFocusController.releaseFocus();
            mediaPlayer.release();
        }
        mediaPlayer = null;
    }

    /**
     * 设置监听器
     * @param listener
     */
    public void setProcessListener(ProcessListener listener) {
        this.listener = listener;
    }

    private void init() {
        // 初始化
        // 设置音频睡醒
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        // 默认播放模式为列表循环
        mode = LIST_MUSIC_LOOP;
        // 设置监听器
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            prepared = true;
            if (listener != null) {
                listener.onPrepared();
            }
        });
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (listener != null) {
                listener.onCompletion();
            }
        });
        mediaPlayer.setOnErrorListener((mediaPlayer, i, i1) -> {
            isPlaying = false;
            prepared = false;
            if (listener != null) {
                listener.onError();
                listener.onPause();
            }
            // 返回为true则不会继续执行 completion中的代码
            // 如果false则会继续执行
            return true;
        });

        audioFocusController.setListener(focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:// 永久失去音频焦点，不会再获取到焦点
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:// 暂时失去音频焦点
                    // 暂停播放
                    isPlaying = false;
                    mediaPlayer.pause();
                    if (listener != null)
                        listener.onPause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:// 与其他应用共焦点
                    Toast.makeText(context, "与其他应用共享音频焦点", Toast.LENGTH_SHORT).show();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:// 获取到焦点
                    // 开始播放
                    isPlaying = true;
                    mediaPlayer.start();
                    if (listener != null) {
                        listener.onStart();
                    }
                    break;
            }
        });
    }
}
