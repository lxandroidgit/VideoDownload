package com.example.sniffer.httpdownload.bean;

import java.io.File;

/**
 * 视频文件实体
 */
public class VideoFileInfo {
    private File videoFile;
    private int videoFileTime;
    private boolean isSelect;

    public VideoFileInfo() {
    }


    public boolean isSelect() {
        return isSelect;
    }

    public void setIsSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }

    public int getVideoFileTime() {
        return videoFileTime;
    }

    public void setVideoFileTime(int videoFileTime) {
        this.videoFileTime = videoFileTime;
    }
}
