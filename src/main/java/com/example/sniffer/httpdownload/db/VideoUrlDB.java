package com.example.sniffer.httpdownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 视频网址数据库
 */
public class VideoUrlDB extends SQLiteOpenHelper {

    public VideoUrlDB(Context context) {
        super(context, "videourl.db", null, 1);
    }

    /**
     * id,name,videourl,imageurl,type,time
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table urls(id integer primary key autoincrement,name varchar(100)," +
                "mp4url varchar(50),imageurl varchar(50),type integer,time integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
