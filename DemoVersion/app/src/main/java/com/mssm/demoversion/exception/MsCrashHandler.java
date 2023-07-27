package com.mssm.demoversion.exception;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.mssm.demoversion.R;
import com.mssm.demoversion.activity.AdvertisePlayActivity;
import com.mssm.demoversion.base.BaseApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Easyhood
 * @desciption 全局异常捕获
 * @since 2023/7/26
 **/
public class MsCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "MsCrashHandler";

    // 系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    //程序的Context对象
    private Context mContext;

    // MsCrashHandler
    private static volatile MsCrashHandler msCrashHandler;

    //保证只有一个MsCrashHandler实例
    private MsCrashHandler() {
    }

    // 获取CrashHandler实例 单例模式 - 双重校验锁
    public static MsCrashHandler getInstance() {
        Log.d(TAG, "getInstance");
        if (msCrashHandler == null) {
            synchronized (MsCrashHandler.class) {
                if (msCrashHandler == null) {
                    msCrashHandler = new MsCrashHandler();
                }
            }
        }
        return msCrashHandler;
    }

    /**
     * 初始化
     *
     * @param ctx
     */
    public void init(Context ctx) {
        Log.d(TAG, "init");
        mContext = ctx;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该MsCrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.d(TAG, "uncaughtException");
        if (!handleExample(e) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理 目的是判断异常是否已经被处理
            mDefaultHandler.uncaughtException(t, e);
        } else {
            try {//Sleep 来让线程停止一会是为了显示Toast信息给用户，然后Kill程序
                Thread.sleep(3000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                Log.d(TAG, "uncaughtException: " + e1.getMessage());
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.d(TAG, "uncaughtException: " + e2.getMessage());
            }
            /** 关闭App 与下面的restartApp重启App保留一个就行 看你需求 **/
            // 如果不关闭程序,会导致程序无法启动,需要完全结束进程才能重新启动
            // android.os.Process.killProcess(android.os.Process.myPid());
            // System.exit(1);
            restartApp();
        }
    }

    /**
     * 自定义错误处理,收集错误信息 将异常信息保存 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleExample(Throwable ex) {
        // 如果已经处理过这个Exception,则让系统处理器进行后续关闭处理
        if (ex == null)
            return false;

        new Thread(() -> {
            // Toast 显示需要出现在一个线程的消息队列中
            Looper.prepare();
            Toast.makeText(mContext, R.string.app_crash_exit,
                    Toast.LENGTH_SHORT).show();
            Looper.loop();
        }).start();

        // 将异常记录到本地的文件中
        saveCrashInfoToFile(ex);
        return true;
    }

    /**
     * 重启应用
     */
    public void restartApp() {
        Intent intent = new Intent(BaseApplication.getInstances().getApplicationContext(),
                AdvertisePlayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     */
    private void saveCrashInfoToFile(Throwable ex) {
        Log.d(TAG, "saveCrashInfoToFile ex : " + ex);
        //获取错误原因
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable exCause = ex.getCause();
        while (exCause != null) {
            exCause.printStackTrace(printWriter);
            exCause = exCause.getCause();
        }
        printWriter.close();

        // 错误日志文件名称
        String fileName = "crash-" + timeStampDate() + ".log";
        Log.d(TAG, "saveCrashInfoToFile: fileName = " + fileName);

        // 判断sd卡可正常使用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //文件存储位置
            String path = Environment.getExternalStorageDirectory().getPath() + "/crash_logInfo/";
            File fl = new File(path);
            //创建文件夹
            if (!fl.exists()) {
                Log.d(TAG, "saveCrashInfoToFile: !fl.exists()");
                fl.mkdirs();
            }
            String saveFilePath = Environment.getExternalStorageDirectory().getPath() +
                    "/crash_logInfo/" + fileName;
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);
                fileOutputStream.write(writer.toString().getBytes());
                fileOutputStream.close();
                Log.d(TAG, "saveCrashInfoToFile: fileOutputStream.close");
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                Log.d(TAG, "saveCrashInfoToFile: e1 = " + e1.getMessage());
            } catch (IOException e2) {
                e2.printStackTrace();
                Log.d(TAG, "saveCrashInfoToFile: e2 = " + e2.getMessage());
            }
        }
    }

    /**
     * 时间戳转换成日期格式字符串
     * 格式 - 2021-08-05 13:59:05
     */
    public String timeStampDate() {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd-HH_mm_dd");
        return sdFormatter.format(nowTime);
    }
}
