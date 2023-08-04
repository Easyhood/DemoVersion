package com.mssm.demoversion.activity;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.mssm.demoversion.R;
import com.mssm.demoversion.model.MqttModel;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Easyhood
 * @desciption 互动结束界面
 * @since 2023/7/14
 **/
public class EndDisplayActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, View.OnClickListener{

    private static final String TAG = "EndDisplayActivity";

    private SurfaceView svEndBg;

    private ImageView ivEndBg;

    private TextView tvEndTitle;

    // meidiaplayer对象
    private MediaPlayer mediaPlayer;

    private MqttModel mqttModel;

    //播放列表
    private List<Uri> playlist;

    private int currentVideoIndex;

    private long playTimeL;

    private String endTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_display);
        initView();
        initMqttModel();
        initData();
    }

    /**
     * 初始化View
     */
    private void initView() {
        svEndBg = findViewById(R.id.sv_end_bg);
        ivEndBg = findViewById(R.id.iv_end_bg);
        tvEndTitle = findViewById(R.id.tv_end_title);
        // 设置点击事件
        svEndBg.setOnClickListener(this::onClick);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        playlist = new ArrayList<>();
        playlist.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_gold_av));
        // meidiaplayer对象
        mediaPlayer = new MediaPlayer();
        currentVideoIndex = 0;
        // 设置准备监听
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        if (mqttModel.getBgLayerModel().getBgResType().equals(Constant.VIDEO_TYPE)) {
            // svEndBg.setVisibility(View.VISIBLE);
            svEndBg.setVisibility(View.INVISIBLE);
            ivEndBg.setVisibility(View.VISIBLE);
            // startPlay();
        } else {
            svEndBg.setVisibility(View.INVISIBLE);
            ivEndBg.setVisibility(View.VISIBLE);
            // ivEndBg.setImageResource(R.drawable.mssq_gold_img);
        }
        tvEndTitle.setText(endTitle);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startAdvertisePlayActivity(getApplicationContext());
                finish();
            }
        }, playTimeL);
    }

    /**
     * 初始化MqttModel
     */
    private void initMqttModel() {
        LogUtils.d(TAG, "initMqttModel");
        Intent intent = getIntent();
        if (intent == null) {
            Log.d(TAG, "init: intent is null");
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
            playTimeL = Long.valueOf((mqttModel.getDisplayTime() + 1) * 1000);
            endTitle = mqttModel.getEndText();
        } catch (Exception exception) {
            LogUtils.e(TAG, "initMqttModel: exception is " + exception);
            startAdvertisePlayActivity(this.getApplicationContext());
            finish();
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        LogUtils.d(TAG, "onCompletion: Easyhood");
        playNextVideo();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        LogUtils.d(TAG, "onPrepared: Easyhood");
        mediaPlayer.start();
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
        svEndBg.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                LogUtils.d(TAG, "surfaceCreated: Easyhood");
                //String filePath = new File(getExternalFilesDir(""), "mssm_1.mp4").getAbsolutePath();
                try {
//
                    Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_gold_av);
                    mediaPlayer.setDataSource(getApplicationContext(), uri);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setDisplay(svEndBg.getHolder());
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
            mediaPlayer.setDisplay(svEndBg.getHolder()); // surfaceHolder 是用于显示视频的SurfaceHolder对象
            mediaPlayer.prepareAsync();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
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
        if (!AdvertisePlayActivity.class.getName().equals(topActivityName)) {
            Intent activityIntent = new Intent(context, AdvertisePlayActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        }
    }
}