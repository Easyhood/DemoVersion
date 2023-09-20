package com.mssm.demoversion.util;

import com.mssm.demoversion.presenter.AdDownloadFinishedListener;
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
}
