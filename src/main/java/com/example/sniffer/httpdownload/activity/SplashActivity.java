package com.example.sniffer.httpdownload.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.utils.Key;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * 欢迎界面
 */
public class SplashActivity extends Activity {
    private RelativeLayout rlRoot;
    private SharedPreferences sp;
    protected static final int CODE_INTO_HOME = 0;
    private static final int CODE_NET_ERORR = 1;

    /**
     * 使用静态内部handler类解决内存泄露
     *
     * @author sniffer
     */
    private static class MyHandler extends Handler {
        private final WeakReference<SplashActivity> myActivity;

        public MyHandler(SplashActivity activity) {
            myActivity = new WeakReference<>(activity);
        }

        public void handleMessage(android.os.Message msg) {
            SplashActivity activity = myActivity.get();
            switch (msg.what) {
                case CODE_NET_ERORR:
                    Toast.makeText(activity, "网络错误", Toast.LENGTH_LONG).show();
                    activity.intoHome();
                    break;
                case CODE_INTO_HOME:
                    activity.intoHome();
                    break;

            }
        }
    }

    private MyHandler mHandler = new MyHandler(this);

    /**
     * 进入主页面
     */
    protected void intoHome() {
        Intent intent = new Intent();
        intent.setClass(this, ListMp4Activity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        rlRoot = (RelativeLayout) findViewById(R.id.rlRoot);
        sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        CheckHomeUrl();
        // 渐变的动画效果
        AlphaAnimation anim = new AlphaAnimation(0.3f, 1f);
        anim.setDuration(2000);
        rlRoot.startAnimation(anim);
    }


    private void CheckHomeUrl() {
        new Thread() {
            @Override
            public void run() {
                long mTime = System.currentTimeMillis();
                Message msg = Message.obtain();// 创建消息
                try {
                    Document doc = Jsoup.connect(Key.VIDEO_HOME)
                            .get();
                    Elements links = doc.select("a[href]");
                    String str = " ";
                    for (Element link : links) {
                        String s = link.attr("target");
                        if (s.equals("_parent")) {
                            str = link.attr("href");
                        }
                    }
                    String homeUrl = sp.getString("homeUrl", " ");
                    if (!str.equals(" ") && !str.equals(homeUrl)) {
                        sp.edit().putString("homeUrl", str).commit();
                    }
                    Log.e(getName(), "home:" + homeUrl);
                    msg.what = CODE_INTO_HOME;
                } catch (IOException e) {
                    // 网络错误异常
                    msg.what = CODE_NET_ERORR;
                    e.printStackTrace();
                } finally {
                    long mEndTime = System.currentTimeMillis();
                    long mPast = mEndTime - mTime; // 访问网络花费时间
                    if (mPast < 2000) {
                        // 强制休眠一段时间,保证闪屏页展示2秒钟
                        try {
                            Thread.sleep(2000 - mPast);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mHandler.sendMessage(msg);
            }
        }.start();
    }
}
