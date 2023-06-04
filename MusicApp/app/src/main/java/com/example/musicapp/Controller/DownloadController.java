package com.example.musicapp.Controller;

import android.os.AsyncTask;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.example.musicapp.NetWork.NetworkRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Norton-Lin
 * @date 2023.6.1
 * @brief 下载控制器
 */
public class DownloadController extends AsyncTask<String, Integer, Integer> {
    public static final int DOWNLOAD_STATE_SUCCESS = 0;
    public static final int DOWNLOAD_STATE_FAIL = 1;

    private DownLoadListener downLoadListener;
    File file = null;
    InputStream inputStream = null;
    RandomAccessFile randomAccessFile = null;
    private String downloadUrl = ""; // 下载链接
    private String fileName = ""; // 文件名字
    private String path = ""; // 文件路径
    private int curProgress; // 上次的进度
    private int statusCode = 0; // 状态码
    private long downloadLength = 0; // 上次已经下载的长度

    public DownloadController(@NonNull DownLoadListener listener) {
        this.downLoadListener = listener;
    }

    /**
     * 后台任务 重写
     * 
     * @param strings
     * @return
     */
    @Override
    protected Integer doInBackground(String... strings) {
        downloadUrl = strings[0];
        fileName = strings[1] + ".mp3";
        try {
            // 文件路径为 系统下载文件夹
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory, fileName);
            path = file.getAbsolutePath();
            if (file.exists()) {
                // 如果文件已经存在，获取已经下载的进度
                downloadLength = file.length();
            }
            NetworkRequest request = RetrofitController.getRetrofit().create(NetworkRequest.class);
            Call<ResponseBody> responseBodyCall = request.downloadFile(downloadUrl, downloadLength);
            Response<ResponseBody> response = responseBodyCall.execute();
            f: if (response.code() == HttpURLConnection.HTTP_OK && response.body() != null) {
                // 如果code==200 且响应体不为空
                long contextLength = response.body().contentLength();
                // 获取下载内容的大小
                if (contextLength == 0) {
                    // 没有内容
                    statusCode = DOWNLOAD_STATE_FAIL;
                    break f;
                }
                if (contextLength == downloadLength) {
                    // 已下载的进度等于要下载的进度，返回成功
                    statusCode = DOWNLOAD_STATE_SUCCESS;
                    break f;
                }
                // 获取输入流
                inputStream = response.body().byteStream();
                try {
                    downLoadListener.onStart(path);
                    // 断点续传
                    randomAccessFile = new RandomAccessFile(file, "rw");
                    // 缓冲区长度1024byte
                    byte[] bytes = new byte[1024];
                    int readLength;
                    while ((readLength = inputStream.read(bytes)) != -1) {
                        // 写入文件
                        randomAccessFile.write(bytes, 0, readLength);
                    }
                    statusCode = DOWNLOAD_STATE_SUCCESS;
                } catch (Exception e) {
                    e.printStackTrace();
                    statusCode = DOWNLOAD_STATE_FAIL;
                }
            } else
                statusCode = DOWNLOAD_STATE_FAIL;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (randomAccessFile != null)
                    randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return statusCode;
    }

    /**
     * 进度更新函数
     * 
     * @param values
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (values[0] > curProgress && values[1] < 100) {
            // 第一个参数当前位置，多少kb，第二参数百分比
            downLoadListener.onProgress(values[0], values[1]);
            curProgress = values[0];
        }
    }

    /**
     * 下载结果回调重写
     * 
     * @param integer
     */
    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        switch (integer) {
            case DOWNLOAD_STATE_FAIL:
                downLoadListener.onError();
                break;
            case DOWNLOAD_STATE_SUCCESS:
                downLoadListener.onSuccess(path);
                break;
        }
    }
}
