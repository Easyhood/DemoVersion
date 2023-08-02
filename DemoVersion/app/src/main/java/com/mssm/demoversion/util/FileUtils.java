package com.mssm.demoversion.util;

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author Easyhood
 * @desciption 文件操作工具类
 * @since 2023/8/1
 **/
public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * 按照时间先后删除老文件
     * @param dirPath 文件目录路径
     */
    public static void removeFileByTime(String dirPath) {
        LogUtils.d(TAG, "removeFileByTime: dirPath is " + dirPath);
        //获取目录下所有文件
        List<File> allFile = getDirAllFile(new File(dirPath));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //获取当前时间
        Date end = new Date(System.currentTimeMillis());
        try {
            end = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));
        } catch (Exception e){
            e.printStackTrace();
        }
        for (File file : allFile) {//ComDef
            try {
                //文件时间减去当前时间
                Date start = dateFormat.parse(dateFormat.format(new Date(file.lastModified())));
                long diff = end.getTime() - start.getTime();//这样得到的差值是微秒级别
                long days = diff / (1000 * 60 * 60 * 24);
                if(30 <= days){
                    deleteFile2(file);
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除文件夹及文件夹下所有文件
     * @param file File
     */
    public static void deleteFile2(File file) {
        LogUtils.d(TAG, "deleteFile2: file is " + file.toString());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile2(f);
            }
            file.delete();
        } else if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取指定目录下一级文件
     * @param file File
     * @return fileList
     */
    public static List<File> getDirAllFile(File file) {
        LogUtils.d(TAG, "getDirAllFile: file is " + file.toString());
        List<File> fileList = new ArrayList<>();
        File[] fileArray = file.listFiles();
        if(fileArray == null)
            return fileList;
        for (File f : fileArray) {
            fileList.add(f);
        }
        fileSortByTime(fileList);
        return fileList;
    }

    /**
     * 对文件进行时间排序
     * @param fileList List
     */
    public static void fileSortByTime(List<File> fileList) {
        LogUtils.d(TAG, "fileSortByTime: fileList is " + fileList.toString());
        Collections.sort(fileList, new Comparator<File>() {
            public int compare(File p1, File p2) {
                if (p1.lastModified() < p2.lastModified()) {
                    return -1;
                }
                return 1;
            }
        });
    }
}
