package com.example.musicapp.Controller;

/**
 * @author Norton-Lin
 * @date 2023.6.2
 * @brief 下载监听接口 新增中断续传
 */
public interface DownLoadListener {
    void onStart(String path);

    void onError();

    void onSuccess(String path);

    void onProgress(int position, int percentageProgress);
}
