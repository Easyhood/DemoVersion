package com.mssm.demoversion.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jaeger.library.StatusBarUtil;
import com.mssm.demoversion.R;
import com.mssm.demoversion.http.HttpRequest;
import com.mssm.demoversion.presenter.DownloadCompletedListener;
import com.mssm.demoversion.services.DaemonService;
import com.mssm.demoversion.util.CallBackUtils;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.Utils;
import com.mssm.demoversion.util.cache.PreloadManager;
import com.mssm.demoversion.view.Advance;
import com.mssm.demoversion.view.AdvanceView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Easyhood
 * @desciption 轮播广告播放界面
 * @since 2023/7/10
 **/
public class AdvertisePlayActivity extends AppCompatActivity implements DownloadCompletedListener {

    private static final String TAG = "AdvertisePlayActivity";



    private AdvanceView mViewPager;

    private Context mContext;

    private HttpRequest httpRequest;

    public Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.DOWNLOAD_COMPLETED:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initDownloadData();
                        }
                    });

                    break;
                default:
                    Log.d(TAG, "handleMessage default");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise_play);
        CallBackUtils.setListener(this);
        mContext = getApplicationContext();
        StatusBarUtil.setTranslucentForImageView(this, 0, null);
        Utils.checkPermission(this);
        Utils.hideActionBar(this);
        initView();
        initData();
        httpRequest = new HttpRequest();
        httpRequest.requestAdvertisePlan();
        Intent intent = new Intent(this, DaemonService.class);
        startForegroundService(intent);
    }

    private final List<Advance> data = new ArrayList<>();

    /**
     * 初始化数据
     */
    private void initData() {
        data.clear();
        addAdvancePhotoData("mspg_1.jpg", R.raw.mspg_1, mContext);
        addAdvancePhotoData("mspg_2.jpg", R.raw.mspg_2, mContext);
        addAdvancePhotoData("mspg_3.jpg", R.raw.mspg_3, mContext);
        addAdvancePhotoData("mspg_4.jpg", R.raw.mspg_4, mContext);
        addAdvancePhotoData("mspg_5.jpg", R.raw.mspg_5, mContext);
        addAdvancePhotoData("mspg_6.jpg", R.raw.mspg_6, mContext);
        addAdvanceVideoData("mssm_1.mp4", R.raw.mssm_1, mContext);
        addAdvanceVideoData("mssm_2.mp4", R.raw.mssm_2, mContext);
        addAdvanceVideoData("mssm_3.mp4", R.raw.mssm_3, mContext);
        mViewPager.setData(data);
    }

    /**
     * 加入到Advance照片数据中
     *
     * @param sourcePath 文件名称
     * @param rawId      文件ID
     * @param context    Context
     */
    private void addAdvancePhotoData(String sourcePath, int rawId, Context context) {
        String path = Utils.checkDefaultFilePath(sourcePath, rawId, context);
        Log.d(TAG, "addAdvanceData: " + path);
        if (path != null) {
            Advance advance = new Advance(path, "2");
            data.add(advance);
        }
    }

    /**
     * 加入到Advance视频数据中
     *
     * @param sourcePath 文件名称
     * @param rawId      文件ID
     * @param context    Context
     */
    private void addAdvanceVideoData(String sourcePath, int rawId, Context context) {
        String path = Utils.checkDefaultFilePath(sourcePath, rawId, context);
        Log.d(TAG, "addAdvanceData: " + path);
        if (path != null) {
            Advance advance = new Advance(path, "1");
            data.add(advance);
        }
    }

    public void initDownloadData(){
        Log.d(TAG, "initDownloadData");
        data.clear();
        addAdvancePhotoData("mspg_6.jpg", R.raw.mspg_6, mContext);
        addAdvanceVideoData("mssm_1.mp4", R.raw.mssm_1, mContext);
        mViewPager.setData(data);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mViewPager = findViewById(R.id.home_vp);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: mViewPager");
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mViewPager.setResume();
        Utils.hideActionBar(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mViewPager.setPause();
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mViewPager.setDestroy();
        PreloadManager.getInstance(this).removeAllPreloadTask();
        mHandler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(Constant.ACTION_DESTROYED);
        sendBroadcast(intent);
    }

    @Override
    public void completedCallback() {
        Log.d(TAG, "completedCallback MultiDownload");
        Message message = new Message();
        message.what = Constant.DOWNLOAD_COMPLETED;
        mHandler.sendMessageDelayed(message, Constant.DELAY_TIMES);
    }
}