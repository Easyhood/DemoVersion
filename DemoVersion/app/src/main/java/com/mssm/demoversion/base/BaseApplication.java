package com.mssm.demoversion.base;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hjq.http.EasyConfig;
import com.hjq.http.config.IRequestInterceptor;
import com.hjq.http.config.IRequestServer;
import com.hjq.http.model.HttpHeaders;
import com.hjq.http.model.HttpParams;
import com.hjq.http.request.HttpRequest;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.mssm.demoversion.BuildConfig;
import com.mssm.demoversion.http.ReleaseServer;
import com.mssm.demoversion.http.RequestHandler;

import me.jessyan.autosize.AutoSize;
import okhttp3.OkHttpClient;
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
        initHttpRequest();
        initFileDownload();
    }

    /**
     * 初始化视频播放模块
     */
    private void initVideoPlay() {
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                //使用使用IjkPlayer解码
                .setPlayerFactory(IjkPlayerFactory.create())
                .setLogEnabled(true)
                .build());
    }

    /**
     * 网络请求初始化
     */
    private void initHttpRequest() {
        IRequestServer server = new ReleaseServer();
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().build();
        EasyConfig.with(okHttpClient)
                // 是否打印日志
                .setLogEnabled(BuildConfig.DEBUG)
                // 设置请求处理策略
                .setHandler(new RequestHandler(this))
                // 设置请求参数拦截器
                .setInterceptor(new IRequestInterceptor() {
                    @Override
                    public void interceptArguments(@NonNull HttpRequest<?> httpRequest,
                                                   @NonNull HttpParams params,
                                                   @NonNull HttpHeaders headers) {
                        headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
                    }
                })
                // 设置请求重试次数
                .setRetryCount(1)
                // 设置请求重试时间
                .setRetryTime(2000)
                // 添加全局请求参数
                .addParam("token", "666666")
                // 添加全局请求头
                // .addHeader("date", "20230712")
                .into();
    }

    private void initFileDownload() {
        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new FileDownloadUrlConnection
                        .Creator(new FileDownloadUrlConnection.Configuration()
                        .connectTimeout(15_000) // set connection timeout.
                        .readTimeout(15_000) // set read timeout.
                ))
                .commit();
    }
}
