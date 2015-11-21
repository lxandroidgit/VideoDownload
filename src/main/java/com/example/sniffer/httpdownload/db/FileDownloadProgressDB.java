package com.example.sniffer.httpdownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 文件下载进度数据库
 */
public class FileDownloadProgressDB extends SQLiteOpenHelper {
    public FileDownloadProgressDB(Context context) {
        super(context, "download.db", null, 1);
    }

    //当数据库被首次创建时执行该方法，一般将创建表等初始化操作在该方法中执行
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table fileprogress(id integer primary key autoincrement," +
                "downpath varchar(100),threadid integer,datalength integer,completelength integer," +
                "isData integer,writeCount integer)");
    }

    //当打开数据库时传入的版本号与当前的版本号不同时会调用该方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
