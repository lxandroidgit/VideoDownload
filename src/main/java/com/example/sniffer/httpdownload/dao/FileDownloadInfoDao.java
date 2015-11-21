package com.example.sniffer.httpdownload.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.db.FileDownloadInfoDB;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库的操作方法
 */
public class FileDownloadInfoDao {

    private FileDownloadInfoDB helper;

    public FileDownloadInfoDao(Context context) {
        //初始化数据库
        helper = new FileDownloadInfoDB(context);
    }

    /**
     * 添加下载任务
     *
     * @param info
     */
    public void addDownloadTask(VideoDownInfo info) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String name = info.getVideoName();
        String url = info.getVideoMp4Url();
        Integer size = info.getVideosize();
        Integer state = info.getState();
        Integer progcess = info.getVideoProgress();
        values.put("name", name);
        values.put("url", url);
        values.put("size", size);
        values.put("state", state);
        values.put("progcess", progcess);
        db.insert("task", null, values);
        db.close();
    }

    /**
     * 更新下载任务
     *
     * @param info
     */
    public void updataDownloadTask(VideoDownInfo info) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String url = info.getVideoMp4Url();
        Integer state = info.getState();
        Integer progcess = info.getVideoProgress();
        values.put("state", state);
        values.put("progcess", progcess);
        db.update("task", values, "url=?", new String[]{url});
        db.close();
    }

    /**
     * 获取全部的下载任务
     *
     * @return
     */
    public List<VideoDownInfo> getDownloadTaskAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("task", new String[]{"name", "url", "size", "state", "progcess"}, null, null, null, null, null);
        List<VideoDownInfo> mListVideo = new ArrayList<VideoDownInfo>();
        while (cursor.moveToNext()) {
            VideoDownInfo info = new VideoDownInfo();
            info.setVideoName(cursor.getString(0));
            info.setVideoMp4Url(cursor.getString(1));
            info.setVideosize(cursor.getInt(2));
            info.setState(cursor.getInt(3));
            info.setVideoProgress(cursor.getInt(4));
            mListVideo.add(info);
        }
        cursor.close();
        db.close();
        return mListVideo;
    }

    /**
     * 更新全部的下载任务
     *
     * @return
     */
    public void updataDownloadTaskAll(List<VideoDownInfo> mListVideo) {
        SQLiteDatabase db = helper.getWritableDatabase();
        for (VideoDownInfo info : mListVideo) {
            ContentValues values = new ContentValues();
            String url = info.getVideoMp4Url();
            Integer state = info.getState();
            Integer progcess = info.getVideoProgress();
            values.put("state", state);
            values.put("progcess", progcess);
            db.update("task", values, "url=?", new String[]{url});
        }
        db.close();
    }

    /**
     * 寻找下载任务
     *
     * @param url
     * @return
     */
    public boolean findDownloadTask(String url) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("task", null, "url=?", new String[]{url}, null, null, null);
        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }

    /**
     * 更新任务的状态
     *
     * @param url
     * @param state
     */
    public void upDownloadState(String url, Integer state) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("state", state);
        db.update("task", values, "url=?", new String[]{url});
        db.close();
    }

    /**
     * 获取任务的状态
     *
     * @param url
     * @return
     */
    public Integer findDownloadState(String url) {
        Integer state = -1;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("task", new String[]{"state"}, "url=?", new String[]{url}, null, null, null);
        if (cursor.moveToNext()) {
            state = cursor.getInt(0);
        }
        db.close();
        return state;
    }

    /**
     * 更新任务的进度
     *
     * @param url
     * @param progcess
     */
    public void upDownloadProgcess(String url, Integer progcess) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("progcess", progcess);
        db.update("task", values, "url=?", new String[]{url});
        db.close();
    }

    /**
     * 获取全部的下载任务状态
     *
     * @return
     */
    public void upDownloadStateAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("task", new String[]{"state"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put("state", 1);
            db.update("task", values, null, null);
        }
        cursor.close();
        db.close();
    }

    /**
     * 删除下载任务
     * @param info
     */
    public void deleteDownload(VideoDownInfo info) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("task", "url=?", new String[]{info.getVideoMp4Url()});
        db.close();
    }

}
