package com.example.sniffer.httpdownload.download;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.dao.VideoUrlDao;
import com.example.sniffer.httpdownload.read.HomeReadUrlThread;
import com.example.sniffer.httpdownload.read.VideoResourcesReadThread;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 给主线程发送消息
 */
public class InitDataThread extends Thread {
    private List<String> listStr;
    private List<VideoDownInfo> list;
    private Context context;
    private ThreadPoolExecutor executor;
    private Handler mHandler;
    private int number;
    private static boolean mFlag;
    private int MsgWhat;
    private VideoUrlDao videoUrlDao;

    public InitDataThread(Context context, Handler mHandler, int number, int MsgWhat) {
        this.context = context;
        listStr = new ArrayList<>();
        this.mHandler = mHandler;
        this.number = number;
        this.MsgWhat = MsgWhat;
        list = new ArrayList<>();
        mFlag = false;
        videoUrlDao = new VideoUrlDao(context);
    }

    @Override
    public void run() {
        final String url = "http://www.99rr3.com/latest-updates/" + number + "/";
        new HomeReadUrlThread(url, listStr).start();
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("初始化", "正在读取listStr的数量" + listStr.size());
        if (listStr.size() > 0) {
            //创建读取每个Video内容的线程池
            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
            readVideoInfo();
            //线程池的运行线程不为0则循环
            while (executor.getActiveCount() != 0) {
                SystemClock.sleep(500);
                if (mFlag) {
                    return;
                }
            }
            for (VideoDownInfo videoDownInfo : list) {
                if (!videoUrlDao.findVideoUrl(videoDownInfo.getVideoMp4Url())) {
                    videoUrlDao.addVideoUrl(videoDownInfo);
                    Log.e("数据库查询", "name:" + videoDownInfo.getVideoName() + "--mp4Url:" +
                            videoDownInfo.getVideoMp4Url() + "--ImageUrl:" + videoDownInfo.getVideoImageUrl() +
                            "--Time" + videoDownInfo.getTime());
                }
            }
            Log.i("初始化", "读取网址关闭......");
            executor.shutdownNow();
        }
        Message msg = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", (Serializable) list);
        msg.setData(bundle);
        msg.what = MsgWhat;
        mHandler.sendMessage(msg);
        mFlag = true;
    }

    /**
     * 读取Video实体
     */

    public void readVideoInfo() {

        for (int i = 0, length = listStr.size(); i < length; i++) {
            //线程池加入线程
            executor.execute(new VideoResourcesReadThread(listStr.get(i), new VideoResourcesReadThread.addVideo() {
                @Override
                public void addvideo(VideoDownInfo videoDownInfo) {
                    list.add(videoDownInfo);
                }
            }));
        }

    }

    public static boolean isEndThread() {
        return mFlag;
    }

    public static void setEndThread() {
        mFlag = true;
    }

/*    private class HandlerData extends Handler {

        private HandlerData(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            System.out.println("子线程收到:" + msg.what);
        }
    }*/
}
