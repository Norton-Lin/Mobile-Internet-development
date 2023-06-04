package com.example.musicapp.Model;

import java.util.List;

public class Song {
    public String name; // 歌曲名
    public Integer id; // 歌曲id

    public List<SingerItem> singer; // 歌手
    public SongItem album;// 专辑信息
    public Integer version;

}
