package com.example.sniffer.httpdownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 下载任务数据库
 */
public class FileDownloadInfoDB extends SQLiteOpenHelper {
    public FileDownloadInfoDB(Context context) {
        super(context, "downloadtask.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table task(id integer primary key autoincrement,name varchar(100)," +
                "url varchar(50),size integer,state integer,progcess integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
