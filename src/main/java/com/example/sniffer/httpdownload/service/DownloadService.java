package com.example.sniffer.httpdownload.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.dao.FileDownloadInfoDao;
import com.example.sniffer.httpdownload.download.VideoFileDownload;
import com.example.sniffer.httpdownload.utils.Key;
import com.example.sniffer.httpdownload.utils.SendBroadcestRevice;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载服务
 */
public class DownloadService extends Service {
    private File downloadPath;
    private ThreadPoolExecutor Downloadexecutor;
    private Map<String, VideoFileDownload> tasks;
    private UpdataReceiver receiver;
    private FileDownloadInfoDao infodao;
    private NotificationManager nManager;
    public IBinder serviceBinder = new DownloadServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("生命周期", "Service——onBind");
        return serviceBinder;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("生命周期", "Service——onCreate");
        tasks = new HashMap();
        Downloadexecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        infodao = new FileDownloadInfoDao(this);
        //注册广播
        receiver = new UpdataReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Key.ACTION_DOWNLOAD_STATE);
        intentfilter.addAction(Key.ACTION_DOWNLOAD_DELETE);
        intentfilter.addAction(Key.ACTION_DOWNLOAD_CLOSE);
        registerReceiver(receiver, intentfilter);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            downloadPath = new File(Environment.getExternalStorageDirectory() + "/VideoDownload/");
        } else {
            Toast.makeText(DownloadService.this, "内存卡不可用！", Toast.LENGTH_SHORT);
            stopSelf();
        }
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("开启下载")
                .setContentText("下载...")
                .setOngoing(true)
                .setSmallIcon(R.mipmap.icon_download)
                .build();
        nManager.notify(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("生命周期", "Service——onStartCommand");
        if (intent != null) {
            VideoDownInfo videoinfo = (VideoDownInfo) intent.getSerializableExtra("videoinfo");
            if (Key.ACTION_DOWNLOAD_START.equals(intent.getAction())) {
                VideoFileDownload task = new VideoFileDownload(DownloadService.this, videoinfo,
                        downloadPath, Key.DOWNLOAD_THREAD_COUNT);
                if (getTask(videoinfo)) {
                    tasks.put(videoinfo.getVideoMp4Url(), task);
                }
                Downloadexecutor.execute(task);
                if (tasks.size() > 3) {
                    SendBroadcestRevice.sendRecice(DownloadService.this, videoinfo,
                            Key.DOWNLOAD_STATE_WAIT, videoinfo.getVideoProgress());
                }
            }
        } else {
            //恢复下载任务继续下载
            List<VideoDownInfo> videoDownInfos = infodao.getDownloadTaskAll();
            Log.e("恢复", "恢复任务");
            for (VideoDownInfo videoDownInfo : videoDownInfos) {
                int state = videoDownInfo.getState();
                if (0 == state || 4 == state) {
                    VideoFileDownload task = new VideoFileDownload(this, videoDownInfo, downloadPath,
                            Key.DOWNLOAD_THREAD_COUNT);
                    tasks.put(videoDownInfo.getVideoMp4Url(), task);
                    Downloadexecutor.execute(task);
                }
            }
            //恢复等待下载的任务
            for (VideoDownInfo videoDownInfo : videoDownInfos) {
                int state = videoDownInfo.getState();
                if (3 == state) {
                    VideoFileDownload task = new VideoFileDownload(this, videoDownInfo, downloadPath,
                            Key.DOWNLOAD_THREAD_COUNT);
                    tasks.put(videoDownInfo.getVideoMp4Url(), task);
                    Downloadexecutor.execute(task);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 开始下载
     *
     * @param videoDownInfo
     * @return
     */
    public boolean startDownload(VideoDownInfo videoDownInfo) {

        VideoFileDownload task = new VideoFileDownload(DownloadService.this, videoDownInfo, downloadPath,
                Key.DOWNLOAD_THREAD_COUNT);
        if (getTask(videoDownInfo)) {
            tasks.put(videoDownInfo.getVideoMp4Url(), task);
        }
        Downloadexecutor.execute(task);
        if (tasks.size() > 3) {
            return false;
        }
        return true;
    }

    public void pauseDownload(VideoDownInfo videoDownInfo) {
        String mUrl = videoDownInfo.getVideoMp4Url();
        VideoFileDownload task = tasks.get(mUrl);
        if (null != task) {
            boolean isStop = task.stopDownload(mUrl);
            if (isStop) {
                tasks.remove(mUrl);
            }
        }
    }

    public boolean stopWaitDownload(VideoDownInfo videoDownInfo) {
        String mUrl = videoDownInfo.getVideoMp4Url();
        VideoFileDownload task = tasks.get(mUrl);
        if (null != task) {
            Downloadexecutor.remove(task);
            tasks.remove(mUrl);
            return true;
        }
        return false;
    }


    /**
     * 任务是否存在
     *
     * @param info
     * @return
     */
    public boolean getTask(VideoDownInfo info) {
        String mUrl = info.getVideoMp4Url();
        if (tasks.size() > 0 && tasks.containsKey(mUrl)) {
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        nManager.cancel(1);
        Log.i("生命周期", "Service--onDestroy");

    }

    private class UpdataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            VideoDownInfo info = (VideoDownInfo) intent.getSerializableExtra("videoinfo");
            if (info != null) {
                if (Key.ACTION_DOWNLOAD_DELETE.equals(intent.getAction())) {
                    String mUrl = info.getVideoMp4Url();
                    VideoFileDownload task = tasks.get(mUrl);
                    if (null != task) {
                        boolean isStop = task.stopDownload(mUrl);
                        if (isStop) {
                            tasks.remove(mUrl);
                        }
                        Downloadexecutor.remove(task);
                    }
                }
                if (Key.ACTION_DOWNLOAD_STATE.equals(intent.getAction())) {
                    infodao.updataDownloadTask(info);
                    //任务下载完成，从集合中删除，更新任务在数据库的标志
                    if (info.getState() == Key.DOWNLOAD_STATE_COMPLETE) {
                        tasks.remove(info.getVideoMp4Url());
                        infodao.deleteDownload(info);
                        //如果下载全部完成，关掉服务
                        if (tasks.size() == 0) {
                            stopSelf();
                        }
                    } else if (info.getState() == Key.DOWNLOAD_STATE_FAILURE) {
                        tasks.remove(info.getVideoMp4Url());
                        //下载失败，是否还有任务
                        if (tasks.size() == 0) {
                            stopSelf();
                        }
                    }
                }
            } else if (info == null && Key.ACTION_DOWNLOAD_CLOSE.equals(intent.getAction())) {
                if (tasks.size() == 0) {
                    stopSelf();
                }
            }
        }

    }

    public class DownloadServiceBinder extends Binder {
        public DownloadService getDownloadService() {
            return DownloadService.this;
        }
    }
}
