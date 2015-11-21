package com.example.sniffer.httpdownload.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sniffer.httpdownload.bean.FileInfo;
import com.example.sniffer.httpdownload.db.FileDownloadProgressDB;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据库的操作方法
 */
public class FileDownloadProgressDao {

    private static SQLiteOpenHelper mDatabasehelper;
    private static SQLiteDatabase db;
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private static FileDownloadProgressDao dao;

    private FileDownloadProgressDao() {
    }

    public static synchronized void initializeInstance(Context context) {
        if (dao == null) {
            dao = new FileDownloadProgressDao();
            mDatabasehelper = new FileDownloadProgressDB(context);
        }
    }

    public static synchronized FileDownloadProgressDao getDao() {
        return dao;
    }

    public synchronized SQLiteDatabase openWritableDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            db = mDatabasehelper.getWritableDatabase();
        }
        return db;
    }

    public SQLiteDatabase openReadableDatabase() {
        db = mDatabasehelper.getReadableDatabase();
        return db;
    }

    public synchronized void closeDatabase() {
        if (mOpenCounter.incrementAndGet() == 0) {
            db.close();
        }
    }

    /**
     * 获取下载的数据
     */
    public List<FileInfo> getData(String downpath) {
        SQLiteDatabase db = FileDownloadProgressDao.getDao().openReadableDatabase();
        Cursor cursor = db.query("fileprogress", new String[]{"threadid", "datalength",
                        "completelength", "isData", "writeCount"}, "downpath=?",
                new String[]{downpath}, null, null, null);
        List<FileInfo> listdata = new ArrayList<>();
        while (cursor.moveToNext()) {
            FileInfo data = new FileInfo(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
                    cursor.getInt(3), cursor.getInt(4));
            listdata.add(data);
        }
        cursor.close();
        FileDownloadProgressDao.getDao().closeDatabase();
        return listdata;
    }

    /**
     * 获取任务下载进度
     */
    public int getProgress(String downpath) {
        SQLiteDatabase db = FileDownloadProgressDao.getDao().openReadableDatabase();
        Cursor cursor = db.query("fileprogress", new String[]{"completelength"}, "downpath=?",
                new String[]{downpath}, null, null, null);
        int progress = 0;
        while (cursor.moveToNext()) {
            int completelength = cursor.getInt(0);
            progress += completelength;
        }
        cursor.close();
        FileDownloadProgressDao.getDao().closeDatabase();
        return progress;
    }

    /**
     * 添加下载任务
     *
     * @param downpath 下载地址
     * @param listdata 线程id和数据长度的集合
     */
    public void addDownload(String downpath, List<FileInfo> listdata) {
        SQLiteDatabase db = FileDownloadProgressDao.getDao().openWritableDatabase();
        //开始独占模式下的事务
        db.beginTransaction();
        try {
            for (FileInfo data : listdata) {
                ContentValues values = new ContentValues();
                values.put("downpath", downpath);
                values.put("threadid", data.getThreadid());
                values.put("datalength", data.getDatalength());
                values.put("completelength", data.getCompletelength());
                values.put("writeCount", data.getWriteCount());
                db.insert("fileprogress", null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        FileDownloadProgressDao.getDao().closeDatabase();
    }

    /**
     * 实时更新每条线程已经下载的文件长度
     *
     * @param threadid       线程标示
     * @param Completelength 已完成数据
     */
    public void upData(String downpath, int threadid, int Completelength, int writeCount) {
        SQLiteDatabase db = FileDownloadProgressDao.getDao().openWritableDatabase();
        //开启事务
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("completelength", Completelength);
            values.put("writeCount", writeCount);
            db.update("fileprogress", values, "downpath=? and threadid=?", new String[]{downpath, String.valueOf(threadid)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        FileDownloadProgressDao.getDao().closeDatabase();
    }

    /**
     * 更新每条线程是否下载完成
     *
     * @param
     */
    public void upIsData(String downpath, int threadid, int isData) {
        SQLiteDatabase db = FileDownloadProgressDao.getDao().openWritableDatabase();
        //开始独占模式下的事务
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("isData", isData);
            db.update("fileprogress", values, "downpath=? and threadid=?", new String[]{downpath, String.valueOf(threadid)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        FileDownloadProgressDao.getDao().closeDatabase();
    }

    /**
     * 删除数据表的下载任务
     *
     * @param downpath
     */
    public void deletePath(String downpath) {
        SQLiteDatabase db = FileDownloadProgressDao.getDao().openWritableDatabase();
        db.delete("fileprogress", "downpath=?", new String[]{downpath});
        FileDownloadProgressDao.getDao().closeDatabase();
    }

}
