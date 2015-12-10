package com.example.sniffer.httpdownload.utils;

/**
 * 常量
 */
public class Key {
    //广播action
    public static final String ACTION_DOWNLOAD_STATE = "ACTION_DOWNLOAD_STATE";
    public static final String ACTION_DOWNLOAD_START = "ACTION_DOWNLOAD_START";
    public static final String ACTION_DOWNLOAD_DELETE = "ACTION_DOWNLOAD_DELETE";
    public static final String ACTION_DOWNLOAD_PROGRESS = "ACTION_DOWNLOAD_PROGRESS";
    public static final String ACTION_DOWNLOAD_CLOSE = "ACTION_DOWNLOAD_CLOSE";
    //任务状态
    public static final int DOWNLOAD_STATE_RUN = 0;
    public static final int DOWNLOAD_STATE_PAUSE = 1;
    public static final int DOWNLOAD_STATE_COMPLETE = 2;
    public static final int DOWNLOAD_STATE_WAIT = 3;
    public static final int DOWNLOAD_STATE_READY = 4;
    public static final int DOWNLOAD_STATE_FAILURE = 5;
    public static final int DOWNLOAD_THREAD_COUNT = 10;//下载线程数量
    public static final int UP_VIDEO_DATA = 0;
    public static final int UP_VIDEO_UPDATE = 1;
    public static final int IMAGE_CACHE_SIZE = 100000000;
    public static int totalnumber;
    public static String VIDEO_ALL_URL;
    public static final String VIDEO_HOME = "http://www.99rr1.com/";
}
