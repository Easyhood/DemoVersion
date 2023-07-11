package com.mssm.demoversion.activity;

/**
 * @author Easyhood
 * @desciption 视频播放界面
 * @since 2023/7/10
 **/

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayActivity extends AppCompatActivity {
    private AdvanceView mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        StatusBarUtil.setTranslucentForImageView(this,0,null);
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
    private void initData(){
        data.clear();
        //https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF
        Advance advance1= new Advance(toURLString("https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF").trim(),"2");
        data.add(advance1);
        Advance advance = new Advance(toURLString("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4").trim(),"1");
        data.add(advance);
        Advance advance2 = new Advance("http://vjs.zencdn.net/v/oceans.mp4","1");
        data.add(advance2);
        Advance advance3 = new Advance(toURLString("https://t7.baidu.com/it/u=1415984692,3889465312&fm=193&f=GIF").trim(),"2");
        data.add(advance3);
        mViewPager.setData(data);
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