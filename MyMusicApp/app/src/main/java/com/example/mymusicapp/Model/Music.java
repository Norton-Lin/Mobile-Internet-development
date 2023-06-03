package com.example.mymusicapp.Model;

import androidx.annotation.NonNull;

public class Music {
    private String id;//歌曲id
    private String name;//歌曲名称
    private String singer;//歌手
    private String playUrl;//播放链接URL
    private String picUrl;//专辑封面照片URL
    @NonNull
    @Override
    public String toString() {
        return "MusicData{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", picUrl='" + picUrl + '\'' +
                ", artist='" + singer + '\'' +
                ", playUrl='" + playUrl + '\'' +
                '}';
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.playUrl = "http://music.163.com/song/media/outer/url?id=" + id + ".mp3";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
