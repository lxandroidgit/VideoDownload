package com.example.sniffer.httpdownload.abandoned;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.widget.Toast;

import com.example.sniffer.httpdownload.utils.MyApp;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 *
 */
public class AutoIo implements IoAuto {

    private InputStream in = null;
    private HttpURLConnection conn = null;
    private BufferedReader br = null;
    private static final int CODE_URLERROR = 1;
    private static final int CODE_HTTPERROR = 2;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case CODE_URLERROR:
                    Toast.makeText(MyApp.getContext(), "网址错误", Toast.LENGTH_SHORT).show();
                    break;
                case CODE_HTTPERROR:
                    Toast.makeText(MyApp.getContext(), "无法连接网络", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    public void downloadHttpUrl(String url) {
        openConn(url);
    }

    @Override
    public void closeAll() throws IOException {
        closeIn();
    }

    @Override
    public BufferedReader getbufferedReader() {
        return br;
    }


    public void closeIn() throws IOException {
        if (br != null) {
            br.close();
        }
        if (in != null) {
            in.close();
        }
        if (conn != null) {
            conn.disconnect();
        }
    }


    private void openConn(final String url) {
        final Message msg = new Message();
        new Thread() {
            @Override
            public void run() {
                try {
                    URL httpUrl = new URL(url);
                    conn = (HttpURLConnection) httpUrl.openConnection();
                    //设置请求方法
                    conn.setRequestMethod("GET");
                    //设置连接超时时间
                    conn.setReadTimeout(5000);
                    SystemClock.sleep(400);
                    if (conn != null) {
                        //设置可以读取输入流
                        in = conn.getInputStream();
                        br = new BufferedReader(new InputStreamReader(in));
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    msg.what = CODE_URLERROR;
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.what = CODE_HTTPERROR;
                } finally {
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

}
