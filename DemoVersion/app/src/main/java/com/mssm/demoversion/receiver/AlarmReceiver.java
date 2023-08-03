package com.mssm.demoversion.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mssm.demoversion.download.MultiDownload;
import com.mssm.demoversion.exception.MsCrashHandler;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.FileUtils;
import com.mssm.demoversion.util.LogUtils;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.d(TAG, "onReceive: action  is " + action);
        if (action.equals(Constant.ACTION_DELETE_LOG)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileUtils.removeFileByTime(LogUtils.LOG_PATH);
                    FileUtils.removeFileByTime(MsCrashHandler.CRASH_PATH);
                    FileUtils.removeFileByTime(MultiDownload.DOWNLOAD_PATH);
                }
            }).start();
        }
    }
}