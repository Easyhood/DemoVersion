package com.mssm.demoversion.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.mssm.demoversion.R;
import com.mssm.demoversion.activity.AdvertisePlayActivity;
import com.mssm.demoversion.activity.ScanQRCodeActivity;
import com.mssm.demoversion.base.BaseApplication;
import com.mssm.demoversion.http.HttpRequest;
import com.mssm.demoversion.http.HttpResRequest;
import com.mssm.demoversion.presenter.ApkDownloadFinishedListener;
import com.mssm.demoversion.presenter.ResourceDownloadFinishedListener;
import com.mssm.demoversion.util.CallBackUtils;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.SharedPreferencesUtils;
import com.mssm.demoversion.util.Utils;
import com.mssm.demoversion.view.Advance;
import com.youngfeel.yf_rk356x_api.YF_RK356x_API_Manager;

import java.util.List;

/**
 * @author Easyhood
 * @desciption 守护进程服务
 * @since 2023/7/14
 **/
public class DaemonService extends Service implements ApkDownloadFinishedListener,
        ResourceDownloadFinishedListener {

    private static final String TAG = "DaemonService";

    private static final int NOTIFICATION_ID = 1;
    private String channelId = "null";
    private DestroyReceiver destroyReceiver;

    private Handler handler;

    // 循环请求变量声明
    private Handler cycleHandler;
    private Runnable cycleRunnable;

    // 循环预加载资源请求变量声明
    private Handler cycleResHandler;
    private Runnable cycleResRunnable;

    private HttpRequest httpRequest;

    private HttpResRequest httpResRequest;

    private YF_RK356x_API_Manager yfManager;

    public DaemonService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("com.mssm", "ForegroundService");
        } else {
            channelId = "";
        }
        // create a notification for the service
        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("MSSM")
                .setContentText("This is a persistent service")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        // make the service foreground
        startForeground(NOTIFICATION_ID, notification);
        // create the receiver and the handler
        destroyReceiver = new DestroyReceiver();
        handler = new Handler();
        cycleHandler = new Handler();
        cycleResHandler = new Handler();
        httpRequest = new HttpRequest();
        httpResRequest = new HttpResRequest();
        yfManager = new YF_RK356x_API_Manager(BaseApplication.getContext());
        CallBackUtils.setApkDownloadFinishedListener(this);
        CallBackUtils.setResourceDownloadFinishedListener(this);
        // register the receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ACTION_DESTROYED);
        registerReceiver(destroyReceiver, filter);
        startCycleRequestApk();
        startCycleRequestRes();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // do some work and return START_STICKY to keep the service running
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unregister the receiver and remove any pending tasks
        unregisterReceiver(destroyReceiver);
        handler.removeCallbacksAndMessages(null);
        cycleHandler.removeCallbacks(cycleRunnable);
        cycleResHandler.removeCallbacks(cycleResRunnable);
    }

    /**
     * 创建通知通道
     *
     * @param channelId
     * @param channelName
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public void onApkDownloadFinished(String savePath) {
        LogUtils.d(TAG, "onApkDownloadFinished: savePath = " + savePath);
        if (savePath == null) {
            LogUtils.d(TAG, "return : savePath is null");
            return;
        }
        installApkPackage(savePath);
    }

    @Override
    public void onResourceDownloadFinished(String status) {
        Log.d(TAG, "onResourceDownloadFinished: status = " + status);
        httpResRequest.pushResourceDownloadStatus(status);
    }

    /**
     * a custom receiver to handle broadcasts from the activity
     */
    private class DestroyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(Constant.ACTION_DESTROYED)) {
                // post a delayed task to start the activity after 60 seconds
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startAdvertisePlayActivity(context);
                    }
                }, Constant.DELAY_MILLIS);
            }
        }
    }

    /**
     * 启动广播轮播界面，需要检测栈顶activity是否该界面，防止重复启动
     *
     * @param context Context
     */
    private void startAdvertisePlayActivity(Context context) {
        String topActivityName = Utils.getTopActivityName(context);
        LogUtils.d(TAG, "startAdvertisePlayActivity: topActivityName is " + topActivityName);
        if (ScanQRCodeActivity.class.getName().equals(topActivityName)) {
            return;
        }
        if (!AdvertisePlayActivity.class.getName().equals(topActivityName)) {
            Intent activityIntent = new Intent(context, AdvertisePlayActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        }
    }

    /**
     * 开始循环请求apk线程
     */
    private void startCycleRequestApk() {
        cycleRunnable = new Runnable() {
            @Override
            public void run() {
                httpRequest.requestNewAdApk();
                cycleHandler.postDelayed(cycleRunnable, Constant.DELAY_APK_MILLIS);
            }
        };
        cycleHandler.post(cycleRunnable);
    }

    /**
     * 安装app安装包
     * @param apkPath apk存放路径
     */
    private void installApkPackage(String apkPath) {
        LogUtils.d(TAG, "installApkPackage");
        Intent intent = new Intent();
        intent.setAction("com.android.yf_slient_install");
        intent.putExtra("path",apkPath);
        intent.putExtra("isboot",true);
        sendBroadcast(intent);
    }


    /**
     * 开始循环请求预加载资源线程
     */
    private void startCycleRequestRes() {
        cycleResRunnable = new Runnable() {
            @Override
            public void run() {
                httpResRequest.requestNewResource();
                cycleResHandler.postDelayed(cycleResRunnable, Constant.DELAY_NEW_RESOURCE_MILLIS);
            }
        };
        cycleResHandler.postDelayed(cycleResRunnable, Constant.DELAY_NEW_RESOURCE_MILLIS);
    }
}