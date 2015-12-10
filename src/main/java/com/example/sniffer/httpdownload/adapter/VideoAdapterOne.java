package com.example.sniffer.httpdownload.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.dao.FileDownloadInfoDao;
import com.example.sniffer.httpdownload.read.VideoImageReadThread;
import com.example.sniffer.httpdownload.read.VideoSizeReadThread;
import com.example.sniffer.httpdownload.service.DownloadService;
import com.example.sniffer.httpdownload.utils.Key;

import java.util.List;


/**
 * 视频数据适配器
 */
public class VideoAdapterOne extends MyBaseAdapter<VideoDownInfo> {

    private Context context;
    //private VideoInfoReadAsyncTask mInfoAsyncTask;
    private int start;
    private int end;
    // public static String[] mSizeURL;
    private boolean flag;
    private FileDownloadInfoDao infodao;
    private VideoSizeReadThread mSizeThread;
    private VideoImageReadThread mImageThread;
    private GridView mGridView;
    private List<VideoDownInfo> videoDownInfos;

    public VideoAdapterOne(Context context, List<VideoDownInfo> datas, int laoyoutId, GridView mGridView) {
        super(context, datas, laoyoutId);
        this.context = context;
        this.mGridView = mGridView;
        this.videoDownInfos = datas;
        // mInfoAsyncTask = new VideoInfoReadAsyncTask(context, mGridView, mImageURL);
        mSizeThread = new VideoSizeReadThread(context, mGridView, videoDownInfos);
        mImageThread = new VideoImageReadThread(context, mGridView, videoDownInfos);
        //mSizeURL = new String[datas.size()];
        infodao = new FileDownloadInfoDao(context);
        flag = true;
    }


    @Override
    public void convert(MyViewHolder myViewHolder, final VideoDownInfo videoDownInfo, int position) {
        ImageView iv_down = myViewHolder.getView(R.id.iv_down);
        TextView tv_downsize = myViewHolder.getView(R.id.tv_downsize);
        TextView tv_dataname = myViewHolder.getView(R.id.tv_dataname);
        ImageView iv_download = myViewHolder.getView(R.id.iv_download);

        String mp4Url = videoDownInfo.getVideoMp4Url();
        tv_downsize.setTag(mp4Url);
        tv_downsize.setText("正在获取...");
        //     mInfoAsyncTask.showVideoSize(hanler.tv_downsize, mp4Url);
        mSizeThread.getVideoSize(tv_downsize, mp4Url);
        tv_dataname.setText(videoDownInfo.getVideoName());
        String imageUrl =  videoDownInfo.getVideoImageUrl();
        iv_down.setTag(imageUrl);
        iv_down.setBackgroundResource(R.mipmap.ic_launcher);
        //mInfoAsyncTask.showVideoDrawable(iv_down, imageUrl);
        mImageThread.showVideoDrawable(iv_down, imageUrl);
        iv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoDownInfo.getVideosize() <= 0) {
                    Toast.makeText(context, "添加失败", Toast.LENGTH_SHORT).show();
                } else if (infodao.findDownloadTask(videoDownInfo.getVideoMp4Url())) {
                    Toast.makeText(context, "下载任务已存在", Toast.LENGTH_SHORT).show();
                } else {
                    infodao.addDownloadTask(videoDownInfo);
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.setAction(Key.ACTION_DOWNLOAD_START);
                    intent.putExtra("videoinfo", videoDownInfo);
                    context.startService(intent);
                    Toast.makeText(context, "添加下载任务成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void getDatas(List mDatas) {
        //Log.i("适配器", "mDatas:" + mDatas.size());
    }

    /**
     * 滚动刷新数据
     *
     * @param scrollState
     * @param state
     */
    public void onScrollFlsh(int scrollState, int state) {
        if (scrollState == state) {
            //mInfoAsyncTask.loadVideoInfos(start, end);
            mImageThread.loadVideoInfos(start, end);
            mSizeThread.loadVideoSize(start, end);
            int lastVisiblePosition = mGridView.getLastVisiblePosition();
            Log.i("位置", "最后一条数据：" + lastVisiblePosition);
        } else {
            //mInfoAsyncTask.cancelAllTasks();
            mImageThread.cancelAllTasks();
            mSizeThread.cancelAllTasks();
        }
    }

    /**
     * 进入时加载数据
     *
     * @param firstVisibleItem
     * @param visibleItemCount
     */
    public void onScrollStart(int firstVisibleItem, int visibleItemCount) {
        start = firstVisibleItem;
        end = visibleItemCount + firstVisibleItem;
        if (flag && visibleItemCount > 0) {
            //mInfoAsyncTask.loadVideoInfos(firstVisibleItem, visibleItemCount);
            mImageThread.loadVideoInfos(firstVisibleItem, visibleItemCount);
            mSizeThread.loadVideoSize(firstVisibleItem, visibleItemCount);
            flag = false;
        }
    }


    public void closeTaskAll() {
        //mInfoAsyncTask.cancelAllTasks();
        if (mImageThread != null) {
            mImageThread.cancelAllTasks();
            mImageThread.clearExecutor();
        } else if (mSizeThread != null) {
            mSizeThread.clearExecutor();
        }

    }

    public List<VideoDownInfo> getListDatas() {
        return videoDownInfos;
    }
}


