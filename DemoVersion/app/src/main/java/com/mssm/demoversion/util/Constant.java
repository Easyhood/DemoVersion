package com.mssm.demoversion.util;

/**
 * @author Easyhood
 * @desciption 命名、链接、地址等工具类
 * @since 2023/7/12
 **/
public class Constant {

    // Bugly的APPID
    public static final String BUGLY_APPID = "e20b968e06";

    // 播放界面结束广播
    public static final String ACTION_DESTROYED = "com.mssm.action.DESTROYED";

    // 广告UUID key
    public static final String AD_UUID_KEY = "AD_UUID_KEY";

    // 请求类型
    // 广告页面下载请求
    public static final int ADVERTISE_DOWNLOAD = 10001;
    // 二维码互动页面下载请求
    public static final int SCANQRCODE_DOWNLOAD = 10002;

    // 图片类型
    public static final String IMAGE_TYPE = "image";
    public static final String IMAGE_INDEX = "2";

    // 视频类型
    public static final String VIDEO_TYPE = "video";
    public static final String VIDEO_INDEX = "1";

    /**
     *     handler类型
     */
    // 广告更新时间
    public static final long DELAY_TIMES = 2000L;
    // 下载未完成
    public static final int DOWNLOAD_NOT = 0;
    // 下载完成
    public static final int DOWNLOAD_COMPLETED = 1;

    public static final long DELAY_MILLIS = 32000L;

    // 扫码通知标识
    // 扫码播放标识
    public static final String DISPLAY_RT_EVENT = "DISPLAY_RT_EVENT";
    // 扫码播放停止标识
    public static final String END_RT_EVENT = "END_RT_EVENT";

    // 数字索引
    public static final int INDEX_0 = 0;
    public static final int INDEX_1 = 1;
    public static final int INDEX_2 = 2;

}
