package com.mssm.demoversion.base;

import android.app.Application;

import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.liulishuo.filedownloader.services.DownloadMgrInitialParams;
import com.mssm.demoversion.exception.MsCrashHandler;

import me.jessyan.autosize.AutoSize;
import xyz.doikki.videoplayer.ijk.IjkPlayerFactory;
import xyz.doikki.videoplayer.player.VideoViewConfig;
import xyz.doikki.videoplayer.player.VideoViewManager;

/**
 * @author Easyhood
 * @desciption 基础application类
 * @since 2023/7/11
 **/
public class BaseApplication extends Application {
    public static BaseApplication instances;

    // private HttpProxyCacheServer proxy;
    public static BaseApplication getInstances() {
        return instances;
    }

    public void onCreate() {
        super.onCreate();
        instances = this;
        AutoSize.initCompatMultiProcess(this);
        initVideoPlay();
        initFileDownload();
        MsCrashHandler.getInstance().init(this.getApplicationContext());
    }

    /**
     * 初始化视频播放模块
     */
    private void initVideoPlay() {
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                // 使用使用IjkPlayer解码
                .setPlayerFactory(IjkPlayerFactory.create())
                .setLogEnabled(true)
                .build());
    }



    private void initFileDownload() {
        FileDownloader.setupOnApplicationOnCreate(getInstances())
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .connectTimeout(15_000) // set connection timeout.
                        .readTimeout(15_000) // set read timeout.
                ))
                .commit();
    }
}
