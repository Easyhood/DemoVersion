package com.mssm.demoversion.util;

import com.mssm.demoversion.presenter.DownloadCompletedListener;
import com.mssm.demoversion.presenter.TimerComputedListener;

/**
 * @author Easyhood
 * @desciption 回调中间件
 * @since 2023/7/18
 **/
public class CallBackUtils {
    private static DownloadCompletedListener mListener;

    private static TimerComputedListener timerComputedListener;

    /**
     * 设置下载监听
     *
     * @param listener 监听
     */
    public static void setListener(DownloadCompletedListener listener) {
        mListener = listener;
    }

    /**
     * 下载结束回调函数
     *
     * @param tag 调用参数
     */
    public static void doCallBackMethod(int tag) {
        mListener.completedCallback(tag);
    }

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
}
