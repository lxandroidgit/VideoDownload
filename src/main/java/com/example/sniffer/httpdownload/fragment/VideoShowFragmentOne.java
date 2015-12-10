package com.example.sniffer.httpdownload.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.View.PullToRefreshGridView;
import com.example.sniffer.httpdownload.activity.ListMp4Activity;
import com.example.sniffer.httpdownload.adapter.VideoAdapterOne;
import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.dao.VideoUrlDao;
import com.example.sniffer.httpdownload.download.InitDataThread;
import com.example.sniffer.httpdownload.utils.FileUtils;
import com.example.sniffer.httpdownload.utils.Key;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 显示视频
 */
public class VideoShowFragmentOne extends Fragment {
    private List<VideoDownInfo> list;
    private PullToRefreshGridView gv_video_one;
    private TextView tv_data_number;
    private VideoAdapterOne adapter;
    private ProgressBar pb_video_show_one;
    private VideoUrlDao videoUrlDao;
    private int lastVisiblePosition;
    private int number = 0;
    private boolean isQuray = true;
    private MyHandler mHandler;

    private class MyHandler extends Handler {
        private WeakReference<ListMp4Activity> myActivity;

        public MyHandler(ListMp4Activity activity) {
            myActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ListMp4Activity activity = myActivity.get();
            Bundle bundle = msg.getData();
            list = (List<VideoDownInfo>) bundle.getSerializable("list");
            if (list != null && list.size() > 0) {
                switch (msg.what) {
                    case Key.UP_VIDEO_DATA:
                        if (list != null && list.size() > 0) {
                            if (adapter == null) {
                                pb_video_show_one.setVisibility(View.INVISIBLE);
                                adapter = new VideoAdapterOne(activity, list, R.layout.item_data, gv_video_one);
                                gv_video_one.setAdapter(adapter);
                            } else {
                                adapter.getListDatas().addAll(list);
                            }
                        }
                        break;
                    case Key.UP_VIDEO_UPDATE:
                        if (adapter != null) {
                            List<VideoDownInfo> videoDownInfos = adapter.getListDatas();
                            for (VideoDownInfo videoDownInfo : list) {
                                if (!videoDownInfos.contains(videoDownInfo)) {
                                    videoDownInfos.add(videoDownInfo);
                                }
                            }
                        }
                        break;
                }
            }
            if (!isQuray) {
                gv_video_one.loadComplete();
                gv_video_one.reflashComplete();
                adapter.notifyDataSetChanged();
                isQuray = true;
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("生命周期", "Fragment——onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("生命周期", "Fragment——onActivityCreated");
        initUI();
        initData();
    }

    private void initData() {
        SharedPreferences sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        Key.VIDEO_ALL_URL = sp.getString("homeUrl", " ");
        Key.totalnumber = videoUrlDao.findTotalNumber();
        if (Key.totalnumber > 0) {
            list = videoUrlDao.getVideoUrl(number);
            Log.i("initData", "list:" + list.size());
            pb_video_show_one.setVisibility(View.INVISIBLE);
            adapter = new VideoAdapterOne(getActivity(), list, R.layout.item_data, gv_video_one);
            gv_video_one.setAdapter(adapter);
            new InitDataThread(getActivity(), mHandler, 1, Key.UP_VIDEO_UPDATE).start();
        } else {
            new InitDataThread(getActivity(), mHandler, 1, Key.UP_VIDEO_DATA).start();
        }
        /**
         * 滚动停止时加载图片和大小
         */
        gv_video_one.setScrollDataInterface(new PullToRefreshGridView.OnScrollDataListener() {
            @Override
            public void onScrollState(int state) {
                if (adapter != null) {
                    adapter.onScrollFlsh(state, AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
                }
            }

            /**
             * GridView初始化数据
             * lastVisiblePosition——屏幕最后数据的位置
             * @param firstVisibleItem
             * @param visibleItemCount
             */
            @Override
            public void onScrolldata(int firstVisibleItem, int visibleItemCount) {
                if (adapter != null) {
                    adapter.onScrollStart(firstVisibleItem, visibleItemCount);
                }

                lastVisiblePosition = gv_video_one.getLastVisiblePosition();
                tv_data_number.setText((lastVisiblePosition + 1) + "/" + Key.totalnumber);
            }

            /**
             * 加载更多数据
             */
            @Override
            public void onLoad() {
                number = number + 30;
                if (Key.totalnumber > 0 && Key.totalnumber > lastVisiblePosition + 1) {
                    final List<VideoDownInfo> videoDownInfos = videoUrlDao.getVideoUrl(number);
                    adapter.getListDatas().addAll(videoDownInfos);
                    adapter.notifyDataSetChanged();
                    gv_video_one.loadComplete();
                } else {
                    if (isQuray) {
                        int page = number / 30 + 1;
                        new InitDataThread(getActivity(),  mHandler, page, Key.UP_VIDEO_DATA).start();
                        isQuray = false;
                    } else {
                        gv_video_one.loadComplete();
                    }
                }
            }
        });
        /**
         * 上拉更新数据
         */
        gv_video_one.setRfreshDataInterface(new PullToRefreshGridView.RfreshDataListener() {
            @Override
            public void onRfreshData() {
                if (isQuray) {
                    new InitDataThread(getActivity(),  mHandler, 1, Key.UP_VIDEO_UPDATE).start();
                    isQuray = false;
                } else {
                    gv_video_one.reflashComplete();
                }
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, null);
        gv_video_one = (PullToRefreshGridView) view.findViewById(R.id.gv_video_one);
        pb_video_show_one = (ProgressBar) view.findViewById(R.id.pb_video_show_one);
        tv_data_number = (TextView) getActivity().findViewById(R.id.tv_data_number);
        LinearLayout head = (LinearLayout) view.findViewById(R.id.header);
        LinearLayout footer = (LinearLayout) view.findViewById(R.id.footer);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        head.addView(gv_video_one.getHeadView(), lp);
        footer.addView(gv_video_one.getFooterView(), lp);
        Log.i("生命周期", "Fragment——onCreateView");
        return view;
    }

    private void initUI() {
        videoUrlDao = new VideoUrlDao(getActivity());
        mHandler = new MyHandler((ListMp4Activity) getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
        Log.i("生命周期", "Fragment——onStart");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            Log.i("TAG", "释放资源");
            adapter.closeTaskAll();
        }
        InitDataThread.setEndThread();
        adapter = null;
        if (FileUtils.getDirFileCacheSize() > Key.IMAGE_CACHE_SIZE) {
            FileUtils.deleteFile();
        }
        Log.i("生命周期", "Fragment——onDestroy");
    }
}
