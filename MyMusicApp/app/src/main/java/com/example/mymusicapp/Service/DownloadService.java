package com.example.mymusicapp.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.mymusicapp.Controller.DownloadController;
import com.example.mymusicapp.R;

public class DownloadService extends Service {
    private DownloadController controller;
    private boolean isStart;

    public DownloadService(){

    }

    private final DownloadController.DownloadListener listener = new DownloadController.DownloadListener() {
        //根据相应的状态构建相应的通知
        @Override
        public void onStart(String path) {
            //开始下载
            isStart = true;
            getNotificationManager().notify(1, buildNotification("开始下载", 0));
        }

        @Override
        public void onError() {
            //下载出错
            isStart = false;
            controller = null;
            stopForeground(true);
            getNotificationManager().notify(1, buildNotification("下载失败", -1));
        }

        @Override
        public void onSuccess() {
            //下载成功
            isStart = false;
            controller = null;
            stopForeground(true);
            getNotificationManager().notify(1, buildNotification("下载成功", -1));
        }

        @Override
        public void onProgress(int position, int percentageProgress) {
            //进度，第二个参数为百分比进度
            //这个进度如果构建通知，回应因更新太快而显示不出来，
            //导致实际下载进度与显示进度不一致，实际进度快
            getNotificationManager().notify(1, buildNotification("下载中，请稍后", percentageProgress));
        }
    };

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    private Notification buildNotification(String title, int progress) {
        //创建通知渠道
        NotificationChannel notificationChannel = new NotificationChannel(
                "1", "下载通知",
                NotificationManager.IMPORTANCE_HIGH);
        getNotificationManager().createNotificationChannel(notificationChannel);
        Notification.Builder builder = new Notification.Builder(this, "1");
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.download));
        builder.setSmallIcon(R.drawable.download);
        builder.setTicker(title);
        builder.setContentTitle(title);
        if (progress > 0) {
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        } else {
            builder.setContentText(title);
        }
        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    public class DownloadBinder extends Binder {
        //开始下载
        //第一个参数 下载链接 第二个参数 文件名字
        public void start(String downloadUrl, String fileName) {
            if (controller == null) {
                controller = new DownloadController(listener);
            } else if (isStart) {
                //正在下载就return，不继续执行
                return;
            }
            controller.execute(downloadUrl, fileName);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消所有通知
        getNotificationManager().cancelAll();
    }
}
