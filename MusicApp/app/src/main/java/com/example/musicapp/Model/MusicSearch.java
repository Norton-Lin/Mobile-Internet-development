package com.example.musicapp.Model;

import java.util.List;

/**
 * @auther Norton
 * @date 2023.5.28
 * @brief 音乐搜索类
 * @details 用于搜索音乐
 */
public class MusicSearch {
    public ResultDTO result;

    public static class ResultDTO {
        public List<SongsDTO> songs;

        public static class SongsDTO {
            public String name;             //歌曲名
            public Integer id;              //歌曲id

            public List<ArDTO> ar;          //歌手
            public ResultDTO.SongsDTO.AlDTO al;//专辑信息
            public Integer version;

            public static class AlDTO {
                public Integer id;
                public String name;
                public String picUrl;           //专辑图
            }

            public static class ArDTO {
                public Integer id;
                public String name;
            }
        }
    }
}
