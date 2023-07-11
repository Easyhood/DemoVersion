package com.mssm.demoversion.activity;

/**
 * @author Easyhood
 * @desciption 视频播放界面
 * @since 2023/7/10
 **/

import android.annotation.SuppressLint;
import android.app.appsearch.AppSearchSchema;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jaeger.library.StatusBarUtil;
import com.mssm.demoversion.R;
import com.mssm.demoversion.util.Utils;
import com.mssm.demoversion.util.cache.PreloadManager;
import com.mssm.demoversion.view.Advance;
import com.mssm.demoversion.view.AdvanceView;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayActivity extends AppCompatActivity {

    private static final String TAG = "VideoPlayActivity";

    private AdvanceView mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        StatusBarUtil.setTranslucentForImageView(this, 0, null);
        Utils.hideActionBar(this);
        initView();
        initData();
        //模拟刷新数据,自行放开
//         new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                initData();
//            }
//        },20000);
    }

    private final List<Advance> data = new ArrayList<>();

    /**
     * 初始化数据
     */

    private void initData() {
        data.clear();
        Advance advance1 = new Advance(getDataPath("mspg_1.jpg"), "2");
        data.add(advance1);
        Advance advance2 = new Advance(getDataPath("mspg_2.jpg"), "2");
        data.add(advance2);
        Advance advance3 = new Advance(getDataPath("mspg_3.jpg"), "2");
        data.add(advance3);
        Advance advance4 = new Advance(getDataPath("mspg_4.jpg"), "2");
        data.add(advance4);
        Advance advance5 = new Advance(getDataPath("mspg_5.jpg"), "2");
        data.add(advance5);
        Advance advance6 = new Advance(getDataPath("mspg_6.jpg"), "2");
        data.add(advance6);
        Advance advance7 = new Advance(getDataPath("mssm_1.mp4"), "1");
        data.add(advance7);
        Advance advance8 = new Advance(getDataPath("mssm_2.mp4"), "1");
        data.add(advance8);
        Advance advance9 = new Advance(getDataPath("mssm_3.mp4"), "1");
        data.add(advance9);
        mViewPager.setData(data);
    }

    /**
     * 获取文件路径
     *
     * @param sourcePath String
     * @return dataUrl
     */
    private String getDataPath(String sourceId) {
        String folderUrl = new File(Environment.getExternalStorageDirectory(), sourceId).
                getAbsolutePath();
        Log.d(TAG, "Easy getDataPath: " + folderUrl);
        return folderUrl;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mViewPager = findViewById(R.id.home_vp);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }


    /**
     * @param str 解决中文路径
     * @return
     */
    public static String toURLString(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt > 255) {
                try {
                    sb.append(URLEncoder.encode(String.valueOf(charAt), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                sb.append(charAt);
            }
        }
        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewPager.setPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.setDestroy();
        PreloadManager.getInstance(this).removeAllPreloadTask();
    }
}