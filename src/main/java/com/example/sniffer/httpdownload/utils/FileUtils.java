package com.example.sniffer.httpdownload.utils;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.example.sniffer.httpdownload.bean.VideoFileInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件操作工具类
 */
public class FileUtils {
    private static String mSdRootPath = Environment.getExternalStorageDirectory().getPath();

    private static String mDataRootPath = null;

    private static String FOLDER_NAME = "/VideoImage";

    public FileUtils(Context context) {
        mDataRootPath = context.getCacheDir().getPath();
    }

    /**
     * 获取存储Image的目录
     *
     * @return
     */
    private static String getStorageDirectory() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ?
                mSdRootPath + FOLDER_NAME : mDataRootPath + FOLDER_NAME;
    }

    /**
     * 功能：保存图片
     * <p/>
     * getStorageDirectory()有SD卡保存在Sd卡目录，否则保存在手机目录
     * 目录不存在则创建目录
     * separator创建分隔符"/"
     * createNewFile() 创建新文件
     *
     * @param fileName 文件名
     * @param bitmap   图片
     */
    public void savaBitmap(String fileName, Bitmap bitmap) throws IOException {
        if (bitmap == null) {
            return;
        }
        String path = getStorageDirectory();
        File folderFile = new File(path);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        File file = new File(path, File.separator + fileName);
        Log.i("FileUtils", "file:" + file.getPath());
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
    }

    /**
     * 从手机或SD卡获取图片
     *
     * @param fileName
     * @return
     */
    public Bitmap getBitmap(String fileName) {
        File videoImage = new File(getStorageDirectory() + File.separator + fileName);
        if (videoImage.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(videoImage.getPath());
            return bitmap;
        }
        return null;
    }

    /**
     * 判断文件是否存在
     *
     * @param fileName
     * @return
     */
    public boolean isFileExists(String fileName) {
        return new File(getStorageDirectory() + File.separator + fileName).exists();
    }

    /**
     * 获取文件的大小
     *
     * @param fileName
     * @return
     */
    public long getFileSize(String fileName) {
        return new File(getStorageDirectory() + File.separator + fileName).length();
    }

    /**
     * 更改文件名
     *
     * @param src  旧文件
     * @param dest 新文件
     * @return
     */
    public static boolean ranameToNewFile(File src, File dest) {
        boolean isOk = src.renameTo(dest);
        return isOk;
    }

    /**
     * 删除SD卡或者手机的缓存图片和目录
     * isDirectory是否为文件夹
     */
    public static void deleteFile() {
        File dirFile = new File(getStorageDirectory());
        if (!dirFile.exists()) {
            return;
        }
        if (dirFile.isDirectory()) {
            String[] children = dirFile.list();
            for (int i = 0, length = children.length; i < length; i++) {
                new File(dirFile, children[i]).delete();
            }
        }
        dirFile.delete();
    }

    /**
     * 获取目录下所有文件
     *
     * @param FileDirName 文件目录路径
     * @param Type        文件类型
     * @return 文件集合
     */
    public static List<File> getFiledirFiles(String FileDirName, String Type) {
        File fileDir = new File(FileDirName);
        List<File> files = new ArrayList<>();
        if (fileDir.exists() && fileDir.isDirectory()) {
            String[] children = fileDir.list();
            for (int i = 0, length = children.length; i < length; i++) {
                if (children[i].contains(Type)) {
                    files.add(new File(fileDir, children[i]));
                }
            }
        }
        return files;
    }

    public static String getVideoFileTime(String videFileName) {
        String duration = null;
        String[] videoinfos = new String[]{MediaStore.Video.Media.DURATION};
        ContentResolver cr = MyApp.getContext().getContentResolver();
        Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoinfos, "_display_name=?", new String[]{videFileName}, null, null);
        if (cursor.moveToNext()) {
            duration = cursor.getString(0);
        }
        cursor.close();
        return duration;
    }

    /**
     * 获取多媒体库视频时长和名称
     *
     * @return
     */
    public static List<Map<String, String>> getVideoFileTimes() {
        String[] videoinfos = new String[]{MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DISPLAY_NAME};
        List<Map<String, String>> durations = new ArrayList<>();
        ContentResolver cr = MyApp.getContext().getContentResolver();
        Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoinfos, null, null, null, null);
        while (cursor.moveToNext()) {
            Map<String, String> maps = new HashMap<>();
            maps.put(cursor.getString(1), cursor.getString(0));
            durations.add(maps);
        }
        cursor.close();
        return durations;
    }

    /**
     * 删除文件
     *
     * @param videoFile
     */
    public static void deleteVideoFile(File videoFile) {
        if (!videoFile.exists()) {
            return;
        }
        videoFile.delete();
    }

    /**
     * 返回SD卡或者手机的缓存目录大小
     *
     * @return
     */
    public static long getDirFileCacheSize() {
        long dirFilesize = 0;
        File dirFile = new File(getStorageDirectory());
        if (!dirFile.exists()) {
            return dirFilesize;
        }
        if (dirFile.isDirectory()) {
            String[] children = dirFile.list();
            for (int i = 0, length = children.length; i < length; i++) {
                long fileSize = new File(dirFile, children[i]).length();
                dirFilesize += fileSize;
            }
        }
        return dirFilesize;
    }

}
