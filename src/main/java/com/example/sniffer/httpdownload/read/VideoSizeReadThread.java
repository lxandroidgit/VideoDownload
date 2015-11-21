package com.example.sniffer.httpdownload.read;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.LruCache;
import android.widget.GridView;
import android.widget.TextView;

import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.utils.AutoIO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 子线程获取Video的大小
 */
public class VideoSizeReadThread {

    private LruCache<String, Integer> mSizeCache;

    private Context context;

    private ThreadPoolExecutor executor;

    private Set<VideoSizeThread> mSizeTask;

    private GridView mListView;

    private List<VideoDownInfo> videoDownInfos;


    public VideoSizeReadThread(Context context, GridView mListView, List<VideoDownInfo> videoDownInfos) {
        this.context = context;
        this.mListView = mListView;
        this.videoDownInfos = videoDownInfos;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        mSizeTask = new HashSet<>();
        //获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int sizeCache = maxMemory / 10;
        mSizeCache = new LruCache<String, Integer>(sizeCache) {

            @Override
            protected int sizeOf(String key, Integer value) {
                return 32;
            }
        };
    }

    /**
     * 获取缓存中的视频大小
     *
     * @param url
     * @return
     */
    public Integer getVideSizeFromCache(String url) {
        Integer size = mSizeCache.get(url);
        if (url != null) {
            return size;
        }
        return null;
    }

    /**
     * 将视频大小加入缓存
     *
     * @param url
     * @param size
     */
    public void addVideSizeToCache(String url, Integer size) {
        if (null != mSizeCache && null == mSizeCache.get(url)) {
            if (null != url && size > 0) {
                mSizeCache.put(url, size);
            }
        }
    }

    /**
     * 加载从Start到end所有视频图片
     *
     * @param start
     * @param end
     */
    public void loadVideoSize(int start, int end) {

        if (videoDownInfos.size() == 0) {
            return;
        }
        for (int i = start; i < end; i++) {
            VideoDownInfo videoDownInfo = videoDownInfos.get(i);
            String videoUrl = videoDownInfo.getVideoMp4Url();
            TextView mTextView = (TextView) mListView.findViewWithTag(videoUrl);
            Integer size = getVideSizeFromCache(videoUrl);
            if (size == null || size < 0) {
                VideoSizeThread task = new VideoSizeThread(videoDownInfo);
                executor.execute(task);
                mSizeTask.add(task);
            } else {
                if (mTextView != null) {
                    String mp4Size = Formatter.formatFileSize(context, size);
                    mTextView.setText(mp4Size);
                }

            }
        }
    }


    public void getVideoSize(TextView mTextView, String videoUrl) {
        Integer size = getVideSizeFromCache(videoUrl);
        if (null == size || size < 0) {
            mTextView.setText("正在获取...");
        } else {
            String mp4Size = Formatter.formatFileSize(context, size);
            mTextView.setText(mp4Size);
        }
    }

    /**
     * 关闭线程池
     */
    public void clearExecutor() {
        if (executor != null) {
            if (executor.getActiveCount() > 0) {
                executor.shutdownNow();
            }
            executor = null;
        }
    }

    /**
     * 移除线程池队列中的任务
     */
    public void cancelAllTasks() {
        for (VideoSizeThread task : mSizeTask) {
            executor.remove(task);
        }
    }

    /**
     * 读取大小线程
     */
    private class VideoSizeThread extends Thread {

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Bundle bundle = msg.getData();
                    int size = bundle.getInt("size");
                    videoinfo.setVideosize(size);
                    String videoUrl = videoinfo.getVideoMp4Url();
                    addVideSizeToCache(videoUrl, size);
                    String mp4Size = Formatter.formatFileSize(context, size);
                    TextView mTextView = (TextView) mListView.findViewWithTag(videoUrl);
                    if (mTextView != null && size > 0) {
                        mTextView.setText(mp4Size);
                    }
                }

            }
        };
        private VideoDownInfo videoinfo;

        public VideoSizeThread(VideoDownInfo videoinfo) {
            this.videoinfo = videoinfo;
        }

        @Override
        public void run() {
            int size = readsize(videoinfo.getVideoMp4Url());
            Message msg = handler.obtainMessage();
            msg.what = 1;
            Bundle bundle = new Bundle();
            bundle.putInt("size", size);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    /**
     * 读取视频大小
     *
     * @param url
     * @return
     */
    public int readsize(String url) {
        AutoIO mAutoIo = new AutoIO();
        mAutoIo.openIO(url);
        int size = mAutoIo.getDataSize();
        mAutoIo.closeIO();
        return size;
    }
}
