package com.example.sniffer.httpdownload.bean;

/**
 * File下载信息实体
 */
public class FileInfo {

    private int threadid;//线程标示

    private int datalength;//每份数据

    private int completelength;//已完成数据

    private int isData;//数据对比

    private int writeCount;//写入次数

    public FileInfo(int threadid, int datalength, int completelength, int isData, int writeCount) {
        this.threadid = threadid;
        this.datalength = datalength;
        this.completelength = completelength;
        this.isData = isData;
        this.writeCount = writeCount;
    }

    public int getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(int writeCount) {
        this.writeCount = writeCount;
    }

    public int getIsData() {
        return isData;
    }

    public void setIsData(int isData) {
        this.isData = isData;
    }

    public int getThreadid() {
        return threadid;
    }

    public void setThreadid(int threadid) {
        this.threadid = threadid;
    }

    public int getDatalength() {
        return datalength;
    }

    public void setDatalength(int datalength) {
        this.datalength = datalength;
    }

    public int getCompletelength() {
        return completelength;
    }

    public void setCompletelength(int completelength) {
        this.completelength = completelength;
    }
}
