package com.mssm.demoversion.activity;

/**
 * @author Easyhood
 * @desciption 视频播放界面
 * @since 2023/7/10
 **/

import android.app.ActionBar;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mssm.demoversion.R;
import com.mssm.demoversion.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayActivity extends AppCompatActivity implements View.OnClickListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "VideoPlayActivity";

    // 播放视频预览界面
    private SurfaceView surfaceVideo;

    // meidiaplayer对象
    private MediaPlayer mediaPlayer;

    //播放列表
    private List<Uri> playlist;

    private int currentVideoIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        Utils.hideActionBar(this);
        init();
    }

    /**
     * 初始化工作
     */
    private void init() {
        playlist = new ArrayList<>();
        playlist.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssm_1));
        playlist.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssm_2));
        playlist.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssm_3));
        // 播放视频预览界面
        surfaceVideo = findViewById(R.id.surface_video);
        // 设置点击事件
        surfaceVideo.setOnClickListener(this::onClick);
        // meidiaplayer对象
        mediaPlayer = new MediaPlayer();
        currentVideoIndex = 0;
        // 设置准备监听
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        startPlay();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: Easyhood");
    }

    /**
     * 开始播放回调
     *
     * @param mp the MediaPlayer that is ready for playback
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared: Easyhood");
        mediaPlayer.start();
    }

    /**
     * 结束播放回调
     *
     * @param mp the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion: Easyhood");
        playNextVideo();
    }

    /**
     * 开始播放视频
     */
    private void startPlay() {
        Log.d(TAG, "startPlay: Easyhood");
        surfaceVideo.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated: Easyhood");
                //String filePath = new File(getExternalFilesDir(""), "mssm_1.mp4").getAbsolutePath();
                try {
//
                    Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mssm_1);
                    mediaPlayer.setDataSource(getApplicationContext(), uri);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.prepare();
                    mediaPlayer.setDisplay(surfaceVideo.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged: Easyhood");
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed: Easyhood");
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

}