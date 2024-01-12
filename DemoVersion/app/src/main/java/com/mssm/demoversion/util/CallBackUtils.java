package com.mssm.demoversion.util;

import com.mssm.demoversion.presenter.AdDownloadFinishedListener;
import com.mssm.demoversion.presenter.ApkDownloadFinishedListener;
import com.mssm.demoversion.presenter.EndBgResDownloadFinishedListener;
import com.mssm.demoversion.presenter.ResourceDownloadFinishedListener;
import com.mssm.demoversion.presenter.ScanQRCResDownloadFinishedListener;
import com.mssm.demoversion.presenter.TimerComputedListener;
import com.mssm.demoversion.view.Advance;

import java.util.List;

/**
 * @author Easyhood
 * @desciption 回调中间件
 * @since 2023/7/18
 **/
public class CallBackUtils {

    private static TimerComputedListener timerComputedListener;

    private static AdDownloadFinishedListener adDownloadFinishedListener;

    private static ApkDownloadFinishedListener apkDownloadFinishedListener;

    private static ResourceDownloadFinishedListener resourceDownloadFinishedListener;

    private static EndBgResDownloadFinishedListener endBgResDownloadFinishedListener;

    private static ScanQRCResDownloadFinishedListener scanQRCResDownloadFinishedListener;

    /**
     * 设置倒计时结束监听
     * @param listener 监听
     */
    public static void setTimerComputedListener(TimerComputedListener listener) {
        timerComputedListener = listener;
    }

    /**
     * 倒计时结束回调函数
     * @param secondTime 结束秒数
     */
    public static void doTimerComputedCallBackMethod(long secondTime) {
        timerComputedListener.ComputedCallBack(secondTime);
    }

    /**
     * 设置广告计划下载结束监听
     * @param listener AdDownloadFinishedListener
     */
    public static void setAdDownloadFinishedListener(AdDownloadFinishedListener listener) {
        adDownloadFinishedListener = listener;
    }

    /**
     * 广告计划下载结束返回列表
     * @param successAdvanceList 素材列表
     */
    public static void doAdDownloadFinishedListener(List<Advance> successAdvanceList) {
        adDownloadFinishedListener.onAdDownloadFinished(successAdvanceList);
    }

    /**
     * 设置apk下载结束监听
     * @param listener ApkDownloadFinishedListener
     */
    public static void setApkDownloadFinishedListener(ApkDownloadFinishedListener listener) {
        apkDownloadFinishedListener = listener;
    }

    /**
     * Apk下载结束返回存放路径
     * @param savePath 存放路径
     */
    public static void doApkDownloadFinishedListener(String savePath) {
        apkDownloadFinishedListener.onApkDownloadFinished(savePath);
    }

    /**
     * 设置预加载资源下载结束监听
     * @param listener ResourceDownloadFinishedListener
     */
    public static void setResourceDownloadFinishedListener(ResourceDownloadFinishedListener listener) {
        resourceDownloadFinishedListener = listener;
    }

    /**
     * 预加载资源下载状态
     * @param status 下载状态
     */
    public static void doResourceDownloadFinishedListener(String status) {
        resourceDownloadFinishedListener.onResourceDownloadFinished(status);
    }


    /**
     * 设置结束界面资源下载结束监听
     * @param listener EndBgResDownloadFinishedListener
     */
    public static void setEndBgResDownloadFinishedListener(EndBgResDownloadFinishedListener listener) {
        endBgResDownloadFinishedListener = listener;
    }

    /**
     * 结束界面资源下载状态
     * @param savePath 下载地址
     */
    public static void doEndBgResDownloadFinishedListener(String savePath) {
        endBgResDownloadFinishedListener.onEndBgResDownloadFinished(savePath);
    }

    /**
     * 设置宝箱二维码界面资源下载结束监听
     * @param listener ScanQRCResDownloadFinishedListener
     */
    public static void setScanQRCResDownloadFinishedListener(ScanQRCResDownloadFinishedListener listener) {
        scanQRCResDownloadFinishedListener = listener;
    }

    /**
     * 宝箱二维码界面资源下载状态
     * @param savePath 下载地址
     */
    public static void doScanQRCResDownloadFinishedListener(String savePath) {
        scanQRCResDownloadFinishedListener.onScanQRCResDownloadFinished(savePath);
    }
}
