package com.mssm.demoversion.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mssm.demoversion.R;
import com.mssm.demoversion.model.MqttModel;
import com.mssm.demoversion.presenter.TimerComputedListener;
import com.mssm.demoversion.services.MsMqttService;
import com.mssm.demoversion.util.CallBackUtils;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.Utils;
import com.mssm.demoversion.view.TimerTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Easyhood
 * @desciption 扫码互动界面
 * @since 2023/7/14
 **/
public class ScanQRCodeActivity extends AppCompatActivity implements View.OnClickListener,
        TimerComputedListener {

    private static final String TAG = "ScanQRCodeActivity";

    // 播放视频预览界面
    private VideoView videoView;

    private RelativeLayout rlScanQRCode;

    // 背景图片
    private ImageView ivQrCodeBg;

    // 二维码图片
    private ImageView ivQrCode;

    // 倒计时
    private TimerTextView tvTimer;

    private MsMqttService msMqttService;

    private MqttModel mqttModel;

    //播放列表
    private List<Uri> playlist;

    private int currentVideoIndex;

    private long playTimeL;

    private Bitmap qrBitmap;

    private String qrUrl = "888666";

    private int qrWidth = 360;
    private int qrHeight = 360;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);
        CallBackUtils.setTimerComputedListener(this);
        msMqttService = new MsMqttService();
        init();
    }

    /**
     * 初始化工作
     */
    private void init() {
        LogUtils.d(TAG, "init");
        initMqttModel();
        playlist = new ArrayList<>();
        playlist.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_gold_av));
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        // 播放视频预览界面
        videoView = findViewById(R.id.video_view);
        rlScanQRCode = findViewById(R.id.rl_scanqrcode);
        ivQrCodeBg = findViewById(R.id.iv_qrcodebg);
        ivQrCode = findViewById(R.id.iv_qrcode);
        tvTimer = findViewById(R.id.tv_timer);
        ivQrCode.setVisibility(View.INVISIBLE);
        tvTimer.setVisibility(View.INVISIBLE);
        // initTopBgImage();
        // initScanQRCodeImage();
        // 设置点击事件
        String typeEvent = mqttModel.getBgLayerModel().getBgResType();
        if (Constant.VIDEO_TYPE.equals(typeEvent)) {
            ivQrCodeBg.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);
            videoView.setOnClickListener(this::onClick);
            currentVideoIndex = 0;
            startPlay();
        } else {
            videoView.setVisibility(View.INVISIBLE);
            ivQrCodeBg.setVisibility(View.VISIBLE);
            setIvQrCodeBg();
        }
        initQRCode();
        ivQrCode.setVisibility(View.VISIBLE);
        tvTimer.setVisibility(View.VISIBLE);
        tvTimer.setTimes(playTimeL);
        tvTimer.beginRun();
    }

    /**
     * 初始化MqttModel
     */
    private void initMqttModel() {
        LogUtils.d(TAG, "initMqttModel");
        Intent intent = getIntent();
        if (intent == null) {
            LogUtils.d(TAG, "init: intent is null");
            return;
        }
        String mqttModelStr = intent.getStringExtra("bean");
        LogUtils.d(TAG, "init: mqttModelStr = " + mqttModelStr);
        if (mqttModelStr == null) {
            return;
        }
        try {
            Gson gson = new Gson();
            String messageJson = String.valueOf(mqttModelStr.toString().toCharArray());
            messageJson = messageJson.replaceAll("\\s|\\n", "");
            mqttModel = gson.fromJson(messageJson, MqttModel.class);
            playTimeL = Long.valueOf(mqttModel.getDisplayTime() + 1);
        } catch (Exception exception) {
            LogUtils.e(TAG, "initMqttModel: exception is " + exception);
            startAdvertisePlayActivity(this.getApplicationContext());
            finish();
            return;
        }
    }

    /**
     * 初始化二维码
     */
    private void initQRCode() {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, "0");
        BitMatrix encode = null;
        qrUrl = mqttModel.getTopLayerModel().getTopFloatImgModel().getResContent();
        try {
            encode = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);
        } catch (WriterException exception) {
            LogUtils.d(TAG, "initQRCode: exception is " + exception);
            exception.printStackTrace();
        }
        int[] colors = new int[qrWidth * qrHeight];
        for (int i = 0; i < qrWidth; i++) {
            for (int j = 0; j < qrHeight; j++) {
                if (encode.get(i, j)) {
                    colors[i * qrWidth + j] = Color.WHITE;
                } else {
                    colors[i * qrWidth + j] = Constant.TREASURE_DARK_BLACK;
                }
            }
        }
        qrBitmap = Bitmap.createBitmap(colors, qrWidth, qrHeight, Bitmap.Config.RGB_565);
        ivQrCode.setImageBitmap(qrBitmap);
    }

    @Override
    public void onClick(View view) {
        LogUtils.d(TAG, "onClick: Easyhood");
    }

    /**
     * 开始播放视频
     */
    private void startPlay() {
        LogUtils.d(TAG, "startPlay: Easyhood");
        //加载视频资源
        Uri uri = getBoxUri();
        videoView.setVideoURI(uri);
        videoView.start();
        //设置监听
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //回调成功并播放视频
                Log.d(TAG, "setOnPreparedListener");
                mp.start();
                mp.setLooping(true);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        tvTimer.stopRun();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
            videoView.stopPlayback();
            videoView.suspend();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    @Override
    public void ComputedCallBack(long secondTime) {
        LogUtils.d(TAG, "ComputedCallBack " + getString(R.string.timer_compute_end));
        startAdvertisePlayActivity(getApplicationContext());
        finish();
    }

    /**
     * 启动广播轮播界面，需要检测栈顶activity是否该界面，防止重复启动
     *
     * @param context Context
     */
    private void startAdvertisePlayActivity(Context context) {
        String topActivityName = Utils.getTopActivityName(context);
        LogUtils.d(TAG, "startAdvertisePlayActivity: topActivityName is " + topActivityName);
        if (!AdvertisePlayActivity.class.getName().equals(topActivityName)) {
            Intent activityIntent = new Intent(context, AdvertisePlayActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        }
    }

    /**
     * 获取宝箱视频Uri
     *
     * @return boxUri
     */
    private Uri getBoxUri() {
        Uri boxUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_black_av);
        if (mqttModel == null) {
            return boxUri;
        }
        int boxValue = mqttModel.getBgLayerModel().getBgStartResName();
        if (Constant.BOX_BLACK_VALUE == boxValue) {
            boxUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_black_av);
        } else if (Constant.BOX_BLUE_VALUE == boxValue) {
            boxUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_blue_av);
        } else if (Constant.BOX_GOLD_VALUE == boxValue) {
            boxUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_gold_av);
        } else {
            boxUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_black_av);
        }
        return boxUri;
    }

    /**
     * 设置背景图片
     */
    private void setIvQrCodeBg() {
        if (mqttModel == null) {
            return;
        }
        int boxValue = mqttModel.getBgLayerModel().getBgStartResName();
        if (Constant.BOX_BLACK_VALUE == boxValue) {
            ivQrCodeBg.setImageResource(R.drawable.mssq_black_img);
        } else if (Constant.BOX_BLUE_VALUE == boxValue) {
            ivQrCodeBg.setImageResource(R.drawable.mssq_blue_img);
        } else if (Constant.BOX_GOLD_VALUE == boxValue) {
            ivQrCodeBg.setImageResource(R.drawable.mssq_gold_img);
        } else {
            ivQrCodeBg.setImageResource(R.drawable.mssq_black_img);
        }
    }

}