package com.example.musicapp.Model;

import androidx.annotation.NonNull;

/**
 * @auther Norton
 * @date 2023.5.28
 * @brief 音乐数据类
 */
public class Music {
    private String name; // 歌曲名字
    private String id; // 歌曲id
    private String picUrl; // 歌曲封面链接
    private String singer; // 歌手
    private String musicUrl; // 播放链接

    @NonNull
    @Override
    public String toString() {
        return "MusicData{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", picUrl='" + picUrl + '\'' +
                ", artist='" + singer + '\'' +
                ", playUrl='" + musicUrl + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.musicUrl = "http://music.163.com/song/media/outer/url?id=" + id + ".mp3";
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }
}
