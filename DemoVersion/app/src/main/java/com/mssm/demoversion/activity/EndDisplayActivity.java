package com.mssm.demoversion.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.mssm.demoversion.R;
import com.mssm.demoversion.model.MqttModel;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Easyhood
 * @desciption 互动结束界面
 * @since 2023/7/14
 **/
public class EndDisplayActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EndDisplayActivity";

    private VideoView videoEndBg;

    private ImageView ivEndBg;

    private TextView tvEndTitle;

    private MqttModel mqttModel;

    private String mSaveFilePath;

    //播放列表
    private List<Uri> playlist;

    private int currentVideoIndex;

    private int bgStartResNameIndex;

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
        videoEndBg = findViewById(R.id.video_end_bg);
        ivEndBg = findViewById(R.id.iv_end_bg);
        tvEndTitle = findViewById(R.id.tv_end_title);
        // 设置点击事件
        videoEndBg.setOnClickListener(this::onClick);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        playlist = new ArrayList<>();
        playlist.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_gold_av));
        currentVideoIndex = 0;
        if (mqttModel.getBgLayerModel().getBgResType().equals(Constant.VIDEO_TYPE)) {
            tvEndTitle.setVisibility(View.VISIBLE);
            videoEndBg.setVisibility(View.VISIBLE);
            ivEndBg.setVisibility(View.INVISIBLE);
            startPlay();
        } else {
            tvEndTitle.setVisibility(View.VISIBLE);
            videoEndBg.setVisibility(View.INVISIBLE);
            ivEndBg.setVisibility(View.VISIBLE);
            setImageBg();
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
        mSaveFilePath = intent.getStringExtra("savePath");
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
            bgStartResNameIndex = mqttModel.getBgLayerModel().getBgStartResName();
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
        if (videoEndBg != null && videoEndBg.isPlaying()) {
            videoEndBg.pause();
            videoEndBg.stopPlayback();
            videoEndBg.suspend();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        LogUtils.d(TAG, "onClick: Easyhood");
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    /**
     * 开始播放视频
     */
    private void startPlay() {
        LogUtils.d(TAG, "startPlay: Easyhood");
        //加载视频资源
        setVideoEndBgRes(videoEndBg);
        videoEndBg.start();
        //设置监听
        videoEndBg.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //回调成功并播放视频
                Log.d(TAG, "setOnPreparedListener");
                mp.start();
                mp.setLooping(true);
            }
        });
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
     * 初始化video资源
     * @param videoEndBg VideoView
     */
    private void setVideoEndBgRes(VideoView videoEndBg) {
        //加载视频资源
        if (bgStartResNameIndex == Constant.INDEX_301) {
            tvEndTitle.setVisibility(View.INVISIBLE);
            videoEndBg.setVideoPath(mSaveFilePath);
        } else {
            Uri uri = getVideoUri();
            videoEndBg.setVideoURI(uri);
        }
    }

    /**
     * 获取视频素材的Uri
     * @return uri 素材的Uri
     */
    private Uri getVideoUri() {
        Uri uri = null;
        if (bgStartResNameIndex == Constant.INDEX_0) {
            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_black_av);
        } else if (bgStartResNameIndex == Constant.INDEX_1) {
            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_blue_av);
        } else if (bgStartResNameIndex == Constant.INDEX_2) {
            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssq_gold_av);
        } else if (bgStartResNameIndex == Constant.INDEX_301) {
            tvEndTitle.setVisibility(View.INVISIBLE);
            uri = Uri.parse(endTitle);
        } else {
            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.end_black_av);
        }
        return uri;
    }


    /**
     * 设置背景图片
     */
    private void setImageBg() {
        if (bgStartResNameIndex == Constant.INDEX_0) {
            ivEndBg.setImageResource(R.drawable.mssq_black_img);
        } else if (bgStartResNameIndex == Constant.INDEX_1) {
            ivEndBg.setImageResource(R.drawable.mssq_blue_img);
        } else if (bgStartResNameIndex == Constant.INDEX_2) {
            ivEndBg.setImageResource(R.drawable.mssq_gold_img);
        } else if (bgStartResNameIndex == Constant.INDEX_301) {
            tvEndTitle.setVisibility(View.INVISIBLE);
            Uri uri = Uri.parse(mSaveFilePath);
            ivEndBg.setImageURI(uri);
        } else {
            ivEndBg.setImageResource(R.color.black);
        }
    }
}