package com.example.sniffer.httpdownload.read;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.fragment.VideoShowFragmentOne;
import com.example.sniffer.httpdownload.utils.AutoIO;
import com.example.sniffer.httpdownload.utils.MyApp;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;


/**
 * 开启子线程读取视频图片
 */
public class VideoInfoReadAsyncTask {

    public Context context;
    private LruCache<String, Bitmap> mDrawableCache;
    //   private LruCache<String, String> mSizeCache;
    private GridView mListView;
    private Set<VideoImageAsyncTask> mImageTask;
    private String[] mImageURL;
    //   private Set<VideoSizeAsyncTask> mSizeTask;

    public VideoInfoReadAsyncTask(Context context, GridView mListView, String[] mImageURL) {
        this.context = context;
        this.mListView = mListView;
        this.mImageURL = mImageURL;
        mImageTask = new HashSet<>();
        //       mSizeTask = new HashSet<>();
        //获取最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int imageCacheSize = maxMemory / 4;
        //       int sizeCache = maxMemory / 10;
        mDrawableCache = new LruCache<String, Bitmap>(imageCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                int size = value.getByteCount();
                return size;
            }
        };
    }

    /**
     * 加载从Start到end所有视频信息
     *
     * @param start
     * @param end
     */
    public void loadVideoInfos(int start, int end) {
        if (end > start || end < 0 || start < 0) {
            Log.e(this.context.getPackageName(), start + "\t" + end);
        }
        if (mImageURL.length == 0) {
            return;
        }
        for (int i = start; i < end; i++) {
            String mImageUrl = mImageURL[i];
            Bitmap bitmap = getDrawableFromCache(mImageUrl);
            Drawable Icoon = new BitmapDrawable(MyApp.getContext().getResources(), bitmap);
            if (bitmap == null) {
                VideoImageAsyncTask task = new VideoImageAsyncTask(mImageUrl);
                task.execute(mImageUrl);
                mImageTask.add(task);
            } else {
                ImageView mImageView = (ImageView) mListView.findViewWithTag(mImageUrl);
                mImageView.setBackground(Icoon);
            }
        }
    }

    /**
     * 获取Drawable的内存占用大小
     *
     * @param drawable
     * @return
     */
    public int getDrawableSize(Drawable drawable) {
        int size = 0;
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        if (null != bitmap) {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            size = w * h;
        }
        return size;
    }

    /**
     * 获取缓存中的图片
     *
     * @param url
     * @return
     */
    public Bitmap getDrawableFromCache(String url) {
        return mDrawableCache.get(url);
    }

    /**
     * 将图片加入缓存
     *
     * @param url
     * @param
     */
    public void addDrawableToCache(String url, Bitmap bitmap) {
        mDrawableCache.put(url, bitmap);
    }


    /**
     * 获取视频图片
     *
     * @param mImageView image控件
     * @param url        图片地址
     */
    public void showVideoDrawable(ImageView mImageView, String url) {
        Bitmap bitmap = getDrawableFromCache(url);
        Drawable Icoon = new BitmapDrawable(MyApp.getContext().getResources(), bitmap);
        if (bitmap == null) {
            mImageView.setBackgroundResource(R.mipmap.ic_launcher);
        } else {
            mImageView.setBackground(Icoon);
        }
    }

    public void cancelAllTasks() {
        for (VideoImageAsyncTask task : mImageTask) {
            task.cancel(true);
        }
    }

    /**
     * 子线程获取视频图片
     */
    private class VideoImageAsyncTask extends AsyncTask<String, Void, Bitmap> {
        //     private ImageView mImageView;
        private String mUrl;

        public VideoImageAsyncTask(String mUrl) {
            this.mUrl = mUrl;
            //         this.mImageView = mImageView;
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
            options.inSampleSize = 6;
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
            mAutoIo.closeIO();
            Log.i("图片", "获取图片" + bitmap.getByteCount());
            return bitmap;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = getDrawableData(params[0]);
            addDrawableToCache(mUrl, bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //将Bitmap转换为Drawable
            Drawable Icoon = new BitmapDrawable(MyApp.getContext().getResources(), bitmap);
            ImageView mImageView = (ImageView) mListView.findViewWithTag(mUrl);
            if (mImageView != null && Icoon != null) {
                mImageView.setBackground(Icoon);
            }
            if (bitmap.isRecycled()) {
                bitmap.recycle();
                System.gc();
            }
        }

    }


}
