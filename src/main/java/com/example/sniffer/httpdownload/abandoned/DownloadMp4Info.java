package com.example.sniffer.httpdownload.abandoned;

import android.os.SystemClock;

import com.example.sniffer.httpdownload.bean.VideoDownInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取每个Mp4的图片，名字，URL
 */
public class DownloadMp4Info implements IoAuto {
    static Pattern z = Pattern.compile("[\\u4e00-\\u9fa5]+");
    static Pattern h = Pattern.compile("http.*mp4\\/");
    static Pattern i = Pattern.compile("http.*?\\.jpg");
    private IoAuto io;
    private VideoDownInfo videoinfo;

    public DownloadMp4Info(IoAuto io) {
        this.io = io;
    }


    @Override
    public void downloadHttpUrl(String url) {
        io.downloadHttpUrl(url);
        getMp4Info();
    }

    @Override
    public void closeAll() throws IOException {

    }

    @Override
    public BufferedReader getbufferedReader() {
        return null;
    }

    public VideoDownInfo getVideoInfos() {
        return videoinfo;
    }


    public synchronized void getMp4Info() {
        new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(1500);
                BufferedReader br = io.getbufferedReader();
                if (br != null) {
                    String s = null;
 /*                 String videoName = null;
                    String videoMp4 = null;
                    String videoImage = null;*/
                    try {
                        while ((s = br.readLine()) != null) {
                            if (s.contains("desc-video")) {
                                Matcher name = z.matcher(s);
                                if (name.find()) {
                                    System.out.println("名字：" + name.group());
                                    //      videoName = name.group();
                                }
                            }
                            if (s.contains("video_url")) {
                                Matcher mp4 = h.matcher(s);
                                if (mp4.find()) {
                                    //      videoMp4 = mp4.group();
                                    System.out.println("网址：" + mp4.group());
                                }
                                String[] srs = s.split("\\,");
                                for (String sr : srs) {
                                    if (sr.contains("preview_url")) {
                                        Matcher images = i.matcher(sr);
                                        if (images.find()) {
                                            //            videoImage = images.group();
                                            System.out.println("图片：" + images.group());
                                        }
                                    }
                                }
                            }
                        }
                        //      System.out.println("一行：" + videoName + videoMp4 + videoImage);
                        //       videoinfo = new VideoDownInfo(videoName, videoImage, videoMp4);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            io.closeAll();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }.start();
    }
}
