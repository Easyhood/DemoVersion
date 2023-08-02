package com.mssm.demoversion.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.Utils;
import com.mssm.demoversion.view.TimerTextView;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Easyhood
 * @desciption 扫码互动界面
 * @since 2023/7/14
 **/
public class ScanQRCodeActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener,
 MediaPlayer.OnPreparedListener, View.OnClickListener, TimerComputedListener {

    private static final String TAG = "ScanQRCodeActivity";

    // 播放视频预览界面
    private SurfaceView surfaceVideo;

    private RelativeLayout rlScanQRCode;

    // 背景图片
    private ImageView ivQrCodeBg;

    // 二维码图片
    private ImageView ivQrCode;

    // 倒计时
    private TimerTextView tvTimer;

    // meidiaplayer对象
    private MediaPlayer mediaPlayer;

    private MsMqttService msMqttService;

    private MqttModel mqttModel;

    //播放列表
    private List<Uri> playlist;

    private int currentVideoIndex;

    private long playTimeL;

    // 本地背景图片
    private String localtopBgPath;

    // 本地二维码图片
    private String localtopFloatPath;

    private Bitmap qrBitmap;

    private String qrUrl = "https://test-admin.woozatop.com//ad_files/20230716/281c8692-2fc4-463c-bfac-618956d17566.png";

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
        playlist.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_1));

        // 播放视频预览界面
        surfaceVideo = findViewById(R.id.surface_video);
        rlScanQRCode= findViewById(R.id.rl_scanqrcode);
        ivQrCodeBg = findViewById(R.id.iv_qrcodebg);
        ivQrCodeBg.setVisibility(View.GONE);
        ivQrCode = findViewById(R.id.iv_qrcode);
        tvTimer = findViewById(R.id.tv_timer);
        // initTopBgImage();
        // initScanQRCodeImage();
        // 设置点击事件
        surfaceVideo.setOnClickListener(this::onClick);
        // meidiaplayer对象
        mediaPlayer = new MediaPlayer();
        currentVideoIndex = 0;
        // 设置准备监听
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        startPlay();
        initQRCode();
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
        localtopBgPath = intent.getStringExtra("localtopBgPath");
        localtopFloatPath = intent.getStringExtra("localtopFloatPath");
        LogUtils.d(TAG, "init: mqttModelStr = " + mqttModelStr);
        if (mqttModelStr == null) {
            return;
        }
        Gson gson = new Gson();
        String messageJson = String.valueOf(mqttModelStr.toString().toCharArray());
        messageJson = messageJson.replaceAll("\\s|\\n", "");
        mqttModel = gson.fromJson(messageJson, MqttModel.class);
        playTimeL = Long.valueOf(mqttModel.getDisplayTime() + 1);
    }

    /**
     * 初始化背景图片
     */
    private void initTopBgImage() {
        rlScanQRCode.removeView(ivQrCodeBg);
        Bitmap topBgBitmap = BitmapFactory.decodeFile(localtopBgPath);
        ivQrCodeBg.setImageBitmap(topBgBitmap);
        ivQrCodeBg.setImageAlpha(100);
        RelativeLayout.LayoutParams params = new RelativeLayout.
                LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        rlScanQRCode.addView(ivQrCodeBg);
    }

    /**
     * 初始化二维码
     */
    private void initQRCode() {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix encode = null;
        qrUrl = mqttModel.getTopLayerModel().getTopFloatImgModel().getResUrl();
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
                    colors[i * qrWidth + j] = getColor(R.color.treasure_drak_black);
                }
            }
        }
        qrBitmap = Bitmap.createBitmap(colors, qrWidth, qrHeight, Bitmap.Config.RGB_565);
        ivQrCode.setImageBitmap(qrBitmap);
    }

    /**
     * 初始化二维码图片
     */
    private void initScanQRCodeImage() {
        rlScanQRCode.removeView(ivQrCode);
        Bitmap topFloatBitmap = BitmapFactory.decodeFile(localtopFloatPath);
        if (mqttModel == null) {
            LogUtils.d(TAG, "initScanQRCodeImage: mqttModel is null");
            return;
        }
        int display_width = mqttModel.getTopLayerModel().getTopFloatImgModel().
                getDisplayWidth();
        int display_height = mqttModel.getTopLayerModel().getTopFloatImgModel().
                getDisplayHeight();
        int display_offset_x = mqttModel.getTopLayerModel().getTopFloatImgModel().
                getDisplayOffsetX();
        int display_offset_y = mqttModel.getTopLayerModel().getTopFloatImgModel().
                getDisplayOffsetY();
        RelativeLayout.LayoutParams params = new RelativeLayout.
                LayoutParams(display_width, display_height);
        params.leftMargin = display_offset_x;
        params.topMargin = display_offset_y;
        ivQrCode.setImageBitmap(topFloatBitmap);
        rlScanQRCode.addView(ivQrCode);
    }

    @Override
    public void onClick(View view) {
        LogUtils.d(TAG, "onClick: Easyhood");
    }

    /**
     * 开始播放回调
     *
     * @param mp the MediaPlayer that is ready for playback
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        LogUtils.d(TAG, "onPrepared: Easyhood");
        mediaPlayer.start();
    }

    /**
     * 结束播放回调
     *
     * @param mp the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        LogUtils.d(TAG, "onCompletion: Easyhood");
        // playNextVideo();
    }

    /**
     * 开始播放视频
     */
    private void startPlay() {
        LogUtils.d(TAG, "startPlay: Easyhood");
        surfaceVideo.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                LogUtils.d(TAG, "surfaceCreated: Easyhood");
                //String filePath = new File(getExternalFilesDir(""), "mssm_1.mp4").getAbsolutePath();
                try {
//
                    Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_1);
                    mediaPlayer.setDataSource(getApplicationContext(), uri);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare();
                    mediaPlayer.setDisplay(surfaceVideo.getHolder());
                    mediaPlayer.setLooping(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                LogUtils.d(TAG, "surfaceChanged: Easyhood");
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                LogUtils.d(TAG, "surfaceDestroyed: Easyhood");
            }
        });
    }

    /**
     * 播放下一个视频
     */
    private void playNextVideo() {
        if (currentVideoIndex < playlist.size() - 1) {
            currentVideoIndex++;
        } else {
            currentVideoIndex = 0; // 如果已经是列表中的最后一个视频，回到列表开头
        }

        Uri nextVideoUri = playlist.get(currentVideoIndex);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), nextVideoUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDisplay(surfaceVideo.getHolder()); // surfaceHolder 是用于显示视频的SurfaceHolder对象
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tvTimer.stopRun();
        mediaPlayer.stop();
        mediaPlayer.release();
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

}