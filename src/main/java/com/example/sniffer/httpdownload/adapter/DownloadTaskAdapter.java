package com.example.sniffer.httpdownload.adapter;

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.dao.FileDownloadInfoDao;
import com.example.sniffer.httpdownload.dao.FileDownloadProgressDao;
import com.example.sniffer.httpdownload.utils.FileUtils;
import com.example.sniffer.httpdownload.utils.Key;
import com.example.sniffer.httpdownload.utils.SendBroadcestRevice;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 下载列表数据适配器
 */
public class DownloadTaskAdapter extends MyBaseAdapter<VideoDownInfo> {
    private List<VideoDownInfo> videoLists;
    private Map<String, VideoDownInfo> videoMaps;
    private boolean isShowCheck = false;

    public DownloadTaskAdapter(Context context, List<VideoDownInfo> videoDownInfos, int laoyoutId) {
        super(context, videoDownInfos, laoyoutId);
        this.videoLists = videoDownInfos;
        videoMaps = new HashMap();
        for (VideoDownInfo videoDownInfo : videoDownInfos) {
            videoMaps.put(videoDownInfo.getVideoMp4Url(), videoDownInfo);
        }
    }

    @Override
    public void convert(MyViewHolder myViewHolder, VideoDownInfo videoDownInfo, int position) {
        TextView tv_task_State = myViewHolder.getView(R.id.tv_task_State);
        TextView tv_task_scond = myViewHolder.getView(R.id.tv_task_scond);
        ImageView iv_task_icon = myViewHolder.getView(R.id.iv_task_icon);
        TextView tv_task_name = myViewHolder.getView(R.id.tv_task_name);
        TextView tv_task_size = myViewHolder.getView(R.id.tv_task_size);
        ProgressBar pb_task_progcess = myViewHolder.getView(R.id.pb_task_progcess);
        CheckBox cb_delete_select = myViewHolder.getView(R.id.cb_delete_select);
        boolean isCheck = videoDownInfo.isCheck();
        Integer state = videoDownInfo.getState();
        if (isShowCheck) {
            cb_delete_select.setVisibility(View.VISIBLE);
            cb_delete_select.setChecked(isCheck);
        } else {
            cb_delete_select.setVisibility(View.GONE);
        }
        int videoSize = videoDownInfo.getVideosize();
        pb_task_progcess.setMax(videoSize);
        String mp4Size = Formatter.formatFileSize(context, videoSize);
        tv_task_size.setText(mp4Size);
        switch (state) {
            case Key.DOWNLOAD_STATE_READY:
                tv_task_State.setText("准备下载");
                break;
            case Key.DOWNLOAD_STATE_RUN:
                tv_task_State.setText("正在下载");
                int second = videoDownInfo.getSecond();
                String secondSize = Formatter.formatFileSize(context, second);
                tv_task_scond.setText(secondSize + "/s");
                int progcess = videoDownInfo.getVideoProgress();
                pb_task_progcess.setProgress(progcess);
                String progcessSize = Formatter.formatFileSize(context, progcess);
                tv_task_size.setText(progcessSize + "/" + mp4Size);
                break;
            case Key.DOWNLOAD_STATE_PAUSE:
                tv_task_State.setText("暂停");
                break;
            case Key.DOWNLOAD_STATE_COMPLETE:
                tv_task_State.setText("已完成");
                break;
            case Key.DOWNLOAD_STATE_WAIT:
                tv_task_State.setText("等待下载");
                break;
            case Key.DOWNLOAD_STATE_FAILURE:
                tv_task_State.setText("下载失败");
                break;
        }
        if (state == Key.DOWNLOAD_STATE_RUN || state == Key.DOWNLOAD_STATE_READY) {
            tv_task_scond.setVisibility(View.VISIBLE);
        } else {
            tv_task_scond.setVisibility(View.INVISIBLE);
        }
        iv_task_icon.setBackgroundResource(R.mipmap.ic_launcher);
        tv_task_name.setText(videoDownInfo.getVideoName());
        int progcess = videoDownInfo.getVideoProgress();
        pb_task_progcess.setProgress(progcess);

    }

    /**
     * 删除下载
     */
    public void deleSelectTask() {
        File downloadPath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            downloadPath = new File(Environment.getExternalStorageDirectory() + "/VideoDownload/");
        } else {
            Toast.makeText(context, "内存卡不可用！", Toast.LENGTH_SHORT);
        }
        FileDownloadInfoDao infodao = new FileDownloadInfoDao(context);
        FileDownloadProgressDao.initializeInstance(context);
        FileDownloadProgressDao dao = FileDownloadProgressDao.getDao();

        List<VideoDownInfo> deleteLists = new ArrayList<>();
        for (VideoDownInfo videoDownInfo : videoLists) {
            boolean isCheck = videoDownInfo.isCheck();
            if (isCheck) {
                deleteLists.add(videoDownInfo);
            }
        }
        for (VideoDownInfo deleteinfo : deleteLists) {
            int state = deleteinfo.getState();
            String mp4Url = deleteinfo.getVideoMp4Url();
            if (state == Key.DOWNLOAD_STATE_WAIT || state == Key.DOWNLOAD_STATE_READY ||
                    state == Key.DOWNLOAD_STATE_RUN) {
                SendBroadcestRevice.sendServiceRecive(context,
                        Key.ACTION_DOWNLOAD_DELETE, deleteinfo);
            }
            videoLists.remove(deleteinfo);
            videoMaps.remove(mp4Url);
            infodao.deleteDownload(deleteinfo);
            dao.deletePath(mp4Url);
            if (downloadPath.exists()) {
                File videoFile = new File(downloadPath, deleteinfo.getVideoName() + ".download");
                FileUtils.deleteVideoFile(videoFile);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 显示CheckBox
     *
     * @param isShow
     */
    public void isShowCheck(boolean isShow) {
        isShowCheck = isShow;
        notifyDataSetChanged();
    }



    @Override
    public void getDatas(List<VideoDownInfo> mDatas) {

    }

    /**
     * 更新任务下载进度
     *
     * @param url         下载地址
     * @param progress    进度
     * @param speedSecond 每秒的速度
     */
    public void updataProgress(String url, int progress, int speedSecond) {
        VideoDownInfo info = videoMaps.get(url);
        if (info != null) {
            info.setSecond(speedSecond);
            info.setVideoProgress(progress);
            notifyDataSetChanged();
        }
    }

    /**
     * 更新任务的状态
     *
     * @param url   下载地址
     * @param state 任务下载状态
     */
    public void updataState(String url, int state) {
        VideoDownInfo info = videoMaps.get(url);
        if (info != null) {
            info.setState(state);
            notifyDataSetChanged();
        }
    }

    /**
     * 删除已完成任务
     * @param url
     */
    public void deleteCompleteDownload(String url) {
        VideoDownInfo info = videoMaps.get(url);
        if (info != null) {
            videoLists.remove(info);
            videoMaps.remove(url);
            Log.e("videoLists", videoLists.size() + "videoMaps_size:" + videoMaps.size());
            notifyDataSetChanged();
        }
    }

}
