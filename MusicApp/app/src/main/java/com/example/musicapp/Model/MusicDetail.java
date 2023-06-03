package com.example.musicapp.Model;

import java.util.List;

/**
 * @auther Norton
 * @date 2023.5.28
 * @brief 音乐详情类
 */
public class MusicDetail {
    public List<DataDTO> data;

    public static class DataDTO {
        public Integer id;      //歌曲id
        public String url;      //下载链接
    }
}
