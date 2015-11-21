package com.example.sniffer.httpdownload.bean;


import java.io.Serializable;

/**
 * 视频信息实体
 */
public class VideoDownInfo implements Serializable {

    private String VideoName;

    private String VideoImageUrl;

    private String VideoMp4Url;

    private Integer type;

    private Integer time;

    private Integer Videosize = 0;

    private Integer videoProgress = 0;

    private Integer state = 1;

    private Integer second = 0;

    private boolean isSecond;

    private boolean select;

    private boolean isCheck;

    private boolean showCheck;


    public VideoDownInfo() {
    }

    public VideoDownInfo(String videoName, String videoImage, String videoMp4Url, int type) {
        VideoName = videoName;
        VideoImageUrl = videoImage;
        VideoMp4Url = videoMp4Url;
        this.type=type;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean isShowCheck() {
        return showCheck;
    }

    public void setShowCheck(boolean showCheck) {
        this.showCheck = showCheck;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public boolean isSecond() {
        return isSecond;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public void setIsSecond(boolean isSecond) {
        this.isSecond = isSecond;
    }

    public Integer getSecond() {
        return second;
    }

    public void setSecond(Integer second) {
        this.second = second;
    }

    public Integer getVideoProgress() {
        return videoProgress;
    }


    public void setVideoProgress(Integer videoProgress) {
        this.videoProgress = videoProgress;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getVideosize() {
        return Videosize;
    }

    public void setVideosize(Integer videosize) {
        Videosize = videosize;
    }

    public String getVideoMp4Url() {
        return VideoMp4Url;
    }

    public void setVideoMp4Url(String videoMp4Url) {
        VideoMp4Url = videoMp4Url;
    }


    public String getVideoName() {
        return VideoName;
    }


    public void setVideoName(String videoName) {
        VideoName = videoName;
    }


    public String getVideoImageUrl() {
        return VideoImageUrl;
    }

    public void setVideoImageUrl(String videoImageUrl) {
        VideoImageUrl = videoImageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoDownInfo)) return false;

        VideoDownInfo videoDownInfo = (VideoDownInfo) o;

        if (!VideoName.equals(videoDownInfo.VideoName)) return false;
        if (!VideoImageUrl.equals(videoDownInfo.VideoImageUrl)) return false;
        return VideoMp4Url.equals(videoDownInfo.VideoMp4Url);

    }

    @Override
    public int hashCode() {
        int result = VideoName.hashCode();
        result = 31 * result + VideoImageUrl.hashCode();
        result = 31 * result + VideoMp4Url.hashCode();
        return result;
    }
}
