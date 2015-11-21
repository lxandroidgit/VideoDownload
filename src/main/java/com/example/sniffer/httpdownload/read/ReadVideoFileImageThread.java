package com.example.sniffer.httpdownload.read;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.sniffer.httpdownload.R;
import com.example.sniffer.httpdownload.bean.VideoFileInfo;
import com.example.sniffer.httpdownload.utils.FileUtils;
import com.example.sniffer.httpdownload.utils.MyApp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 读取视频文件图片
 */
public class ReadVideoFileImageThread {
    private LruCache<String, Bitmap> mBitmapCache;
    private ListView mListView;
    private List<VideoFileInfo> videoFileInfos;
    private FileUtils fileUtils;
    private Set<ReadVideoFileImageAsync> asyncs;

    public ReadVideoFileImageThread(Context context, ListView mListView, List<VideoFileInfo> videoFileInfos) {
        this.mListView = mListView;
        this.videoFileInfos = videoFileInfos;
        fileUtils = new FileUtils(context);
        asyncs = new HashSet<>();
        long maxMemory = Runtime.getRuntime().maxMemory();
        int videoFileImageCache = (int) (maxMemory / 10);
        mBitmapCache = new LruCache<String, Bitmap>(videoFileImageCache) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    /**
     * 显示从start到end的图片
     *
     * @param start
     * @param end
     */
    public void loadVideoFileImage(int start, int end) {
        if (videoFileInfos.size() == 0) {
            return;
        }
        for (int i = start; i < end; i++) {
            String videoFileName = videoFileInfos.get(i).getVideoFile().getName();
            String videoFilePath = videoFileInfos.get(i).getVideoFile().getPath();
            Bitmap bitmap = getVideoFileimage(videoFilePath);
            ImageView imageView = (ImageView) mListView.findViewWithTag(videoFilePath);
            if (bitmap == null) {
                String[] videoFileNams = videoFileName.split("\\.");
                bitmap = fileUtils.getBitmap(videoFileNams[0]);
                if (bitmap == null) {
                    ReadVideoFileImageAsync async = new ReadVideoFileImageAsync();
                    async.execute(videoFilePath);
                    asyncs.add(async);
                } else {
                    addVideoFileimage(videoFilePath, bitmap);
                }
            }
            if (imageView != null && bitmap != null) {
                Drawable icon = bitmapToDrawable(bitmap);
                imageView.setBackground(icon);
            }

        }
    }

    /**
     * 图片加入缓存
     *
     * @param videoFilePath 图片标识
     * @param bitmap        图片
     */
    public void addVideoFileimage(String videoFilePath, Bitmap bitmap) {
        mBitmapCache.put(videoFilePath, bitmap);
    }

    /**
     * 获取缓存中的图片
     *
     * @param videoFilePath 图片标识
     * @return
     */
    private Bitmap getVideoFileimage(String videoFilePath) {
        return mBitmapCache.get(videoFilePath);
    }

    /**
     * 显示图片
     *
     * @param imageView
     * @param videoFilePath
     */
    public void getVideoFileImage(ImageView imageView, String videoFilePath) {
        Bitmap bitmap = getVideoFileimage(videoFilePath);
        if (bitmap == null) {
            imageView.setBackgroundResource(R.mipmap.ic_launcher);
        } else {
            Drawable icon = bitmapToDrawable(bitmap);
            imageView.setBackground(icon);
        }
    }

    /**
     * 关闭获取图片线程
     */
    public void closeVideFileAsyncs() {
        for (ReadVideoFileImageAsync async : asyncs) {
            async.cancel(true);
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

    private class ReadVideoFileImageAsync extends AsyncTask<String, Void, Bitmap> {
        private String videoFilePath;

        @Override
        protected Bitmap doInBackground(String... params) {
            videoFilePath = params[0];
            Bitmap bitmap = getVideoFileImage(videoFilePath);
            addVideoFileimage(videoFilePath, bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView mImageView = (ImageView) mListView.findViewWithTag(videoFilePath);
            Drawable icon = bitmapToDrawable(bitmap);
            if (mImageView != null && bitmap != null) {
                mImageView.setBackground(icon);
            }
        }

        /**
         * 获取视频略缩图
         *
         * @param videoPath
         * @return
         */
        public Bitmap getVideoFileImage(String videoPath) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath,
                    MediaStore.Video.Thumbnails.MICRO_KIND);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 100, 60,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            Log.i("图片", "获取图片" + bitmap.getByteCount());
            return bitmap;
        }
    }
}
