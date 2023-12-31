package com.mssm.demoversion.activity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.jaeger.library.StatusBarUtil;
import com.mssm.demoversion.R;
import com.mssm.demoversion.http.HttpRequest;
import com.mssm.demoversion.presenter.AdDownloadFinishedListener;
import com.mssm.demoversion.services.DaemonService;
import com.mssm.demoversion.services.MsMqttService;
import com.mssm.demoversion.util.CallBackUtils;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.SharedPreferencesUtils;
import com.mssm.demoversion.util.Utils;
import com.mssm.demoversion.util.cache.PreloadManager;
import com.mssm.demoversion.view.Advance;
import com.mssm.demoversion.view.AdvanceView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Easyhood
 * @desciption 轮播广告播放界面
 * @since 2023/7/10
 **/
public class AdvertisePlayActivity extends AppCompatActivity implements AdDownloadFinishedListener {

    private static final String TAG = "AdvertisePlayActivity";

    //定义一个数组，需要监听几次点击事件数组的长度就为多少
    private long[] mHints = new long[Constant.INDEX_8];// 初始全部为0

    private List<Advance> data = new ArrayList<>();

    private AdvanceView mViewPager;

    private Context mContext;

    private HttpRequest httpRequest;

    // 循环请求变量声明
    private Handler cycleHandler;
    private Runnable cycleRunnable;

    //  首次打开应用
    private boolean isFirstOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise_play);
        CallBackUtils.setAdDownloadFinishedListener(this);
        mContext = getApplicationContext();
        isFirstOpen = true;
        httpRequest = new HttpRequest();
        cycleHandler = new Handler();
        StatusBarUtil.setTranslucentForImageView(this, 0, null);
        Utils.checkPermission(this);
        Utils.hideActionBar(this);
        initView();
        initData();
        Intent intent = new Intent(this, DaemonService.class);
        startForegroundService(intent);
        startMqttService();
        regularlyDelete();
    }

    /**
     * 打开MQTT服务
     */
    private void startMqttService() {
        Intent intent = new Intent(this, MsMqttService.class);
        startForegroundService(intent);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        SharedPreferencesUtils.putString(mContext, Constant.AD_UUID_KEY,
                Constant.AD_UUID_KEY);
        initDefaultData();
        startCycleRequest();
    }

    /**
     * 初始化默认数据
     */
    private void initDefaultData() {
        data.clear();
        addAdvancePhotoData("mspg_3.jpg", R.raw.mspg_3, mContext);
        addAdvancePhotoData("mspg_4.jpg", R.raw.mspg_4, mContext);
        addAdvanceVideoData("mssm_1.mp4", R.raw.mssm_1, mContext);
        mViewPager.setData(data);
    }

    /**
     * 开始循环请求线程
     */
    private void startCycleRequest() {
        cycleRunnable = new Runnable() {
            @Override
            public void run() {
                if (isFirstOpen) {
                    SharedPreferencesUtils.putString(getApplicationContext(), Constant.AD_UUID_KEY,
                            Constant.AD_UUID_KEY);
                    isFirstOpen = false;
                }
                httpRequest.requestAdvertisePlan();
                cycleHandler.postDelayed(cycleRunnable, Constant.DELAY_MILLIS);
            }
        };
        cycleHandler.post(cycleRunnable);
    }

    /**
     * 初始化下载数据
     *
     * @param advancesData 广告下载素材计划
     */
    public void initDownloadData(List<Advance> advancesData) {
        if (advancesData == null || advancesData.size() == Constant.INDEX_0) {
            LogUtils.d(TAG, "initDownloadData: advancesData is null");
            return;
        }
        LogUtils.d(TAG, "initDownloadData");
        data.clear();
        data = advancesData;
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
        LogUtils.d(TAG, "addAdvanceData: " + path);
        if (path != null) {
            Advance advance = new Advance(path, Constant.IMAGE_INDEX, 0);
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
        LogUtils.d(TAG, "addAdvanceData: " + path);
        if (path != null) {
            Advance advance = new Advance(path, Constant.VIDEO_INDEX, 5000);
            data.add(advance);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mViewPager = findViewById(R.id.home_vp);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LogUtils.d(TAG, "onTouch: mViewPager");
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume");
        mViewPager.setResume();
        Utils.hideActionBar(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.d(TAG, "onPause");
        mViewPager.setPause();
        Intent intent = new Intent(Constant.ACTION_DESTROYED);
        sendBroadcast(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
        mViewPager.setDestroy();
        PreloadManager.getInstance(this).removeAllPreloadTask();
        cycleHandler.removeCallbacks(cycleRunnable);
        Intent intent = new Intent(Constant.ACTION_DESTROYED);
        sendBroadcast(intent);
    }

    /**
     * 定期删除过期文件
     */
    private void regularlyDelete() {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Constant.INDEX_14);
        calendar.set(Calendar.MINUTE, Constant.INDEX_0);
        calendar.set(Calendar.SECOND, Constant.INDEX_0);
        Intent intent = new Intent(Constant.ACTION_DELETE_LOG);
        PendingIntent pi = PendingIntent.getBroadcast(this, Constant.INDEX_0, intent, PendingIntent.FLAG_IMMUTABLE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                Constant.INDEX_1000 * Constant.INDEX_60 * Constant.INDEX_60 * Constant.INDEX_24, pi);
    }

    @Override
    public void onBackPressed() {
        //将mHints数组内的所有元素左移一个位置
        System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
        //获得当前系统已经启动的时间
        mHints[mHints.length - 1] = SystemClock.uptimeMillis();
        if (SystemClock.uptimeMillis() - mHints[0] <= 5000) {
            DialogSeven();
        }
    }

    /**
     * 返回到系统桌面
     */
    private void startToSystemLauncher() {
        Intent intent = new Intent();
        intent.setClassName("com.android.launcher3",
                "com.android.launcher3.uioverrides.QuickstepLauncher");
        startActivity(intent);
    }

    /**
     * 编辑dialog
     */
    @SuppressLint("StringFormatInvalid")
    public void DialogSeven() {
        final EditText editText = new EditText(AdvertisePlayActivity.this);
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editText.setMaxLines(Constant.INDEX_1);
        editText.setMaxEms(Constant.INDEX_10);
        editText.setMaxWidth(Constant.INDEX_10);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constant.INDEX_10)});
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(AdvertisePlayActivity.this, R.style.MyAlertDialogStyle);
        String exitStr = getResources().getString(R.string.exit_launcher_password);
        exitStr = String.format(exitStr, Utils.getAppVersionName());
        inputDialog.setTitle(exitStr).setView(editText);
        inputDialog.setPositiveButton(getString(R.string.exit_launcher_sure),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String enter = editText.getText().toString();
                        if (Constant.EXIT_PASSWORD.equals(enter) || Constant.EXIT_OLD_PASSWORD.equals(enter)) {
                            startToSystemLauncher();
                        }
                    }
                });
        AlertDialog dialog = inputDialog.create();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = Constant.INDEX_20;
        params.height = Constant.INDEX_20;
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    @Override
    public void onAdDownloadFinished(List<Advance> successAdvanceList) {
        for (int i = 0; i < successAdvanceList.size(); i++) {
            Log.d(TAG, "onAdDownloadFinished: successAdvanceList i = " + i + "; data = "
                    + successAdvanceList.get(i).toString());
        }
        LogUtils.d(TAG, "onAdDownloadFinished");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initDownloadData(successAdvanceList);
            }
        });
    }
}