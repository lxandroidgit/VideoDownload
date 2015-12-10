package com.example.sniffer.httpdownload.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.adapter.VideoFileAdapter;
import com.example.sniffer.httpdownload.bean.VideoFileInfo;
import com.example.sniffer.httpdownload.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 已完成
 */
public class VideoCompleteDownloadFragment extends Fragment {
    private Button btn_duration;
    private List<VideoFileInfo> videoFileInfos;
    private ListView lv_videos;
    private VideoFileAdapter adapter;
    private boolean mItemShow;
    private int mCount = 0;
    private setCompleteTextmCount listener;

    public interface setCompleteTextmCount {
        void onCompleteDownloadmCount(int mCount);
    }

    public void setCompleteTextmCountListener(setCompleteTextmCount listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        String videoFilePath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            videoFilePath = Environment.getExternalStorageDirectory() + "/VideoDownload";
        } else {
            return;
        }
        videoFileInfos = new ArrayList<>();
        List<File> files = FileUtils.getFiledirFiles(videoFilePath, ".mp4");
        List<Map<String, String>> duraitons = FileUtils.getVideoFileTimes();
        for (File file : files) {
            VideoFileInfo videoFileInfo = new VideoFileInfo();
            for (Map<String, String> map : duraitons) {
                String duration = map.get(file.getName());
                if (duration != null && !duration.equals("")) {
                    videoFileInfo.setVideoFileTime(Integer.valueOf(duration));
                }
            }
            videoFileInfo.setVideoFile(file);
            videoFileInfos.add(videoFileInfo);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_complete, null);
        lv_videos = (ListView) view.findViewById(R.id.lv_videos);
        btn_duration = (Button) view.findViewById(R.id.btn_duration);
        btn_duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver cr = getActivity().getContentResolver();
                String[] videoinfos = new String[]{MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.MIME_TYPE};
                Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoinfos, null, null, null, null);
                while (cursor.moveToNext()) {
                    Log.e("video", "video_data:" + cursor.getString(0) + "-video_type:" + cursor.getString(1));
                }
                cursor.close();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new VideoFileAdapter(getActivity(), videoFileInfos, R.layout.item_video, lv_videos);
        lv_videos.setAdapter(adapter);
        lv_videos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoFileInfo videoFileInfo = (VideoFileInfo) lv_videos.getItemAtPosition(position);
                File file = videoFileInfo.getVideoFile();
                if (mItemShow) {
                    if (videoFileInfo.isSelect()) {
                        videoFileInfo.setIsSelect(false);
                        mCount--;
                    } else {
                        videoFileInfo.setIsSelect(true);
                        mCount++;
                    }
                    adapter.notifyDataSetChanged();
                    Log.e("size", mCount + "");
                    if (listener != null) {
                        listener.onCompleteDownloadmCount(mCount);
                    }
                } else {
                    if (file != null) {
                        Log.e("file", "file is path" + file.getPath());
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), "video/mp4");
                        startActivity(intent);
                    }
                }
            }
        });
    }

    /**
     * 删除视频文件
     *
     * @return
     */
    public int deleteVideoFile() {
        if (videoFileInfos.size() > 0 && mCount > 0) {
            List<VideoFileInfo> deletes = new ArrayList<>();
            for (VideoFileInfo videoFileInfo : videoFileInfos) {
                if (videoFileInfo.isSelect()) {
                    deletes.add(videoFileInfo);
                }
            }
            mCount = mCount - deletes.size();
            for (VideoFileInfo videoFileInfo : deletes) {
                videoFileInfo.getVideoFile().delete();
                videoFileInfos.remove(videoFileInfo);
            }
            adapter.notifyDataSetChanged();
        }
        return mCount;
    }

    /**
     * ListView的Check显示或取消
     *
     * @param isShow
     * @return
     */
    public boolean showVideoFileSelect(boolean isShow) {
        if (videoFileInfos != null && videoFileInfos.size() > 0) {
            adapter.isShowCheck(isShow);
            mItemShow = isShow;
            return true;
        }
        return false;
    }

    /**
     * Item全部选择或取消
     *
     * @param isSelect 选中或取消
     */
    public int selectVideoFileAll(boolean isSelect) {
        if (videoFileInfos != null) {
            mCount = videoFileInfos.size();
        }
        if (mCount > 0) {
            for (VideoFileInfo videoFileInfo : videoFileInfos) {
                videoFileInfo.setIsSelect(isSelect);
            }
            adapter.notifyDataSetChanged();
        }
        if (isSelect) {
            return mCount;
        }
        mCount = 0;
        return mCount;
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.e("CompleteFragment", "onStart");
    }

    @Override
    public void onDestroy() {
        adapter.closeVideFileAsyncs();
        Log.e("CompleteFragment", "onDestroy");
        super.onDestroy();
    }
}
