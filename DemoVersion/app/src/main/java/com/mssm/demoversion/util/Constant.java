package com.mssm.demoversion.util;

import android.os.Environment;

/**
 * @author Easyhood
 * @desciption 命名、链接、地址等工具类
 * @since 2023/7/12
 **/
public class Constant {

    // Bugly的APPID
    public static final String BUGLY_APPID = "e20b968e06";

    public static final String EXIT_PASSWORD = "88888888";

    public static final String EXIT_OLD_PASSWORD = "722405";

    // 播放界面结束广播
    public static final String ACTION_DESTROYED = "com.mssm.action.DESTROYED";

    public static final String ACTION_DELETE_LOG = "com.mssm.action.DELETE_LOG";

    // 广告UUID key
    public static final String AD_UUID_KEY = "AD_UUID_KEY";

    // OTA UUID key
    public static final String OTA_UUID_KEY = "OTA_UUID_KEY";

    // 预加载UUID key
    public static final String NEW_RES_UUID_KEY = "NEW_RES_UUID_KEY";

    // 预加载资源下载状态 - 下载中
    public static final String STATUS_DOWNLOADING = "downloading";

    // 预加载资源下载状态 - 下载完成
    public static final String STATUS_FINISHED = "finished";

    // 请求类型
    // 广告页面下载请求
    public static final int ADVERTISE_DOWNLOAD = 10001;
    // 二维码互动页面下载请求
    public static final int SCANQRCODE_DOWNLOAD = 10002;

    // 二维码定制颜色
    public static final int TREASURE_DARK_BLACK = 0xFF040413;

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

    public static final long PROTECT_DELAY_TIMES = 5000L;
    // 下载未完成
    public static final int DOWNLOAD_NOT = 0;
    // 下载完成
    public static final int DOWNLOAD_COMPLETED = 1;

    public static final long DELAY_MILLIS = 60000L;

    public static final long DELAY_APK_MILLIS = 60*60*1000L;

    public static final long DELAY_NEW_RESOURCE_MILLIS = 47*1000L;

    // 扫码通知标识
    // 扫码播放标识
    public static final String DISPLAY_RT_EVENT = "DISPLAY_RT_EVENT";
    // 扫码播放停止标识
    public static final String END_RT_EVENT = "END_RT_EVENT";

    /**
     * DOWNLOAD_PATH 下载地址
     */
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath() + "/MSSMDownload";

    /**
     * DOWNLOAD_PATH 下载地址
     */
    public static final String APK_DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath() + "/MSSMApkDownload";

    // 数字索引
    public static final int INDEX_0 = 0;
    public static final int INDEX_1 = 1;
    public static final int INDEX_2 = 2;
    public static final int INDEX_3 = 3;
    public static final int INDEX_4 = 4;
    public static final int INDEX_5 = 5;
    public static final int INDEX_7 = 7;
    public static final int INDEX_8 = 8;
    public static final int INDEX_10 = 10;
    public static final int INDEX_14 = 14;
    public static final int INDEX_20 = 20;
    public static final int INDEX_23 = 23;
    public static final int INDEX_24 = 24;

    public static final int INDEX_25 = 25;
    public static final int INDEX_60 = 60;

    public static final int INDEX_301 = 301;
    public static final int INDEX_1000 = 1000;

    public static final int INDEX_10000 = 10000;

    // 宝箱值
    public static final int BOX_BLACK_VALUE = 0;
    public static final int BOX_BLUE_VALUE = 1;
    public static final int BOX_GOLD_VALUE = 2;

    public static final int BOX_OTHER_VALUE = 3;

}
