package com.example.sniffer.httpdownload.adapter;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.bean.VideoFileInfo;
import com.example.sniffer.httpdownload.read.ReadVideoFileImageThread;

import java.io.File;
import java.util.List;

/**
 * 已完成视频
 */
public class VideoFileAdapter extends MyBaseAdapter<VideoFileInfo> implements AbsListView.OnScrollListener {
    private boolean isShowCheck;
    private ReadVideoFileImageThread readVideoFileImageThread;
    private int start;
    private int end;
    private boolean mFlag = true;

    public VideoFileAdapter(Context context, List<VideoFileInfo> datas, int laoyoutId,
                            ListView mListView) {
        super(context, datas, laoyoutId);
        readVideoFileImageThread = new ReadVideoFileImageThread(context, mListView, datas);
        mListView.setOnScrollListener(this);
    }

    @Override
    public void convert(MyViewHolder myViewHolder, VideoFileInfo videoFileInfo, int position) {
        File file = videoFileInfo.getVideoFile();
        ImageView iv_videoimage = myViewHolder.getView(R.id.iv_videoimage);
        TextView tv_videoname = myViewHolder.getView(R.id.tv_videoname);
        TextView tv_videosize = myViewHolder.getView(R.id.tv_videosize);
        TextView tv_videotime = myViewHolder.getView(R.id.tv_videotime);
        CheckBox cb_video_select = myViewHolder.getView(R.id.cb_video_select);
        boolean isSelect = videoFileInfo.isSelect();
        if (isShowCheck) {
            cb_video_select.setVisibility(View.VISIBLE);
            cb_video_select.setChecked(isSelect);
        } else {
            cb_video_select.setVisibility(View.GONE);
        }
        String fileName = file.getName();
        tv_videoname.setText(fileName);
        String videSize = android.text.format.Formatter.formatFileSize(context, file.length());
        int seconds = (videoFileInfo.getVideoFileTime() / 1000) % 60;
        int duration = videoFileInfo.getVideoFileTime() / 1000 / 60;
        tv_videotime.setText("时长:" + duration + "分" + seconds + "秒");
        tv_videosize.setText(videSize);
        iv_videoimage.setTag(file.getPath());
        readVideoFileImageThread.getVideoFileImage(iv_videoimage, file.getPath());
    }

    @Override
    public void getDatas(List<VideoFileInfo> mDatas) {

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
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            readVideoFileImageThread.loadVideoFileImage(start, end);
        } else {
            readVideoFileImageThread.closeVideFileAsyncs();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        start = firstVisibleItem;
        end = visibleItemCount + firstVisibleItem;
        if (mFlag && visibleItemCount > 0) {
            readVideoFileImageThread.loadVideoFileImage(start, end);
            mFlag = false;
        }
    }

    public void closeVideFileAsyncs() {
        readVideoFileImageThread.closeVideFileAsyncs();
    }
}
