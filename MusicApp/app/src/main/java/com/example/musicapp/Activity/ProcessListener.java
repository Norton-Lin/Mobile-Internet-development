package com.example.musicapp.Activity;

/**
 * @auther Norton
 * @date 2023.5.28
 * @brief 音乐播放进度监听器
 */
public interface ProcessListener {
    void onStart();

    void onPause();

    void onPreparing();

    void onPrepared();

    void onError();

    void onCompletion();
}
