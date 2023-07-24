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
import com.mssm.demoversion.R;
import com.mssm.demoversion.model.MqttModel;
import com.mssm.demoversion.model.MqttTestModel;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MsMqttService extends Service {

    private static final String TAG = "MsMqttService";
    private static final int NOTIFICATION_ID = 1001;
    public static MqttTestModel mqttTestModel;

    private String channelId = "null";
    private Handler handler;
    private MqttClient client;
    private MqttConnectOptions options;
    private ScheduledExecutorService scheduler;

    //这些都写你自己的或者找个测试的地址
    private String host = "TCP://test-mqtt.woozatop.com:1883";     // TCP协议
    private String userName = "test_server"; //mqtt用户名称
    private String passWord = "test_server";//mqtt用户密码
    private String mqtt_id = "mqttx_9baa666f";//mqtt id
    private String mqtt_sub_topic = "ad_service_to_device";//mqtt订阅的主题的标识
    private String mqtt_pub_topic = "well/1123/0";//mqtt你发布的主题的标识

    public MsMqttService() {
    }

    /**
     * Mqtt接收回调参数
     */
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            //连接丢失后，一般在这里面进行重连
            Log.d(TAG, "connectionLost: 连接丢失");
            startReconnect();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            //subscribe后得到的消息会执行到这里面
            Log.d(TAG, "messageArrived: subscribe后得到的消息 : " + message);
            Gson gson = new Gson();
            String messageJson = String.valueOf(message.toString().toCharArray());
            Log.d(TAG, "messageArrived: messageJson = " + messageJson);
            mqttTestModel = gson.fromJson(messageJson, MqttTestModel.class);
            Log.d(TAG, "messageArrived: mqttModel.cmdStr = " + mqttTestModel.getCmdStr());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //publish后会执行到这里
            Log.d(TAG, "deliveryComplete: publish后会执行 =" + token.isComplete());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("com.mssm.mqtt", "MQTTForegroundService");
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
        mqttInit();
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
                        //如果还未连接 开始连接
                        // MqttConnectOptions options = null;
                        client.connect(options);
                        Log.d(TAG, "run: 连接成功");
                        client.subscribe(mqtt_sub_topic, 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: 连接失败 cause by : " + e);
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
    /* ========================================================================================== */

    /**
     * 判断网络是否连接
     * @return isConnectIsNormal
     */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
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
}