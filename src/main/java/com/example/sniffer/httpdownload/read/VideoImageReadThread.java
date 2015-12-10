package com.example.sniffer.httpdownload.read;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.utils.AutoIO;
import com.example.sniffer.httpdownload.utils.FileUtils;
import com.example.sniffer.httpdownload.utils.MyApp;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 子线程获取Video的图片
 */
public class VideoImageReadThread {

    private LruCache<String, Bitmap> mDrawableCache;
    private ThreadPoolExecutor executor;
    private GridView mListView;
    private List<VideoDownInfo> videoDownInfos;
    private Set<VideoImageThread> mImageTask;
    private FileUtils fileUtils;


    public VideoImageReadThread(Context context, GridView mListView, List<VideoDownInfo> videoDownInfos) {
        this.mListView = mListView;
        this.videoDownInfos = videoDownInfos;
        fileUtils = new FileUtils(context);
        mImageTask = new HashSet<>();
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        //获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int imageCacheSize = maxMemory / 8;
        mDrawableCache = new LruCache<String, Bitmap>(imageCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                int size = value.getByteCount();
                return size;
            }
        };
    }

    /**
     * 加载从Start到end所有视频图片
     *
     * @param start
     * @param end
     */
    public void loadVideoInfos(int start, int end) {
    /*    if (end > start || end < 0 || start < 0) {
            Log.e(this.context.getPackageName(), start + "\t" + end);
        }*/
        if (videoDownInfos.size() == 0) {
            return;
        }
        for (int i = start; i < end; i++) {
            String mImageUrl = videoDownInfos.get(i).getVideoImageUrl();
            String mImageName = videoDownInfos.get(i).getVideoName();
            Bitmap bitmap = getDrawableFromCache(mImageUrl);
            ImageView mImageView = (ImageView) mListView.findViewWithTag(mImageUrl);
            if (bitmap == null) {
                bitmap = fileUtils.getBitmap(mImageName);
                if (bitmap == null) {
                    VideoImageThread task = new VideoImageThread(mImageUrl, mImageName);
                    executor.execute(task);
                    mImageTask.add(task);
                } else {
                    addDrawableToCache(mImageUrl, bitmap);
                }
            }
            if (mImageView != null && bitmap != null) {
                Drawable icon = bitmapToDrawable(bitmap);
                mImageView.setBackground(icon);
            }
        }
    }

    /**
     * 移除线程池队列中的任务
     */
    public void cancelAllTasks() {
        for (VideoImageThread task : mImageTask) {
            executor.remove(task);
        }
    }

    /**
     * 关闭线程池
     */
    public void clearExecutor() {
        if (executor != null) {
            if (executor.getActiveCount() > 0) {
                executor.shutdownNow();
            }
            executor = null;
        }
    }


    /**
     * 获取缓存中的图片
     *
     * @param url
     * @return
     */
    public Bitmap getDrawableFromCache(String url) {
        if (null != mDrawableCache) {
            Bitmap bitmap = mDrawableCache.get(url);
            if (url != null) {
                return bitmap;
            }
        }
        return null;
    }

    /**
     * 将图片加入缓存
     *
     * @param url
     * @param
     */
    public void addDrawableToCache(String url, Bitmap bitmap) {
        if (null != mDrawableCache && null == mDrawableCache.get(url)) {
            if (null != url && null != bitmap) {
                mDrawableCache.put(url, bitmap);
            }
        }
    }


    /**
     * 获取视频图片
     *
     * @param mImageView image控件
     * @param url        图片地址
     */
    public void showVideoDrawable(ImageView mImageView, String url) {
        Drawable icon = bitmapToDrawable(getDrawableFromCache(url));
        if (icon == null) {
            mImageView.setBackgroundResource(R.mipmap.ic_launcher);
        } else {
            mImageView.setBackground(icon);
        }
    }

    /**
     * Bitmap转化为Drawable
     *
     * @param bitmap
     * @return
     */
    private Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(MyApp.getContext().getResources(), bitmap);
    }


    /**
     * 读取图片线程
     */
    private class VideoImageThread extends Thread {

        private Handler handler = new Handler();
        private String mUrl;
        private String fileName;

        public VideoImageThread(String mUrl, String fileName) {
            this.mUrl = mUrl;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            final Bitmap bitmap = getDrawableData(mUrl);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //将Bitmap转换为Drawable
                    if (bitmap != null) {
                        addDrawableToCache(mUrl, bitmap);
                        new SavaImageFileCache(fileName, bitmap).start();
                        Drawable icon = bitmapToDrawable(bitmap);
                        ImageView mImageView = (ImageView) mListView.findViewWithTag(mUrl);
                        if (mImageView != null && icon != null) {
                            mImageView.setBackground(icon);
                        }
                        if (bitmap.isRecycled()) {
                            Log.e("bitmap", "bitmap is recycle");
                            bitmap.recycle();
                            System.gc();
                        }
                    }
                }
            });
        }
    }

    /**
     * 读取网络图片
     *
     * @param url 图片的URL
     * @return
     */
    public Bitmap getDrawableData(String url) {
        AutoIO mAutoIo = new AutoIO();
        InputStream in = mAutoIo.openIO(url);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;
        Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
        mAutoIo.closeIO();
        //Log.i("图片", "获取图片" + bitmap.getByteCount());
        return bitmap;
    }

    class SavaImageFileCache extends Thread {
        private String fileName;
        private Bitmap bitmap;

        public SavaImageFileCache(String fileName, Bitmap bitmap) {
            this.fileName = fileName;
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            try {
                fileUtils.savaBitmap(fileName, bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
