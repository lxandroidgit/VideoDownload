package com.example.sniffer.httpdownload.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.adapter.DownloadTaskAdapter;
import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.dao.FileDownloadInfoDao;
import com.example.sniffer.httpdownload.dao.FileDownloadProgressDao;
import com.example.sniffer.httpdownload.utils.Key;

import java.util.ArrayList;
import java.util.List;

/**
 * 正在下载
 */
public class VideoLsDownloadFragment extends Fragment {
    private FileDownloadInfoDao infodao;
    private List<VideoDownInfo> videoDownInfos;
    private ListView lv_download_task;
    private DownloadTaskAdapter adapter;
    private boolean mItemShow = false;
    private int mCount = 0;
    private UpdataReceiver receiver;

    private setLsTextmCount listener;

    public interface setLsTextmCount {
        void onLsDownloadmCount(int mCount);

        void onDownloadTask(VideoDownInfo videoDownInfo);
    }

    public void setLsTextmCountListener(setLsTextmCount listener) {
        this.listener = listener;
    }

    //初始化数据
    @Override
    public void onCreate(Bundle savedInstanceState) {
        initData();
        super.onCreate(savedInstanceState);
    }

    private void initData() {
        infodao = new FileDownloadInfoDao(getActivity());
        videoDownInfos = new ArrayList();
        videoDownInfos = infodao.getDownloadTaskAll();
        FileDownloadProgressDao.initializeInstance(getActivity());
        FileDownloadProgressDao dao = FileDownloadProgressDao.getDao();
        for (VideoDownInfo videoDownInfo : videoDownInfos) {
            int progress = dao.getProgress(videoDownInfo.getVideoMp4Url());
            videoDownInfo.setVideoProgress(progress);
        }


        //注册广播
        receiver = new UpdataReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Key.ACTION_DOWNLOAD_PROGRESS);
        intentfilter.addAction(Key.ACTION_DOWNLOAD_STATE);
        getActivity().registerReceiver(receiver, intentfilter);
    }

    //初始化UI
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_ls, null);
        lv_download_task = (ListView) view.findViewById(R.id.lv_download_task);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        adapter = new DownloadTaskAdapter(getActivity(), videoDownInfos, R.layout.item_download_task);
        lv_download_task.setAdapter(adapter);
        /**
         * ListView的item点击事件
         */
        lv_download_task.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object object = lv_download_task.getItemAtPosition(position);
                VideoDownInfo info = (VideoDownInfo) object;
                if (mItemShow) {
                    if (info.isCheck()) {
                        info.setIsCheck(false);
                        mCount--;
                    } else {
                        info.setIsCheck(true);
                        mCount++;
                    }
                    adapter.notifyDataSetChanged();
                    if (listener != null) {
                        listener.onLsDownloadmCount(mCount);
                    }
                } else {
                    listener.onDownloadTask(info);
                }

            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 删除选中的任务
     */
    public void deleteSelectDownloadTask() {
        if (videoDownInfos.size() > 0 && mCount > 0) {
            adapter.deleSelectTask();
            mCount = 0;
        }
    }

    /**
     * 全部选中或取消
     */
    public int selectCheckAll(boolean isSelect) {
        if (videoDownInfos != null) {
            mCount = videoDownInfos.size();
        }
        if (mCount > 0) {
            for (VideoDownInfo videoDownInfo : videoDownInfos) {
                videoDownInfo.setIsCheck(isSelect);
            }
            adapter.notifyDataSetChanged();
        }
        if (isSelect) {
            return mCount;
        }
        mCount = 0;
        return mCount;
    }

    /**
     * ListView的Check显示或取消
     *
     * @param isShow
     * @return
     */
    public boolean showVideoFileSelect(boolean isShow) {
        if (videoDownInfos != null && videoDownInfos.size() > 0) {
            adapter.isShowCheck(isShow);
            mItemShow = isShow;
            return true;
        }
        return false;
    }

    /**
     * 更新ListView的Item状态
     *
     * @param url
     * @param state
     */
    public void updataFragmentState(String url, int state) {
        adapter.updataState(url, state);
    }

    /**
     * 接收下载进度和下载状态广播,更新ListView
     */
    private class UpdataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Key.ACTION_DOWNLOAD_PROGRESS.equals(intent.getAction())) {
                int progress = intent.getIntExtra("progress", 0);
                String mUrl = intent.getStringExtra("mp4Url");
                int speedSecond = intent.getIntExtra("second", 0);
                // Log.i("进度更新广播","正在接收广播..."+"位置:"+videoId+"---进度:"+progress);
                adapter.updataProgress(mUrl, progress, speedSecond);
            } else if (Key.ACTION_DOWNLOAD_STATE.equals(intent.getAction())) {
                VideoDownInfo info = (VideoDownInfo) intent.getSerializableExtra("videoinfo");
                int state = info.getState();
                String mUrl = info.getVideoMp4Url();
                adapter.updataState(mUrl, state);
                if (state == Key.DOWNLOAD_STATE_COMPLETE) {
                    adapter.deleteCompleteDownload(mUrl);
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("VideoLsFragment", "onDestroy");
        infodao.updataDownloadTaskAll(videoDownInfos);
        getActivity().unregisterReceiver(receiver);
    }
}
