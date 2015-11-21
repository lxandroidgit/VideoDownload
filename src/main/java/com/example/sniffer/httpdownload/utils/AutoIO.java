package com.example.sniffer.httpdownload.utils;


import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 开启关闭IO
 */
public class AutoIO {
    private  HttpURLConnection conn;
    private  InputStream in = null;


    public  InputStream openIO(String url) {
        try {
            URL httpUrl = new URL(url);
            conn = (HttpURLConnection) httpUrl.openConnection();
            //设置连接超时
            conn.setConnectTimeout(3000);
            //从连接读取数据超时
            conn.setReadTimeout(3000);
            //设置请求方法
            conn.setRequestMethod("GET");
            //设置可以读取输入流
            conn.setDoInput(true);
            //创建输入流
            in = conn.getInputStream();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    public  void closeIO() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            conn.disconnect();// 关闭网络连接
        }
    }

    public  int getDataSize() {
        int size = 0;
        if (conn != null) {
            size = conn.getContentLength();
        }
        SystemClock.sleep(20);
        return size;
    }


}
