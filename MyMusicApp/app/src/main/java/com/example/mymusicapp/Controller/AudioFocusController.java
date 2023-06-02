package com.example.mymusicapp.Controller;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

import java.util.jar.Attributes;

/**
 * OnAudioFocusChangeListener 是一个接口，它用于监听音频焦点的状态。
 * 当音频焦点发生改变时，将调用此回调。在 Android 中，
 * 两个或多个应用程序可以同时向同一输出流播放音频，系统会将所有内容混合在一起。
 * 为了避免每个音乐应用同时播放，Android 引入了音频焦点的概念。
 */
public class AudioFocusController implements AudioManager.OnAudioFocusChangeListener {
    private AudioManager manager = null;
    private AudioFocusRequest request = null;
    private AudioAttributes attributes = null;
    private AudioManager.OnAudioFocusChangeListener listener;
    public AudioFocusController(Context context){
        //获取系统服务
        manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onAudioFocusChange(int change) {
        //接口传递
        if(listener!=null)
            listener.onAudioFocusChange(change);
    }
    //获取音频焦点
    public void getRequestFocus()
    {
        if(request == null)
        {
            if(attributes == null)
            {
                attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
            }
            //这里要求 API > 26
            request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();
        }
        //获取结果码
        int result = manager.requestAudioFocus(request);
        if(listener != null)
            listener.onAudioFocusChange(result);
    }

    /**
     * 释放音频焦点
     */
    public void releaseFocus(){
        manager.abandonAudioFocusRequest(request);
        if(listener!=null)
            listener.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
    }
    public void setListener(AudioManager.OnAudioFocusChangeListener listener) {
        this.listener = listener;
    }

}
