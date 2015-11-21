package com.example.sniffer.httpdownload.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.View.ViewPagerIndicator;
import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.fragment.VideoCompleteDownloadFragment;
import com.example.sniffer.httpdownload.fragment.VideoLsDownloadFragment;
import com.example.sniffer.httpdownload.service.DownloadService;
import com.example.sniffer.httpdownload.utils.Key;


/**
 * 下载列表页面
 */
public class DownloadListActivity extends FragmentActivity implements View.OnClickListener,
        VideoCompleteDownloadFragment.setCompleteTextmCount, VideoLsDownloadFragment.setLsTextmCount {

    private ImageView iv_show_off;
    private TextView tv_show_on;
    private boolean mSelect = true;
    private LinearLayout ll_delete_btn;
    private Button btn_selectAll;
    private Button btn_delete;
    private int mCount = 0;
    private ImageView iv_back;
    private DownloadService downloadService;
    private FragmentManager fm;
    private VideoLsDownloadFragment lsFragment;
    private ViewPagerIndicator download_list_indicator;
    private VideoCompleteDownloadFragment completeFragment;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);
        initUI();
        initData();
        Log.e("DownloadListActivity", "onCreate");
    }

    /**
     * 初始化数据
     */
    private void initData() {
        fm = getSupportFragmentManager();
        showLsDownload();

        Intent intent = new Intent(DownloadListActivity.this, DownloadService.class);
        bindService(intent, downloadConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * 初始化UI
     */
    private void initUI() {
//      lv_download_task = (ListView) findViewById(R.id.lv_download_task);
        iv_show_off = (ImageView) findViewById(R.id.iv_show_off);
        tv_show_on = (TextView) findViewById(R.id.tv_show_on);
        btn_selectAll = (Button) findViewById(R.id.btn_selectAll);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        ll_delete_btn = (LinearLayout) findViewById(R.id.ll_delete_btn);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        download_list_indicator = (ViewPagerIndicator) findViewById(R.id.download_list_indicator);
        iv_show_off.setOnClickListener(this);
        tv_show_on.setOnClickListener(this);
        btn_selectAll.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        iv_back.setOnClickListener(this);

        download_list_indicator.setOnClickTextListener(new ViewPagerIndicator.onClickText() {
            @Override
            public void onClickLeftText() {
                showLsDownload();
            }

            @Override
            public void onClickRightText() {
                showCompleteDownload();

            }
        });
    }


    /**
     * 显示下载列表Fragment
     */
    private void showLsDownload() {
        FragmentTransaction fmbt = fm.beginTransaction();
        if (lsFragment == null) {
            lsFragment = new VideoLsDownloadFragment();
            lsFragment.setLsTextmCountListener(DownloadListActivity.this);
            fmbt.add(R.id.ll_download_task, lsFragment);
            fmbt.commit();
        } else {
            if (!(fragment instanceof VideoLsDownloadFragment)) {
                hideDelete();
            }
            fmbt.hide(completeFragment);
            fmbt.show(lsFragment);
            fmbt.commit();
        }
        fragment = lsFragment;
    }

    /**
     * 显示已下载Fragment
     */
    private void showCompleteDownload() {
        FragmentTransaction fmbt = fm.beginTransaction();
        fmbt.hide(lsFragment);
        if (completeFragment == null) {
            completeFragment = new VideoCompleteDownloadFragment();
            completeFragment.setCompleteTextmCountListener(DownloadListActivity.this);
            fmbt.add(R.id.ll_download_task, completeFragment);
            fmbt.commit();
        } else {
            fmbt.show(completeFragment);
            fmbt.commit();
        }
        if (!(fragment instanceof VideoCompleteDownloadFragment)) {
            hideDelete();
        }
        fragment = completeFragment;
    }

    /**
     * 处理点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {

        Log.e("fragmentSize", "" + fragment);
        switch (v.getId()) {
            case R.id.tv_show_on:
                hideDelete();
                break;
            case R.id.iv_show_off:
                showDelete(fragment);
                break;
            case R.id.btn_selectAll:
                selectCheckAll(fragment);
                break;
            case R.id.btn_delete:
                deleteSelectDownloadTask(fragment);
                break;
            case R.id.iv_back:
                finish();
                break;
        }

    }

    /**
     * 删除选中的任务
     */
    private void deleteSelectDownloadTask(Fragment fragment) {
        if (fragment instanceof VideoLsDownloadFragment) {
            lsFragment.deleteSelectDownloadTask();
            mCount = 0;
        } else if (fragment instanceof VideoCompleteDownloadFragment) {
            mCount = completeFragment.deleteVideoFile();
        }
        setdeleteTextandColor(mCount);
    }

    /**
     * 全部选中或取消
     */
    private void selectCheckAll(Fragment fragment) {
        if (fragment instanceof VideoLsDownloadFragment) {
            mCount = lsFragment.selectCheckAll(mSelect);
        } else if (fragment instanceof VideoCompleteDownloadFragment) {
            mCount = completeFragment.selectVideoFileAll(mSelect);
        }
        setdeleteTextandColor(mCount);
        if (mSelect) {
            btn_selectAll.setText("取消选中");
            mSelect = false;
        } else {
            btn_selectAll.setText("全选");
            mSelect = true;
        }
    }

    /**
     * “删除”TextView按键文字和边框颜色改变
     *
     * @param count
     */
    public void setdeleteTextandColor(int count) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        if (count == 0) {
            drawable.setStroke(1, Color.BLUE);
            btn_delete.setBackground(drawable);
            btn_delete.setTextColor(Color.GRAY);
            btn_delete.setText("删除");
        } else {
            drawable.setStroke(1, Color.RED);
            btn_delete.setBackground(drawable);
            btn_delete.setTextColor(Color.RED);
            btn_delete.setText("删除(" + count + ")");
        }

    }

    /**
     * 退出删除模式
     */
    private void hideDelete() {
        if (fragment != null && fragment instanceof VideoLsDownloadFragment) {
            lsFragment.showVideoFileSelect(false);
            lsFragment.selectCheckAll(false);
        } else if (fragment != null && fragment instanceof VideoCompleteDownloadFragment) {
            completeFragment.showVideoFileSelect(false);
            completeFragment.selectVideoFileAll(false);
        }
        btn_selectAll.setText("全选");
        mCount = 0;
        setdeleteTextandColor(mCount);
        mSelect = true;
        ll_delete_btn.setVisibility(View.GONE);
        tv_show_on.setVisibility(View.INVISIBLE);
        iv_show_off.setVisibility(View.VISIBLE);
    }

    /**
     * 开启删除模式
     */
    private void showDelete(Fragment fragment) {
        boolean mshow = false;
        if (fragment != null && fragment instanceof VideoLsDownloadFragment) {
            mshow = lsFragment.showVideoFileSelect(true);

        } else if (fragment != null && fragment instanceof VideoCompleteDownloadFragment) {
            mshow = completeFragment.showVideoFileSelect(true);
        }
        if (mshow) {
            ll_delete_btn.setVisibility(View.VISIBLE);
            iv_show_off.setVisibility(View.INVISIBLE);
            tv_show_on.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 获取下载服务对象
     */
    private ServiceConnection downloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadService = ((DownloadService.DownloadServiceBinder) service).getDownloadService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("DownloadListActivity", "onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(downloadConnection);
        Log.e("DownloadListActivity", "onDestroy");
    }


    /**
     * Item单击后回调
     * 改变删除TextView的数量及颜色
     * @param mCount
     */
    @Override
    public void onCompleteDownloadmCount(int mCount) {
        setdeleteTextandColor(mCount);
    }

    @Override
    public void onLsDownloadmCount(int mCount) {
        setdeleteTextandColor(mCount);
    }

    /**
     * Item单击后回调
     * 启动下载任务
     * @param videoDownInfo
     */
    @Override
    public void onDownloadTask(VideoDownInfo videoDownInfo) {
        startService(new Intent(this, DownloadService.class));
        int mFlag = videoDownInfo.getState();
        String mp4Url = videoDownInfo.getVideoMp4Url();
        Log.i("标志", mFlag + "");
        if (Key.DOWNLOAD_STATE_PAUSE == mFlag || Key.DOWNLOAD_STATE_FAILURE == mFlag) {
            boolean isStart = downloadService.startDownload(videoDownInfo);
            if (!isStart) {
                lsFragment.updataFragmentState(mp4Url, Key.DOWNLOAD_STATE_WAIT);
            }
        } else if (Key.DOWNLOAD_STATE_RUN == mFlag || Key.DOWNLOAD_STATE_READY == mFlag) {
            downloadService.pauseDownload(videoDownInfo);
        } else if (Key.DOWNLOAD_STATE_WAIT == mFlag) {
            boolean isDelete = downloadService.stopWaitDownload(videoDownInfo);
            if (isDelete) {
                lsFragment.updataFragmentState(mp4Url, Key.DOWNLOAD_STATE_PAUSE);
            }
        }
    }
}
