package com.mssm.demoversion.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.core.content.ContextCompat;

import com.mssm.demoversion.activity.AdvertisePlayActivity;

/**
 * @author Easyhood
 * @desciption 开机广播接收类
 * @since 2023/7/13
 **/
public class BootReceiver extends BroadcastReceiver {

    private HandlerThread handlerThread;
    private Handler handler;

    /**
     * 启动轮播界面
     * @param context Context
     */
    private void startAdvertisePlayActivity(Context context) {
        Intent intentMain = new Intent(context, AdvertisePlayActivity.class);
        intentMain.setAction("android.intent.action.MAIN");
        intentMain.addCategory(Intent.CATEGORY_LAUNCHER);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intentMain);
        } catch (Exception ex) {
            android.util.Log.e("BootStartReceiver", "bootStart.startActivity faild", ex);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // better delay some time.
            startAdvertisePlayActivity(context);
        }
    }
}