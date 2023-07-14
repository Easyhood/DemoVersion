package com.mssm.demoversion.view;

/**
 * @author Easyhood
 * @desciption 播放实体类
 * @since 2023/7/11
 **/
public class Advance {

    public String path;//路径  我使用的是本地绝对路径
    public String type;//类型 1、视频 2、图片

    /**
     * 播放实体类
     *
     * @param path 路径  我使用的是本地绝对路径
     * @param type 类型 1、视频 2、图片
     */
    public Advance(String path, String type) {
        this.path = path;
        this.type = type;
    }
}
