package com.mssm.demoversion.util;

import static android.content.Context.ACTIVITY_SERVICE;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.mssm.demoversion.base.BaseApplication;
import com.youngfeel.yf_rk356x_api.YF_RK356x_API_Manager;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
     * 判断默认某个文件是否存在
     *
     * @param sourcePath 文件绝对路径
     * @return 文件是否存在
     */
    public static boolean isDefaultFileExists(String sourcePath) {
        File file = new File(Environment.getExternalStorageDirectory() + "/MSSMDefault", sourcePath);
        if (!file.getParentFile().exists()) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 判断下载的某个文件是否存在
     *
     * @param sourcePath 文件绝对路径
     * @return 文件是否存在
     */
    public static boolean isDownloadFileExists(String sourcePath) {
        Log.d(TAG, "isDownloadFileExists: sourcePath = " + sourcePath);
        File file = new File(Environment.getExternalStorageDirectory() + "/MSSMDownload/", sourcePath);
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
     *
     * @param sourcePath 文件路径
     * @param rawId      资源ID
     * @param context    Context
     * @return 文件绝对路径
     */
    public static String copyRawFileToExDir(String sourcePath, int rawId, Context context) {
        File dstFile = new File(Environment.getExternalStorageDirectory() + "/MSSMDefault", sourcePath);
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
     *
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
     *
     * @param sourcePath 文件名
     * @param rawId      文件id
     * @param context    Context
     * @return 文件路径
     */
    public static String checkDefaultFilePath(String sourcePath, int rawId, Context context) {
        String path = null;
        File dstFile = new File(Environment.getExternalStorageDirectory() + "/MSSMDefault", sourcePath);
        boolean isFileExists = isDefaultFileExists(sourcePath);
        LogUtils.d(TAG, "checkDefaultFilePath: isFileExists = " + isFileExists);
        if (!isFileExists) {
            path = copyRawFileToExDir(sourcePath, rawId, context);
        } else {
            path = dstFile.getAbsolutePath();
        }
        return path;
    }

    /**
     * 检查下载文件路径
     *
     * @param sourcePath 文件名
     * @return 文件路径
     */
    public static String checkDownloadFilePath(String sourcePath) {
        String path = null;
        File downloadFile = new File(Constant.DOWNLOAD_PATH, sourcePath);
        boolean isFileExists = isDownloadFileExists(sourcePath);
        LogUtils.d(TAG, "checkDownloadFilePath: isFileExists = " + isFileExists);
        if (!isFileExists) {
            File parentDir = downloadFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdir();
            }
        }
        path = downloadFile.getAbsolutePath();
        return path;
    }

    /**
     * 检查APK下载文件路径
     *
     * @param sourcePath 文件名
     * @return 文件路径
     */
    public static String checkApkDownloadPath(String sourcePath) {
        String path = null;
        File downloadFile = new File(Constant.APK_DOWNLOAD_PATH, sourcePath);
        boolean isFileExists = isDownloadFileExists(sourcePath);
        LogUtils.d(TAG, "checkApkDownloadPath: isFileExists = " + isFileExists);
        if (!isFileExists) {
            File parentDir = downloadFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdir();
            }
        }
        path = downloadFile.getAbsolutePath();
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
     *
     * @param context Context
     * @return topActivity
     */
    public static String getTopActivityName(Context context) {
        // get the top activity name
        String topActivity = "";
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        if (taskInfo != null && taskInfo.size() > Constant.INDEX_0) {
            topActivity = taskInfo.get(Constant.INDEX_0).topActivity.getClassName();
        }
        return topActivity;
    }

    /**
     * 获取当前设备SN号
     * @return serialNumber
     */
    public static String getDeviceSnNumber() {
        String serialNumberStr = "1E3D79E9E60F3625";
         yfapiManager = new YF_RK356x_API_Manager(BaseApplication.getInstances());
         serialNumberStr = yfapiManager.yfgetSerialNumber();
        return serialNumberStr;
    }

    /**
     * 获取当前设备SN号
     * @return serialNumber
     */
    public static String getCapitalDeviceSnNumber() {
        // 693C351B999682EF 西影64号机子
        // CA1C547777EFED27 小寨哆啦星球039
        // 小寨 8A01BD7A1FB6E09E
        String serialNumberStr = "8A01BD7A1FB6E09E";
         yfapiManager = new YF_RK356x_API_Manager(BaseApplication.getInstances());
         serialNumberStr = yfapiManager.yfgetSerialNumber().toUpperCase();
         Log.d(TAG, "getCapitalDeviceSnNumber: serialNumberStr = " + serialNumberStr);
        return serialNumberStr;
    }

    public static String getFileName(String sourcePath){
        LogUtils.d(TAG, "getFileName: sourcePath = " + sourcePath);
        String mFileName = "";
        int index = sourcePath.lastIndexOf("/");
        mFileName = sourcePath.substring(index + Constant.INDEX_1);
        LogUtils.d(TAG, "getFileName: mFileName = " + mFileName);
        return mFileName;
    }

    /**
     * 设置默认桌面
     */
    public static void setHomeLauncher(){
        Intent intent = new Intent("com.android.yf_set_defaultLauncher");
        intent.putExtra("pkgname","com.mssm.demoversion");
        intent.putExtra("classname","com.mssm.demoversion.activity.AdvertisePlayActivity");
        BaseApplication.getContext().sendBroadcast(intent);
    }

    /**
     * 自定义SS验证相关类
     */
    public static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            LogUtils.d(TAG, "checkClientTrusted: authType = " + authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            LogUtils.d(TAG, "checkServerTrusted: authType = " + authType);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            LogUtils.d(TAG, "getAcceptedIssuers");
            return new X509Certificate[0];
        }
    }

    public static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            LogUtils.d(TAG, "verify: hostname = " + hostname);
            return true;
        }
    }

    public static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        LogUtils.d(TAG, "createSSLSocketFactory");
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new X509TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            LogUtils.d(TAG, "createSSLSocketFactory: Exception is " + e);
        }
        return ssfFactory;
    }

    /**
     * 获取当前程序版本号
     * @return 版本号
     */
    public static int getAppVersionCode() {
        int versioncode = 0;
        try {
            PackageManager pm = BaseApplication.getContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo("com.mssm.demoversion", 0);
            long versionCodeL = pi.getLongVersionCode();
            versioncode = new Long(versionCodeL).intValue();
            Log.d(TAG, "com.mssm.demoversion getAppVersionCode: " + versioncode);
        } catch (Exception e) {
            LogUtils.e(TAG, "Exception : " + e);
        }
        Log.d(TAG, "getAppVersionCode: versioncode = " + versioncode);
        return versioncode;
    }

    /**
     * 获取当前程序版本名
     * @return 版本名
     */
    public static String getAppVersionName() {
        String versionName = "";
        try {
            PackageManager pm = BaseApplication.getContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo("com.mssm.demoversion", 0);
            versionName = pi.versionName;
        } catch (Exception e) {
            LogUtils.e(TAG, "Exception : " + e);
        }
        Log.d(TAG, "getAppVersionCode: versionName = " + versionName);
        return versionName;
    }

    /**
     * 检查文件的实际MD5值是否与期望值一致，接收一个文件的URL和保存路径作为参数，返回一个布尔值
     *
     * @param expectedMD5 期望MD5
     * @param filePath 保存路径
     * @return 比较MD5是否和期望MD5值相等
     */
    public static boolean checkFileExistsAndMD5(String expectedMD5, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            LogUtils.d(TAG, "checkMD5: file not exists");
            return false;
        }
        try {
            // 使用DigestUtils类计算文件的实际MD5值，并转换为16进制字符串
            String actualMD5 = DigestUtils.md5Hex(new FileInputStream(filePath));
            // 从HashMap中获取该文件的期望MD5值，并比较是否相等，返回结果
            return actualMD5.equals(expectedMD5);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取请求头签名
     * @return md5Str
     */
    public static String getSignatureMd5Str(String queryPath) {
        String signatureMd5Str = null;
        String jsonStr = null;
        String queryMethod = "GET";
        String deviceSnNumber = getCapitalDeviceSnNumber();
        jsonStr = queryPath + queryMethod + deviceSnNumber;
        LogUtils.d(TAG, "jsonStr = " + jsonStr);
        signatureMd5Str = DigestUtils.md5Hex(jsonStr.getBytes(StandardCharsets.UTF_8));
        return signatureMd5Str;
    }

}
