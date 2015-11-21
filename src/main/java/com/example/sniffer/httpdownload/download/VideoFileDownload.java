package com.example.sniffer.httpdownload.download;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;

import com.example.sniffer.httpdownload.bean.FileInfo;
import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.dao.FileDownloadProgressDao;
import com.example.sniffer.httpdownload.utils.FileUtils;
import com.example.sniffer.httpdownload.utils.Key;
import com.example.sniffer.httpdownload.utils.MyApp;
import com.example.sniffer.httpdownload.utils.SendBroadcestRevice;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文件下载
 */
public class VideoFileDownload extends Thread {
    private final File newFile;
    private Context context;
    private File file;
    private AtomicInteger downloadSize;
    private int fileSize = 0;
    private List<FileInfo> fileInfos;
    private FileDownloadProgressDao dao;
    private ThreadPoolExecutor newexecutor;
    private int maxThread;
    private String mUrl;
    private List<DownloadVideoThread> threads;
    private VideoDownInfo videoinfo;

    /**
     * 构建文件下载
     *
     * @param context   上下文
     * @param maxThread 下载线程数量
     */

    public VideoFileDownload(Context context, VideoDownInfo videoinfo, File downloadPath,
                             int maxThread) {
        this.context = context;
        this.maxThread = maxThread;
        this.videoinfo = videoinfo;
        this.fileSize = videoinfo.getVideosize();
        threads = new ArrayList();
        downloadSize = new AtomicInteger(0);
        //如果文件目录不存在，则创建目录
        if (!downloadPath.exists()) {
            downloadPath.mkdirs();
        }
        //初始化数据库
        FileDownloadProgressDao.initializeInstance(context);
        dao = FileDownloadProgressDao.getDao();
        mUrl = videoinfo.getVideoMp4Url();
        String name = videoinfo.getVideoName();
        //构建保存文件
        file = new File(downloadPath, name + ".download");
        newFile = new File(downloadPath, name + ".mp4");
        fileInfos = dao.getData(mUrl);
        //如果存在下载记录
        if (fileInfos.size() > 0) {
            //计算已经下载的数据
            for (FileInfo fileInfo : fileInfos) {
                downloadSize.addAndGet(fileInfo.getCompletelength());
            }
        }
        Log.i("初始化下载", "构建完毕，可以构建线程");
    }

    @Override
    public void run() {
        SendBroadcestRevice.sendRecice(context, videoinfo, Key.DOWNLOAD_STATE_READY,
                downloadSize.get());
        Download();
        readState(newexecutor);
        //关闭线程池
        newexecutor.shutdown();
        fileInfos = dao.getData(mUrl);
        isDownload();
    }


    /**
     * 关闭线程池,停止下载
     *
     * @return
     */
    public boolean stopDownload(String Url) {
        if (mUrl.equals(Url) && threads.size() > 0) {
            for (DownloadVideoThread thread : threads) {
                thread.isPause();
            }
            return true;
        }
        return false;
    }

    /**
     * 更新数据库下载标志
     *
     * @param threadid
     * @param isData
     */
    public synchronized void upIsData(String url, int threadid, int isData) {
        dao.upIsData(url, threadid, isData);
    }

    /**
     * 更新数据库已经下载长度
     *
     * @param threadid
     * @param completelength
     */
    public synchronized void updata(String url, int threadid, int completelength, int writeCount) {
        dao.upData(url, threadid, completelength, writeCount);
    }

    /**
     * 累计下载长度
     *
     * @param size
     */
    protected void append(int size) {
        downloadSize.addAndGet(size);
    }

    /**
     * 开始下载
     *
     * @throws Exception
     */
    public void Download() {
        if (fileSize < 0) {
            return;
        }
        try {
            RandomAccessFile randOut = new RandomAccessFile(file, "rw");
            randOut.setLength(fileSize);
            randOut.close();
            //计算每根线程的数据
            int LeftData = fileSize % maxThread;
            int datalength = (fileSize - LeftData) / maxThread;
            //创建线程池
            newexecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThread);
            if (fileInfos.size() == 0) {
                fileInfos = new ArrayList();
                Log.i("数据总大小", "数据总大小:" + fileSize + "---每份数据：" + datalength +
                        "---模后的数据:" + LeftData);
                //初始化每条线程应下载的数据
                for (int i = 1; i < maxThread + 1; i++) {
                    FileInfo fileinfo;
                    if (i <= maxThread - 1) {
                        fileinfo = new FileInfo(i, datalength, 0, 0, 1);
                        fileInfos.add(fileinfo);
                    }
                    if (i == maxThread) {
                        fileinfo = new FileInfo(i, datalength + LeftData, 0, 0, 1);
                        fileInfos.add(fileinfo);
                    }
                }
                //添加任务到数据库
                dao.addDownload(mUrl, fileInfos);
                //开启线程进行下载
                for (int i = 0, size = fileInfos.size(); i < size; i++) {
                    Log.i("Download-", "开启线程：" + i + "---下载的数据量：" +
                            fileInfos.get(i).getCompletelength());
                    DownloadVideoThread thread = new DownloadVideoThread(mUrl,
                            fileInfos.get(i).getThreadid(), fileInfos.get(i).getDatalength(),
                            fileInfos.get(i).getCompletelength(), fileInfos.get(i).getWriteCount(),
                            this, file, maxThread, LeftData);
//                //线程池加入线程
                    newexecutor.execute(thread);
                    threads.add(thread);
                }
                SendBroadcestRevice.sendRecice(context, videoinfo, Key.DOWNLOAD_STATE_RUN,
                        downloadSize.get());

            } else {
                //取出数据对比
                for (int i = 0, size = fileInfos.size(); i < size; i++) {
                    //开启没有完成数据的线程进行下载
                    if (0 == fileInfos.get(i).getIsData()) {
                        Log.i("继续下载", "开启继续线程：" + i + fileInfos.get(i).getDatalength());
                        DownloadVideoThread thread = new DownloadVideoThread(mUrl,
                                fileInfos.get(i).getThreadid(), fileInfos.get(i).getDatalength(),
                                fileInfos.get(i).getCompletelength(), fileInfos.get(i).getWriteCount(),
                                this, file, maxThread, LeftData);
                        //线程池加入线程
                        newexecutor.execute(thread);
                        threads.add(thread);
                    }
                }
                SendBroadcestRevice.sendRecice(context, videoinfo, Key.DOWNLOAD_STATE_RUN,
                        downloadSize.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
            SendBroadcestRevice.sendRecice(context, videoinfo, Key.DOWNLOAD_STATE_FAILURE,
                    downloadSize.get());
        }
    }

    /**
     * 读取线程池活动线程的状态,发送广播通知进度
     *
     * @param
     */

    private void readState(ThreadPoolExecutor newexecutor) {
        int count;
        int progress = 0;
        int downloadSizeSecond;
        Intent intent = new Intent();
        intent.setAction(Key.ACTION_DOWNLOAD_PROGRESS);
        intent.putExtra("mp4Url", mUrl);
        do {
            downloadSizeSecond = downloadSize.get() - progress;
            //Log.i("每秒速度", "每秒速度为：" + downloadSizeSecond);
            count = newexecutor.getActiveCount();
            progress = downloadSize.get();
            intent.putExtra("progress", progress);
            intent.putExtra("second", downloadSizeSecond);
            context.sendBroadcast(intent);
            //通知目前已经下载完成的数据长度
            // Log.i("下载完成：", "数据最大值：" + progress);
            Log.i("正在下载", "已经下载数据:" + Formatter.formatFileSize(MyApp.getContext(), progress));
            Log.i("开始下载", "任务队列大小：" + count);
            SystemClock.sleep(1000);
        } while (count != 0);

    }


    /**
     * 是否完成下载
     *
     * @return
     */
    public void isDownload() {
        for (FileInfo info : fileInfos) {
            if (info.getIsData() == 0) {
                SendBroadcestRevice.sendRecice(context, videoinfo, Key.DOWNLOAD_STATE_PAUSE,
                        downloadSize.get());
                return;
            }
        }
        Log.i("结束下载：", "删除数据表");
        if (FileUtils.ranameToNewFile(file, newFile)) {
            SendBroadcestRevice.sendRecice(context, videoinfo, Key.DOWNLOAD_STATE_COMPLETE,
                    downloadSize.get());
            dao.deletePath(mUrl);
        }
    }

    /**
     * 检测每根线程的数据是否完成
     *
     * @param list
     * @param dao
     * @param downUrl
     * @return
     */
/*    private int checkDownload(List<FileInfo> list, FileDownloadProgressDao dao, String downUrl) {
        list = dao.getData(downUrl);
        int number = 0;
        for (FileInfo info : list) {
            Log.i("检测下载", "线程id:" + info.getThreadid() + "---应完成数据:" + info.getDatalength() + "---已完成数据:" + info.getCompletelength() +
                    "---完成标志:" + info.getIsData());
            if (info.getDatalength() == info.getCompletelength()) {
                info.setIsData(1);
            } else {
                number += 1;
            }
        }
        if (number > 0) {
            dao.addDownload(downUrl, list);
            return number;
        }
        return number;
    }*/

}




