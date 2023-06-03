package com.example.musicapp.Controller;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
/**
 * @author Norton-Lin
 * @date 2023.6.1
 * @brief 音频焦点控制器
 */
public class AudioFocusController implements AudioManager.OnAudioFocusChangeListener {

    private AudioManager manager;
    private AudioFocusRequest request;
    private AudioAttributes attributes; // 音频属性

    private AudioFocusListener listener;

    public AudioFocusController(Context context) {
        // 获取系统服务
        manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 传递focusChange
     * @param focusChange
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        if (listener != null) {
            listener.onAudioFocusChange(focusChange);
        }
    }

    /**
     * 获取音频焦点
     */
    public void getFocus() {
        // 请求音频焦点
        if (request == null) {
            if (attributes == null) {
                attributes = new AudioAttributes.Builder()
                        // 用途 媒体
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        // 内容类型 音乐
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
            }
            request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();
        }
        // 发起请求，获取结果码
        int result = manager.requestAudioFocus(request);
        if (listener != null)
            listener.onAudioFocusChange(result);
    }

    /**
     * 释放音频焦点
     */
    public void releaseFocus() {
        // 释放音频焦点
        manager.abandonAudioFocusRequest(request);
        if (listener != null)
            listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
    }

    /**
     * 设置监听器
     * @param listener
     */
    public void setListener(AudioFocusListener listener) {
        this.listener = listener;
    }
}
