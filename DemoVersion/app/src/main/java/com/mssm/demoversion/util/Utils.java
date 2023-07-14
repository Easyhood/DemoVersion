package com.mssm.demoversion.util;

import static android.content.Context.ACTIVITY_SERVICE;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.youngfeel.yf_rk356x_api.YF_RK356x_API_Manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author Easyhood
 * @desciption 工具类
 * @since 2023/7/10
 **/
public class Utils {
    private static final String TAG = "Utils";

    private static YF_RK356x_API_Manager yfapiManager;

    /**
     * 检查应用权限
     *
     * @param activity Activity
     * @return false
     */
    public static boolean checkPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            }, 1);

        }
        return false;
    }

    /**
     * 隐藏状态栏和导航栏
     */
    public static void hideActionBar(Activity activity) {
        yfapiManager = new YF_RK356x_API_Manager(activity.getApplicationContext());
        yfapiManager.yfsetNavigationBarVisibility(false);
        yfapiManager.yfsetStatusBarDisplay(false);
        View decorView = activity.getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    /**
     * 判断某个文件是否存在
     *
     * @param sourcePath 文件绝对路径
     * @return 文件是否存在
     */
    public static boolean isFileExists(String sourcePath) {
        File file = new File("/storage/emulated/0/MSSMDefault", sourcePath);
        if (!file.getParentFile().exists()) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 拷贝raw资源到外部存储
     * @param sourcePath 文件路径
     * @param rawId 资源ID
     * @param context Context
     * @return 文件绝对路径
     */
    public static String copyRawFileToExDir(String sourcePath, int rawId, Context context) {
        File dstFile = new File("/storage/emulated/0/MSSMDefault", sourcePath);
        File parentDir = dstFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }
        InputStream mIs = null;
        OutputStream mOs = null;
        try {
            // 获取res/raw目录下的test.mp4文件的输入流
            mIs = context.getResources().openRawResource(rawId);
            // 获取外部存储路径下的test.mp4文件的输出流
            mOs = new FileOutputStream(dstFile);
            // 把输入流的内容复制到输出流
            byte[] buffer = new byte[1024];
            int len;
            while ((len = mIs.read(buffer)) > 0) {
                mOs.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeInAndOut(mIs, mOs);
            return dstFile.getAbsolutePath();
        }

    }

    /**
     * 关闭流
     * @param mIs 输入流
     * @param mOs 输出流
     */
    public static void closeInAndOut(InputStream mIs, OutputStream mOs) {
        try {
            if (mIs != null) {
                mIs.close();
                mIs = null;
            }
            if (mOs != null) {
                mOs.close();
                mOs = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 检查默认文件路径
     * @param sourcePath 文件名
     * @param rawId 文件id
     * @param context Context
     * @return 文件路径
     */
    public static String checkDefaultFilePath(String sourcePath, int rawId, Context context) {
        String path = null;
        File dstFile = new File("/storage/emulated/0/MSSMDefault", sourcePath);
        boolean isFileExists = isFileExists(sourcePath);
        Log.d(TAG, "checkDefaultFilePath: isFileExists = " + isFileExists);
        if (!isFileExists) {
            path = copyRawFileToExDir(sourcePath, rawId, context);
        } else {
            path = dstFile.getAbsolutePath();
        }
        return path;
    }

    /**
     * @param str 解决中文路径
     * @return sb.toString()
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

    /**
     * 获取栈顶activity名字
     * @param context Context
     * @return topActivity
     */
    public static String getTopActivityName(Context context) {
        // get the top activity name
        String topActivity = "";
        ActivityManager activityManager = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        if (taskInfo != null && taskInfo.size() > 0) {
            topActivity = taskInfo.get(0).topActivity.getClassName();
        }
        return topActivity;
    }

}
