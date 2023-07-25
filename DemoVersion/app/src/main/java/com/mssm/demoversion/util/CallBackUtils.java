package com.mssm.demoversion.util;

import com.mssm.demoversion.presenter.DownloadCompletedListener;

/**
 * @author Easyhood
 * @desciption 回调中间件
 * @since 2023/7/18
 **/
public class CallBackUtils {
    private static DownloadCompletedListener mListener;

    /**
     * 设置监听
     * @param listener 监听
     */
    public static void setListener(DownloadCompletedListener listener) {
        mListener = listener;
    }

    public static void doCallBackMethod(int tag) {
        mListener.completedCallback(tag);
    }
}
