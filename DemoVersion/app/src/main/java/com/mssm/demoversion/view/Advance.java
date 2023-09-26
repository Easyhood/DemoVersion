package com.mssm.demoversion.view;

/**
 * @author Easyhood
 * @desciption 播放实体类
 * @since 2023/7/11
 **/
public class Advance {

    public String path;// 路径  我使用的是本地绝对路径
    public String type;// 类型 1、视频 2、图片

    public long playTime;// 播放时间

    /**
     * 播放实体类
     *
     * @param path 路径  我使用的是本地绝对路径
     * @param type 类型 1、视频 2、图片
     */
    public Advance(String path, String type, int playTime) {
        this.path = path;
        this.type = type;
        this.playTime = playTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    @Override
    public String toString() {
        return "Advance{" +
                "path='" + path + '\'' +
                ", type='" + type + '\'' +
                ", playTime=" + playTime +
                '}';
    }
}
