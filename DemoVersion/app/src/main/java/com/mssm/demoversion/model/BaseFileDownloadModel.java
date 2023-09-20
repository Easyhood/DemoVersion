package com.mssm.demoversion.model;

import com.mssm.demoversion.presenter.MultiFileDownloadListener;

/**
 * @author Easyhood
 * @desciption 基础下载文件模板解析类
 * @since 2023/9/19
 **/
public class BaseFileDownloadModel {

    private String downloadUrl;

    private String saveFilePath;

    private String fileType;

    private String md5Str;

    private int filePlayTime;

    private MultiFileDownloadListener listener;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getSaveFilePath() {
        return saveFilePath;
    }

    public void setSaveFilePath(String saveFilePath) {
        this.saveFilePath = saveFilePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getMd5Str() {
        return md5Str;
    }

    public void setMd5Str(String md5Str) {
        this.md5Str = md5Str;
    }

    public int getFilePlayTime() {
        return filePlayTime;
    }

    public void setFilePlayTime(int filePlayTime) {
        this.filePlayTime = filePlayTime;
    }

    public MultiFileDownloadListener getListener() {
        return listener;
    }

    public void setListener(MultiFileDownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public String toString() {
        return "BaseFileDownloadModel{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", saveFilePath='" + saveFilePath + '\'' +
                ", fileType='" + fileType + '\'' +
                ", md5Str='" + md5Str + '\'' +
                ", filePlayTime=" + filePlayTime +
                ", listener=" + listener +
                '}';
    }
}
