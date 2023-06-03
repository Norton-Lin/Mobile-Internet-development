package com.example.musicapp.Controller;

public interface DownLoadListener {
    void onStart(String path);

    void onError();

    void onSuccess(String path);

    void onProgress(int position, int percentageProgress);
}
