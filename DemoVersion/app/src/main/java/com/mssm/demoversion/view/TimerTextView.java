package com.mssm.demoversion.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mssm.demoversion.R;
import com.mssm.demoversion.base.BaseApplication;
import com.mssm.demoversion.util.CallBackUtils;
import com.mssm.demoversion.util.Constant;

/**
 * @author Easyhood
 * @desciption 自定义倒计时view
 * @since 2023/7/25
 **/
@SuppressLint("AppCompatCustomView")
public class TimerTextView extends TextView implements Runnable{

    private static final String TAG = "TimerTextView";

    // 是否启动了
    private boolean isRun = false;

    // 秒
    private long mSecond;

    public TimerTextView(Context context) {
        super(context);
    }

    public TimerTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimerTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimerTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 设定倒计时时间
     * @param times 倒计时时间
     */
    public void setTimes(long times) {
        mSecond = times;
    }

    /**
     * 倒计时计算
     */
    private void ComputeTime() {
        mSecond--;
        if (mSecond <= Constant.INDEX_0) {
            mSecond = Constant.INDEX_0;
            CallBackUtils.doTimerComputedCallBackMethod(mSecond);
        }
    }

    /**
     * 获取是否在运行
     * @return isRun
     */
    public boolean getIsRun() {
        return isRun;
    }

    /**
     * 开始运行倒计时
     */
    public void beginRun() {
        this.isRun = true;
        run();
    }

    /**
     * 停止倒计时运行
     */
    public void stopRun(){
        this.isRun = false;
    }

    @Override
    public void run() {
        //标示已经启动
        if(isRun){
            ComputeTime();
            String timeStr= BaseApplication.getInstances().getString(R.string.timer_compute_run) + mSecond;
            this.setText(timeStr);
            postDelayed(this, 1000);
        }else {
            removeCallbacks(this);
        }
    }
}
