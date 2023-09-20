package com.mssm.demoversion.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Easyhood
 * @desciption log工具类
 * @since 2023/8/1
 **/
public class LogUtils {
    private final static Boolean ENABLE = true;
    private final static Boolean NEED_SAVE = true;
    public final static String LOG_PATH = Environment.getExternalStorageDirectory().getPath() + "/MSSM_Android_Log/";

    public static void d(String tag, String info) {
        if (ENABLE) {
            Log.d(tag, info);
            save(tag, info);
        }
    }

    public static void e(String tag, String info) {
        if (ENABLE) {
            Log.e(tag, info);
            save(tag, info);
        }
    }

    public static void save(String tag, String info) {
        if (!NEED_SAVE) {
            return;
        }
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
        makeRootDirectory(LOG_PATH);
        String filename = "app_log_" + simpleDateFormat.format(nowTime) + ".log";
        String filePath = LOG_PATH + filename;
        String strContent = logDateFormat.format(System.currentTimeMillis()) + "_" + tag + "_" + info + "\r\n";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
