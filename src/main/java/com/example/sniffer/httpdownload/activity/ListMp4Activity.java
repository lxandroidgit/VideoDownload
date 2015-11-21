package com.example.sniffer.httpdownload.activity;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.dao.FileDownloadInfoDao;
import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.fragment.VideoShowFragmentOne;
import com.example.sniffer.httpdownload.utils.Key;
import com.example.sniffer.httpdownload.utils.SendBroadcestRevice;
import com.example.sniffer.httpdownload.utils.ServiceStatusUtils;

import java.util.List;

public class ListMp4Activity extends FragmentActivity {

    private FileDownloadInfoDao infodao;
    private VideoShowFragmentOne mFragment;
    private TextView tv_download_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download_show);
        if (isNetworkConnected(this)) {
            initUI();
            //如果下载服务不存在
            if (!ServiceStatusUtils.isAddressService(this,
                    "com.example.sniffer.httpdownload.service.DownloadService")) {
                updataState();
            }
        } else {
            Intent intent = new Intent(this, NoNetworkActivity.class);
            startActivity(intent);
            finish();
        }
        Log.i("生命周期", "Activity——onCreate");
    }

    //http://www.99rr1.com/get_file/3/85f83124afd7063e1f0de05e5ef78343/35000/35360/35360.mp4
//http://www.99rr1.com/embed/35360
//http://h.hiphotos.baidu.com/image/pic/item/6c224f4a20a446239e8d311c9b22720e0cf3d70d.jpg
    // flashvars
    //http://www.99rr1.com/get_file/3/ef5afabd37efe3bf6debf9552e49f69d/37000/37308/37308.mp4
    //首页 网址：kt_imgrc  图片：KT_rotationStart 次页video_url

    private void initUI() {
        tv_download_list = (TextView) findViewById(R.id.tv_download_list);
        infodao = new FileDownloadInfoDao(this);
        showFragmentData();

        /**
         * 跳转到下载列表
         */
        tv_download_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListMp4Activity.this, DownloadListActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 下载任务状态除了暂停和已完成，全部重置为暂停状态
     */
    public void updataState() {
        List<VideoDownInfo> videoDownInfos = infodao.getDownloadTaskAll();
        for (VideoDownInfo videoDownInfo : videoDownInfos) {
            int state = videoDownInfo.getState();
            if (1 != state && 2 != state) {
                videoDownInfo.setState(1);
            }
        }
        infodao.updataDownloadTaskAll(videoDownInfos);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("生命周期", "Activity——onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("生命周期", "Activity——onRestart");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i("生命周期", "Activity——onConfigurationChanged");
    }

    /**
     * 随机更换页面
     *
     * @param
     */
/*    public void lastVideo(View view) {
        Random random = new Random();
        if (maxPage > 0 && InitDataThread.isEndThread()) {
            int mNumber = random.nextInt(maxPage);
            tv_data_number.setText(mNumber + "/" + maxPage);
            showFragmentData(mNumber);
        }
    }*/
    public void showFragmentData() {
        //开启事务管理者
        FragmentManager mFragementManager = getSupportFragmentManager();
        //开启事务
        FragmentTransaction mTransaction = mFragementManager.beginTransaction();
        mFragment = new VideoShowFragmentOne();
        mTransaction.replace(R.id.ll_content, mFragment);
        //事务提交
        mTransaction.commit();
    }

    /**
     * 获取网络是否连接
     *
     * @param context
     * @return
     */
    public boolean isNetworkConnected(Context context) {
        if (null != context) {
            //获取网络服务管理
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(
                    context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (null != mNetworkInfo) {
                //网络是否连接
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SendBroadcestRevice.sendServiceRecive(this, Key.ACTION_DOWNLOAD_CLOSE, null);
        Log.i("生命周期", "Activity——onDestroy");
    }


}
/**
 * 拷贝数据库
 *
 * @param dbName
 */
/*
    public void copyDB(String dbName) {

        File destFile = new File(getFilesDir(), dbName);// 要拷贝的目标地址
        InputStream in = null;
        FileOutputStream out = null;
        if (destFile.exists()) {
            System.out.println("数据库" + dbName + "已存在!");
            return;
        }
        try {
            Log.i("数据库", "准备打开输入流");
            in = getResources().openRawResource(R.raw.videourl);
            Log.i("数据库", "建立好输入流");
            out = new FileOutputStream(destFile);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            Log.e("数据库", "写数据库");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

/*else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //代码动态添加一个控件，并指定位置
                            FrameLayout frame = new FrameLayout(ListMp4Activity.this);
                            FrameLayout.LayoutParams tvparams = new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            tvparams.gravity = Gravity.CENTER;
                            TextView tv = new TextView(ListMp4Activity.this);
                            tv.setText("网络无法连接");
                            frame.addView(tv, tvparams);
                            LinearLayout.LayoutParams fparams = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                            );
                            fparams.topMargin = -0;
                            ll_mylauout.addView(frame, fparams);
                        }
                    });*/
/*
                    //单线程下载
                    VideoDownInfo urlinfo = list.get(position);
                    hanler.pb_count.setMax(urlinfo.getVideosize());
                    flag = true;
                    if (flag) {
                        hanler.btn_download.setVisibility(View.INVISIBLE);
                        hanler.pb_decoration.setVisibility(View.VISIBLE);
                    }
                    new DownloadMp4Thread(urlinfo.getVideoMp4Url(), handler, ListMp4Activity.this, urlinfo.getVideoName(),
                            new DownloadMp4Thread.DownloadSize() {
                                @Override
                                public void fileress(int progress) {
                                    hanler.pb_count.setProgress(progress);
                                    ress = new DecimalFormat("##.##").format(((double) (progress / 1024)) / 1024);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hanler.tv_downsize.setText(ress + " MB" + "/" + mp4Size + " MB");
                                        }
                                    });
                                }

                                @Override
                                public void isComplete(boolean mflag) {
                                    if (mflag) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                hanler.btn_download.setVisibility(View.VISIBLE);
                                                hanler.pb_decoration.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }

                            }).start();*/
