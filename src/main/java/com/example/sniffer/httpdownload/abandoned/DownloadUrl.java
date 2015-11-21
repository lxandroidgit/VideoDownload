package com.example.sniffer.httpdownload.abandoned;

import android.os.SystemClock;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by sniffer on 15-10-16.
 */
public class DownloadUrl implements IoAuto {
    static Pattern z = Pattern.compile("[\\u4e00-\\u9fa5]+");
    static Pattern h = Pattern.compile("http.*mp4\\/");
    static Pattern i = Pattern.compile("http.*?\\.jpg");
    static Pattern p = Pattern.compile("http.*\\/");
    private IoAuto io;

    private List<String> strlist;

    public DownloadUrl(IoAuto ioAuto) {

        this.io = ioAuto;
    }


    @Override
    public void downloadHttpUrl(String url) {
        io.downloadHttpUrl(url);
        downloadurl();
    }

    @Override
    public void closeAll() throws IOException {

    }

    @Override
    public BufferedReader getbufferedReader() {
        return null;
    }

    public List<String> getListString() {
        return strlist;
    }

    public void downloadurl() {
        new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(1500);
                strlist = new ArrayList<String>();
                BufferedReader br=io.getbufferedReader();
                if (br != null) {
                    String s = null;
                    System.out.println("开始读取数据");
                    try {
                        while ((s = br.readLine()) != null) {
                            if (s.contains("kt_imgrc")) {
                                Matcher http = p.matcher(s);
                                if (http.find()) {
                                    System.out.println("网址：" + http.group());
                                    String str = http.group();
                                    strlist.add(str);
                                }
                            }

                        }
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
