package com.mssm.demoversion.download;

import android.os.Environment;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.mssm.demoversion.util.CallBackUtils;
import com.mssm.demoversion.util.Constant;

import java.io.File;
import java.util.List;

/**
 * @author Easyhood
 * @desciption 多任务下载
 * @since 2023/7/12
 **/
public class MultiDownload {
    private static final String TAG = "MultiDownload";
    // 多任务下载
    private FileDownloadListener downloadListener;

    private int mTaskCount;

    //public String mSaveFolder = FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "feifei_save";
    public static String mSaveFolder = Environment.getExternalStorageDirectory() + "/MSSMDownload";

    public FileDownloadListener createLis(int tag) {
        return new FileDownloadSampleListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                if (task.getListener() != downloadListener) {
                    return;
                }
                Log.d(TAG, "pending taskId:" + task.getId() + ",fileName:"
                        + task.getFilename() + ",soFarBytes:" + soFarBytes + ",totalBytes:"
                        + totalBytes + ",percent:" + soFarBytes * 1.0 / totalBytes);

            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                if (task.getListener() != downloadListener) {
                    return;
                }
                Log.d(TAG, "progress taskId:" + task.getId() + ",fileName:"
                        + task.getFilename() + ",soFarBytes:" + soFarBytes + ",totalBytes:"
                        + totalBytes + ",percent:" + soFarBytes * 1.0 / totalBytes + ",speed:" + task.getSpeed());
            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
                if (task.getListener() != downloadListener) {
                    return;
                }
                Log.d(TAG, "blockComplete taskId:" + task.getId() + ",filePath:"
                        + task.getPath() + ",fileName:" + task.getFilename() + ",speed:"
                        + task.getSpeed() + ",isReuse:" + task.reuse());
                mTaskCount --;
                Log.d(TAG, "blockComplete: mTaskCount = " + mTaskCount);
                if (mTaskCount == Constant.INDEX_0) {
                    completed(task);
                }
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                Log.d(TAG, "completed");
                CallBackUtils.doCallBackMethod(tag);
                if (task.getListener() != downloadListener) {
                    return;
                }
                Log.d(TAG, "completed taskId:" + task.getId() + ",isReuse:" + task.reuse());
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                if (task.getListener() != downloadListener) {
                    return;
                }
                Log.d(TAG, "paused taskId:" + task.getId() + ",soFarBytes:" + soFarBytes
                        + ",totalBytes:" + totalBytes + ",percent:" + soFarBytes * 1.0 / totalBytes);
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                if (task.getListener() != downloadListener) {
                    return;
                }
                Log.d(TAG, "error taskId:" + task.getId() + ",e:" + e.getLocalizedMessage());
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                if (task.getListener() != downloadListener) {
                    return;
                }
                Log.d(TAG, "warn taskId:" + task.getId());
            }
        };
    }

    public void start_multi(List<BaseDownloadTask> tasks, int tag) {

        downloadListener = createLis(tag);
        //(1) 创建 FileDownloadQueueSet
        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(downloadListener);

        //(2) 创建Task 队列

        //(3) 设置参数
        // 每个任务的进度 无回调
        queueSet.disableCallbackProgressTimes();
        // do not want each task's download progress's callback,we just consider which task will completed.
        // 最大回调次数
        //queueSet.setCallbackProgressTimes(100);
        // 每个回调之间的间隔
        //queueSet.setCallbackProgressMinInterval(100);
        //失败 重试次数
        queueSet.setAutoRetryTimes(3);

        //避免掉帧
        FileDownloader.enableAvoidDropFrame();

        //(4)串行下载
        queueSet.downloadSequentially(tasks);

        mTaskCount = tasks.size();

        //(5)任务启动
        queueSet.start();
    }

    public void stop_multi() {
        FileDownloader.getImpl().pause(downloadListener);
    }

    public void deleteAllFile() {

        //清除所有的下载任务
        FileDownloader.getImpl().clearAllTaskData();

        //清除所有下载的文件
        int count = 0;
        File file = new File(FileDownloadUtils.getDefaultSaveRootPath());
        do {
            if (!file.exists()) {
                break;
            }

            if (!file.isDirectory()) {
                break;
            }

            File[] files = file.listFiles();

            if (files == null) {
                break;
            }

            for (File file1 : files) {
                count++;
                file1.delete();
            }

        } while (false);

    }


}
