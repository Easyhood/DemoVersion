package com.mssm.demoversion.util.cache;


import com.danikula.videocache.HttpProxyCacheServer;
import com.mssm.demoversion.R;
import com.mssm.demoversion.base.BaseApplication;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import xyz.doikki.videoplayer.util.L;

/**
 * @author Easyhood
 * @desciption 原理：主动去请求VideoCache生成的代理地址，触发VideoCache缓存机制
 * 缓存到 PreloadManager.PRELOAD_LENGTH 的数据之后停止请求，完成预加载
 * 播放器去播放VideoCache生成的代理地址的时候，VideoCache会直接返回缓存数据，
 * 从而提升播放速度
 * @since 2023/7/11
 **/
public class PreloadTask implements Runnable {

    /**
     * 原始地址
     */
    public String mRawUrl;

    /**
     * 列表中的位置
     */
    public int mPosition;

    /**
     * VideoCache服务器
     */
    public HttpProxyCacheServer mCacheServer;

    /**
     * 是否被取消
     */
    private boolean mIsCanceled;

    /**
     * 是否正在预加载
     */
    private boolean mIsExecuted;

    private final static List<String> blackList = new ArrayList<>();

    @Override
    public void run() {
        if (!mIsCanceled) {
            start();
        }
        mIsExecuted = false;
        mIsCanceled = false;
    }

    /**
     * 开始预加载
     */
    private void start() {
        // 如果在小黑屋里不加载
        if (blackList.contains(mRawUrl)) return;
        L.i("预加载开始：" + mPosition);
        HttpURLConnection connection = null;
        try {
            //获取HttpProxyCacheServer的代理地址
            String proxyUrl = mCacheServer.getProxyUrl(mRawUrl);
            URL url = new URL(proxyUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5_000);
            connection.setReadTimeout(5_000);
            InputStream in = new BufferedInputStream(connection.getInputStream());
            int length;
            int read = -1;
            byte[] bytes = new byte[8 * 1024];
            while ((length = in.read(bytes)) != -1) {
                read += length;
                //预加载完成或者取消预加载
                if (mIsCanceled || read >= PreloadManager.PRELOAD_LENGTH) {
                    if (mIsCanceled) {
                        L.i(BaseApplication.getInstances().getString(R.string.pre_load_cancle) + mPosition +
                                BaseApplication.getInstances().getString(R.string.pre_read_data) + read + " Byte");
                    } else {
                        L.i(BaseApplication.getInstances().getString(R.string.pre_load_success) + mPosition +
                                BaseApplication.getInstances().getString(R.string.pre_read_data) + read + " Byte");
                    }
                    break;
                }
            }
        } catch (Exception e) {
            L.i(BaseApplication.getInstances().getString(R.string.pre_load_exception) + mPosition +
                    BaseApplication.getInstances().getString(R.string.pre_exception_message) + e.getMessage());
            // 关入小黑屋
            blackList.add(mRawUrl);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            L.i(BaseApplication.getInstances().getString(R.string.pre_load_end) + mPosition);
        }
    }

    /**
     * 将预加载任务提交到线程池，准备执行
     */
    public void executeOn(ExecutorService executorService) {
        if (mIsExecuted) return;
        mIsExecuted = true;
        executorService.submit(this);
    }

    /**
     * 取消预加载任务
     */
    public void cancel() {
        if (mIsExecuted) {
            mIsCanceled = true;
        }
    }
}
