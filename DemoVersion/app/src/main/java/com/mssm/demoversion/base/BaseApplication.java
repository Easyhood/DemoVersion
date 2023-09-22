package com.mssm.demoversion.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.mssm.demoversion.exception.MsCrashHandler;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.Utils;
import com.tencent.bugly.crashreport.CrashReport;
import com.youngfeel.yf_rk356x_api.YF_RK356x_API_Manager;

import me.jessyan.autosize.AutoSize;
import xyz.doikki.videoplayer.player.AndroidMediaPlayerFactory;
import xyz.doikki.videoplayer.player.VideoViewConfig;
import xyz.doikki.videoplayer.player.VideoViewManager;

/**
 * @author Easyhood
 * @desciption 基础application类
 * @since 2023/7/11
 **/
public class BaseApplication extends Application {

    private static final String TAG = "BaseApplication";
    public static BaseApplication instances;

    public static Context mContext;

    private static YF_RK356x_API_Manager yfapiManager;

    // private HttpProxyCacheServer proxy;
    public static BaseApplication getInstances() {
        return instances;
    }

    public static Context getContext() {
        return mContext;
    }

    public void onCreate() {
        super.onCreate();
        instances = this;
        mContext = this.getApplicationContext();
        yfapiManager = new YF_RK356x_API_Manager(getInstances());

        AutoSize.initCompatMultiProcess(this);
        initVideoPlay();
        MsCrashHandler.getInstance().init(getInstances().getApplicationContext());
        initBuglySetting();
        Utils.setHomeLauncher();
        setAutoBootApp();
    }

    /**
     * 初始化Bugly设置
     */
    private void initBuglySetting() {
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setDeviceID(yfapiManager.yfgetSerialNumber());
        strategy.setAppChannel(yfapiManager.yfgetSerialNumber());
        strategy.setDeviceModel(yfapiManager.yfgetAndroidDeviceModel());
        strategy.setEnableCatchAnrTrace(true);
        CrashReport.initCrashReport(getApplicationContext(), Constant.BUGLY_APPID, false, strategy);
    }

    /**
     * 初始化视频播放模块
     */
    private void initVideoPlay() {
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                // 使用使用IjkPlayer解码
                // .setPlayerFactory(IjkPlayerFactory.create())
                .setPlayerFactory(AndroidMediaPlayerFactory.create())
                .setLogEnabled(false)
                .build());
    }

    /**
     * 设置开机自启动
     */
    private void setAutoBootApp() {
        Intent intent = new Intent("com.android.yf_set_auto_bootapp");
        intent.putExtra("package","com.mssm.demoversion");
        sendBroadcast(intent);
    }

}
