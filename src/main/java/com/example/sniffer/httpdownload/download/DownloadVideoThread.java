package com.example.sniffer.httpdownload.download;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 断点多线程
 */
public class DownloadVideoThread extends Thread {
    private String mUrl;//下载地址
    private int threadid;//线程标示
    private int blocklength;//每份的总长度
    private int completelength;//已完成长度
    private VideoFileDownload downloader;//数据库对象
    private File file;  //创建的file
    private int maxthread;//最大线程数
    private int LeftData;//数据长度的余数
    private int writeCount;
    private AtomicBoolean pause;


    public DownloadVideoThread(String mUrl, int threadid, int blocklength, int completelength,
                               int writeCount, VideoFileDownload downloader, File file, int maxthread, int LeftData) {
        this.threadid = threadid;
        this.blocklength = blocklength;
        this.completelength = completelength;
        this.downloader = downloader;
        this.file = file;
        this.maxthread = maxthread;
        this.LeftData = LeftData;
        this.writeCount = writeCount;
        this.mUrl =  mUrl;
        pause = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        System.out.println("线程" + threadid + "启动");
        HttpURLConnection httpconn = null;
        InputStream in = null;
        RandomAccessFile threadfile = null;
        if (blocklength > completelength) {
            try {
                URL Url = new URL(mUrl);
                httpconn = (HttpURLConnection) Url.openConnection();
                httpconn.setRequestMethod("GET");
                httpconn.setConnectTimeout(3000);
                httpconn.setReadTimeout(2000);
                //设置开始数据
                int startPos = blocklength * (threadid - 1) + completelength;
                //设置结束数据
                int endPos = blocklength * threadid - 1;
                if (threadid == maxthread) {
                    startPos = (blocklength * (threadid - 1) - LeftData * (maxthread - 1)) + completelength;
                    endPos = ((blocklength * threadid) - LeftData * (maxthread - 1)) - 1;
                }
                //设置获取实体数据的范围
                httpconn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
                // Log.i("线程中", "线程id：" + threadid + "---已经设置完数据");
                //根据响应码获取大小
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    in = httpconn.getInputStream();
                    byte[] buffer = new byte[12 * 1024];
                    int len;
                    Log.i("下载开始", "线程id：" + threadid + " 开始的数据: " + startPos +
                            " 结束的数据：" + endPos);
                    //RandomAccessFile参数1：打开文件，参数2：文件访问模式：4种 r，rw，rws，rwd
                    threadfile = new RandomAccessFile(file, "rwd");
                    //设置到此文件开头测量到的文件指针偏移量
                    threadfile.seek(startPos);
                    int data = blocklength / 10;
                    int endData = data * writeCount;
                    //&& !Thread.currentThread().isInterrupted()
                    while ((len = in.read(buffer)) != -1 && !pause.get()) {
                        threadfile.write(buffer, 0, len);
                        downloader.append(len);
                        completelength += len;
                        if (completelength > endData) {
                            writeCount++;
                            downloader.updata(mUrl, threadid, completelength, writeCount);
                            endData += data;
                            Log.i("正在下载", "线程id：" + threadid + "--每份的数据:" + data + "--应写入数据：" + completelength);
                        }

                    }
                    Log.i("下载结束", "线程id：" + threadid + " 开始的数据: " + startPos +
                            " 结束的数据：" + endPos);
                    if (blocklength == completelength) {
                        Log.i("下载线程", "正在更改标志");
                        downloader.upIsData(mUrl, threadid, 1);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i("DownloadVideoThread.run", "下载地址错误");
                if (pause.get()) {
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("IOException", "接收到一个网络异常，线程重新尝试下载");
                if (pause.get()) {
                    return;
                }

                try {
                    sleep(3000);
                    run();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    pause.set(true);
                    interrupt();
                    Log.i("InterruptedException", "接收到一个中断信号，退出阻塞状态");
                }
            } finally {
                try {
                    Log.i("return", "线程ID:" + threadid + "--return");
                    downloader.updata(mUrl, threadid, completelength, writeCount);
                    if (threadfile != null) {
                        threadfile.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (httpconn != null) {
                        httpconn.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (pause.get()) {
                        return;
                    }
                }

            }

        }

    }

    public void isPause() {
        pause.set(true);
        interrupt();
    }
}

