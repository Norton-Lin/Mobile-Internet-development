package com.example.mymusicapp.Controller;

import android.os.AsyncTask;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.example.mymusicapp.Network.NetRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class DownloadController extends AsyncTask<String, Integer, Integer> {
    private File file =null;
    private InputStream inputStream = null;
    private RandomAccessFile randomAccessFile = null;
    private String downloadUrl = "";//下载链接
    private String filename = "";//文件名
    private String filePath = "";//文件路径
    private int statusCode = 0;//http状态码
    private int downloadProgress = 0;//下载进度
    private long downLoadLength = 0;//下载长度
    public interface DownloadListener {
        void onStart(String path);
        void onError();
        void onSuccess();
        void onProgress(int position, int progress);
    }
    private DownloadListener listener;
    public DownloadController(@NonNull DownloadListener listener){
        this.listener = listener;
    }
    @Override
    protected Integer doInBackground(String... strings) {
        downloadUrl = strings[0];
        filename = strings[1] + ".mp3";
        try{
            //文件路径为 系统下载文件夹
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory, filename);
            filePath = file.getAbsolutePath();
            if (file.exists()) {
                //如果文件已经存在，获取已经下载的进度
                downLoadLength = file.length();
            }
            NetRequest request = RetrofitController.getRetrofit().create(NetRequest.class);
            Call<ResponseBody> responseBodyCall = request.downloadFile(downloadUrl, downLoadLength);
            Response<ResponseBody> response = responseBodyCall.execute();
            label:
            if (response.code() == HttpURLConnection.HTTP_OK && response.body() != null) {
                //如果code==200 且响应体不为空
                long contextLength = response.body().contentLength();
                //获取下载内容的大小
                if (contextLength == 0) {
                    //没有内容
                    statusCode = 1;
                    break label;
                }
                if (contextLength == downLoadLength) {
                    //已下载的进度等于要下载的进度，返回成功
                    statusCode = 0;
                    break label;
                }
                //获取输入流
                inputStream = response.body().byteStream();
                try {
                    listener.onStart(filePath);
                    //断点续传
                    randomAccessFile = new RandomAccessFile(file, "rw");
                    //缓冲区长度1024byte
                    byte[] bytes = new byte[1024];
                    int readLength;
                    while ((readLength = inputStream.read(bytes)) != -1) {
                        //写入文件
                        randomAccessFile.write(bytes, 0, readLength);
                    }
                    statusCode = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    statusCode = 1;
                }
            } else statusCode = 1;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
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
}
