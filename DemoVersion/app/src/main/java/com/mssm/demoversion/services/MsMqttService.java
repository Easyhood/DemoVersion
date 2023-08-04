package com.mssm.demoversion.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.mssm.demoversion.R;
import com.mssm.demoversion.activity.AdvertisePlayActivity;
import com.mssm.demoversion.activity.EndDisplayActivity;
import com.mssm.demoversion.activity.ScanQRCodeActivity;
import com.mssm.demoversion.download.MultiDownload;
import com.mssm.demoversion.model.AdvertiseModel;
import com.mssm.demoversion.model.MqttModel;
import com.mssm.demoversion.presenter.DownloadCompletedListener;
import com.mssm.demoversion.util.CallBackUtils;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.Utils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Easyhood
 * @desciption MQTT服务进程
 * @since 2023/7/14
 **/
public class MsMqttService extends Service implements DownloadCompletedListener {

    private static final String TAG = "MsMqttService";
    private static final int NOTIFICATION_ID = 1001;
    public MqttModel mqttModel;

    private String channelId = "null";
    private Handler mHandler;
    private MqttClient client;
    private MqttConnectOptions options;
    private MultiDownload mMultiDownload;
    private ScheduledExecutorService scheduler;
    private String messageJson;
    private List<BaseDownloadTask> mTask;
    private String localtopBgPath;
    private String localtopFloatPath;

    private boolean isReceived = false;

    //这些都写你自己的或者找个测试的地址
    private String host = "TCP://test-mqtt.woozatop.com:1883";     // TCP协议
    private String userName = "test_server"; //mqtt用户名称
    private String passWord = "test_server";//mqtt用户密码
    private String mqtt_id = "mqttx_9baa666f";//mqtt id
    private String mqtt_sub_topic = "ad_service_to_device_" + Utils.getCapitalDeviceSnNumber();// mqtt订阅的主题的标识
    private String mqtt_pub_topic = "well/1123/0";//mqtt你发布的主题的标识

    public MsMqttService() {
    }

    public MqttModel getMqttModel() {
        return mqttModel;
    }

    public void setMqttModel(MqttModel mqttModel) {
        this.mqttModel = mqttModel;
    }

    /**
     * Mqtt接收回调参数
     */
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            //连接丢失后，一般在这里面进行重连
            LogUtils.d(TAG, "connectionLost : " + getString(R.string.mqtt_connect_lost));
            startReconnect();
            isReceived = false;
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            //subscribe后得到的消息会执行到这里面
            LogUtils.d(TAG, "messageArrived "+ getString(R.string.mqtt_subscribe_message) +" : " + message);
            LogUtils.d(TAG, "messageArrived: isReceived = " + isReceived);
            if (isReceived) {
                return;
            }
            if (!isReceived) {
                isReceived = true;
                protectMqttReceiver();
            }
            try {
                Gson gson = new Gson();
                messageJson = String.valueOf(message.toString().toCharArray());
                messageJson = messageJson.replaceAll("\\s|\\n", "");
                LogUtils.d(TAG, "messageArrived: messageJson = " + messageJson);
                mqttModel = gson.fromJson(messageJson, MqttModel.class);
                setMqttModel(mqttModel);
                LogUtils.d(TAG, "messageArrived: mqttModel.cmdStr = " + mqttModel.getCmdStr());
                if (Constant.DISPLAY_RT_EVENT.equals(mqttModel.getCmdStr())) {
                    startResMultiDownload(mqttModel);
                } else if (Constant.END_RT_EVENT.equals(mqttModel.getCmdStr())) {
                    LogUtils.d(TAG, "messageArrived: getDisplayTime = " + mqttModel.getDisplayTime());
                    if (mqttModel.getDisplayTime() != Constant.INDEX_0) {
                        startToEndDisplay(getApplicationContext());
                    } else {
                        startAdvertisePlayActivity(getApplicationContext());
                    }
                } else {
                    LogUtils.d(TAG, "messageArrived: mqttModel.getCmdStr() is new");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                LogUtils.e(TAG, "messageArrived: exception is " + exception);
                return;
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // publish后会执行到这里
            LogUtils.d(TAG, "deliveryComplete: publish后会执行 =" + token.isComplete());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        CallBackUtils.setListener(this);
        mHandler = new Handler();
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("com.mssm.mqtt",
                    "MQTTForegroundService");
        } else {
            channelId = "";
        }
        // create a notification for the service
        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("MQTT")
                .setContentText("This is a persistent service")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        // make the service foreground
        startForeground(NOTIFICATION_ID, notification);
        mMultiDownload = new MultiDownload();
        mqttInit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * MQTT初始化
     */
    private void mqttInit() {
        try {
            mqtt_id = mqtt_id + System.currentTimeMillis();
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, mqtt_id,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            options.setAutomaticReconnect(true);
            //设置回调
            client.setCallback(mqttCallback);
            startReconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * MQTT连接函数
     */
    private void mqttConnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!client.isConnected() && isConnectIsNormal()) {
                        // 如果还未连接 开始连接
                        // MqttConnectOptions options = null;
                        client.connect(options);
                        Log.d(TAG, "run: " + getString(R.string.mqtt_connect_success));
                        client.subscribe(mqtt_sub_topic, 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: " + getString(R.string.mqtt_connect_fail) + "cause by : " + e);
                }
            }
        }).start();
    }

    /**
     * MQTT重新连接函数
     */
    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    mqttConnect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 订阅函数    (下发任务/命令)
     *
     * @param topic   订阅主题
     * @param message 下发命令
     */
    private void publishMessagePlus(String topic, String message) {
        if (client == null || !client.isConnected()) {
            return;
        }
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());
        try {
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断网络是否连接
     *
     * @return isConnectIsNormal
     */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, getString(R.string.mqtt_network_name) + "：" + name);
            return true;
        } else {
            Log.i(TAG, getString(R.string.mqtt_no_network));
            return false;
        }
    }

    /**
     * 创建通知通道
     *
     * @param channelId   String
     * @param channelName String
     * @return channelId
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void completedCallback(int tag) {
        Log.d(TAG, "completedCallback: tag = " + tag);
        if (Constant.SCANQRCODE_DOWNLOAD == tag) {
            Log.d(TAG, "completedCallback: " + getString(R.string.mqtt_scancode_download));
            startToScanQRCode(getApplicationContext());
        }
    }

    /**
     * 跳转到二维码扫描互动界面
     *
     * @param context Context
     */
    private void startToScanQRCode(Context context) {
        Intent intent = new Intent(context, ScanQRCodeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("bean", messageJson);
        intent.putExtra("localtopBgPath", localtopBgPath);
        intent.putExtra("localtopFloatPath", localtopFloatPath);
        startActivity(intent);
    }

    /**
     * 跳转到二维码扫描互动界面
     *
     * @param context Context
     */
    private void startToEndDisplay(Context context) {
        Intent intent = new Intent(context, EndDisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("bean", messageJson);
        startActivity(intent);
    }

    /**
     * 启动广播轮播界面，需要检测栈顶activity是否该界面，防止重复启动
     *
     * @param context Context
     */
    private void startAdvertisePlayActivity(Context context) {
        String topActivityName = Utils.getTopActivityName(context);
        Log.d(TAG, "startAdvertisePlayActivity: topActivityName is " + topActivityName);
        if (!AdvertisePlayActivity.class.getName().equals(topActivityName)) {
            Intent activityIntent = new Intent(context, AdvertisePlayActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        }
    }

    /**
     * 开始多任务下载
     * @param model 广告实体对象
     */
    private void startResMultiDownload(MqttModel model) {
        Log.d(TAG, "startResMultiDownload");
        mTask = new ArrayList<>();
        mTask.clear();
        String topBgHttpUrlPath = model.getTopLayerModel().getTopBgImageModel().getResUrl();
        localtopBgPath = Utils.checkDownloadFilePath(Utils.getFileName(topBgHttpUrlPath));
        BaseDownloadTask topBgHttpUrlTask = FileDownloader.getImpl().create(topBgHttpUrlPath)
                .setPath(MultiDownload.DOWNLOAD_PATH, true);
        mTask.add(topBgHttpUrlTask);
        String topFloatHttpUrlPath = model.getTopLayerModel().getTopFloatImgModel().getResUrl();
        localtopFloatPath = Utils.checkDownloadFilePath(Utils.getFileName(topFloatHttpUrlPath));
        BaseDownloadTask topFloatHttpUrlTask = FileDownloader.getImpl().create(topFloatHttpUrlPath)
                .setPath(MultiDownload.DOWNLOAD_PATH, true);
        mTask.add(topFloatHttpUrlTask);
        mMultiDownload.start_multi(mTask, Constant.SCANQRCODE_DOWNLOAD);
    }

    private void protectMqttReceiver() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isReceived = false;
            }
        }, Constant.PROTECT_DELAY_TIMES);
    }
}