package com.mssm.demoversion.activity;

/**
 * @author Easyhood
 * @desciption 主Activity入口
 * @since 2023/7/10
 **/

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.mssm.demoversion.R;
import com.mssm.demoversion.util.Utils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.checkPermission(this);
        startToPlayView();
    }

    /**
     * 跳转到视频播放页面
     */
    private void startToPlayView() {
        Intent intent = new Intent(this, VideoPlayActivity.class);
        startActivity(intent);
        finish();
    }

}