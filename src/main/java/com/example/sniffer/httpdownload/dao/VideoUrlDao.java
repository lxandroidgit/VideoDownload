package com.example.sniffer.httpdownload.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.sniffer.httpdownload.bean.VideoDownInfo;
import com.example.sniffer.httpdownload.db.VideoUrlDB;
import com.example.sniffer.httpdownload.utils.Key;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sniffer on 15-11-6.
 */
public class VideoUrlDao {
    private VideoUrlDB helper;

    public VideoUrlDao(Context context) {
        helper = new VideoUrlDB(context);
    }


    public synchronized void addVideoUrl(VideoDownInfo videoDownInfo) {
        // 获取数据库对象
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", videoDownInfo.getVideoName());
        values.put("mp4url", videoDownInfo.getVideoMp4Url());
        values.put("imageurl", videoDownInfo.getVideoImageUrl());
        values.put("type", 1);
        values.put("time", System.currentTimeMillis());
        db.insert("urls", null, values);
        db.close();
    }

    /**
     * 查询video最大的id
     *
     * @return url总数
     */
    public int findTotalNumber() {
        int maxId = 0;
        // 获取数据库对象
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select max(id)  from urls", null);
        while (cursor.moveToNext()) {
            maxId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return maxId;
    }

    /**
     * 返回最后40个数据
     *
     * @return
     */
    public List<VideoDownInfo> getVideoUrl(int page) {
        // 获取数据库对象
        SQLiteDatabase db = helper.getReadableDatabase();
        List<VideoDownInfo> videoDownInfos = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from urls order by time asc limit 30 offset ?",
                new String[]{String.valueOf(page)});
        while (cursor.moveToNext()) {
            VideoDownInfo videoDownInfo = new VideoDownInfo();
            videoDownInfo.setVideoName(cursor.getString(1));
            videoDownInfo.setVideoMp4Url(Key.VIDEO_ALL_URL + cursor.getString(2));
            videoDownInfo.setVideoImageUrl(Key.VIDEO_ALL_URL + cursor.getString(3));
            videoDownInfo.setType(cursor.getInt(4));
            videoDownInfo.setTime(cursor.getInt(5));
            videoDownInfos.add(videoDownInfo);
        }
        cursor.close();
        db.close();
        return videoDownInfos;
    }

    /**
     * 数据是否存在
     *
     * @param url
     * @return
     */
    public boolean findVideoUrl(String url) {
        // 获取数据库对象
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("urls", null, "mp4url=?", new String[]{
                url}, null, null, null);
        if (cursor.moveToNext()) {
            cursor.close();
            db.close();
            return true;
        }
        cursor.close();
        db.close();
        return false;
    }

}
