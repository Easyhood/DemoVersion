package com.mssm.demoversion.base;

/**
 * @author Easyhood
 * @desciption 基础application类
 * @since 2023/7/11
 **/

import android.app.Application;

import me.jessyan.autosize.AutoSize;
import xyz.doikki.videoplayer.ijk.IjkPlayerFactory;
import xyz.doikki.videoplayer.player.VideoViewConfig;
import xyz.doikki.videoplayer.player.VideoViewManager;

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
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                //使用使用IjkPlayer解码
                .setPlayerFactory(IjkPlayerFactory.create())
                .setLogEnabled(true)
                .build());
    }
}
