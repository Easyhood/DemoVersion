package com.mssm.demoversion.download;

import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;

/**
 * @author Easyhood
 * @desciption 单任务下载
 * @since 2023/7/12
 **/
public class SingleDownload {
    BaseDownloadTask singleTask;
    public int singleTaskId = 0;
    String apkUrl = "http://cdn.llsapp.com/android/LLS-v4.0-595-20160908-143200.apk";
    String singleFileSaveName = "liulishuo.apk";
    public String mSinglePath = FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "feifei_save"
            + File.separator + singleFileSaveName;
    public String mSaveFolder = FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "feifei_save";

    public void start_single() {

        String url = apkUrl;
        singleTask = FileDownloader.getImpl().create(url)
//                .setPath(mSinglePath,false)
                .setPath(mSinglePath, true)
                // 最大回调次数
                .setCallbackProgressTimes(300)
                // 每个回调之间的间隔
                .setMinIntervalUpdateSpeed(400)
                //.setTag()
                .setListener(new FileDownloadSampleListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.d("feifei", "pending taskId:" + task.getId() + ",soFarBytes:"
                                + soFarBytes + ",totalBytes:" + totalBytes + ",percent:"
                                + soFarBytes * 1.0 / totalBytes);

                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.d("feifei", "progress taskId:" + task.getId() + ",soFarBytes:"
                                + soFarBytes + ",totalBytes:" + totalBytes + ",percent:"
                                + soFarBytes * 1.0 / totalBytes + ",speed:" + task.getSpeed());
                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                        Log.d("feifei", "blockComplete taskId:" + task.getId() + ",filePath:"
                                + task.getPath() + ",fileName:" + task.getFilename()
                                + ",speed:" + task.getSpeed() + ",isReuse:" + task.reuse());
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Log.d("feifei", "completed taskId:" + task.getId() + ",isReuse:"
                                + task.reuse());
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.d("feifei", "paused taskId:" + task.getId() + ",soFarBytes:"
                                + soFarBytes + ",totalBytes:" + totalBytes + ",percent:"
                                + soFarBytes * 1.0 / totalBytes);
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Log.d("feifei", "error taskId:" + task.getId() + ",e:"
                                + e.getLocalizedMessage());
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        Log.d("feifei", "warn taskId:" + task.getId());
                    }
                });

        singleTaskId = singleTask.start();

    }


    public void pause_single() {
        Log.d("feifei", "pause_single task:" + singleTaskId);
        FileDownloader.getImpl().pause(singleTaskId);
    }

    public void delete_single() {

        //删除单个任务的database记录
        boolean deleteData = FileDownloader.getImpl().clear(singleTaskId, mSaveFolder);
        File targetFile = new File(mSinglePath);
        boolean delate = false;
        if (targetFile.exists()) {
            delate = targetFile.delete();
        }

        Log.d("feifei", "delete_single file,deleteDataBase:" + deleteData
                + ",mSinglePath:" + mSinglePath + ",delate:" + delate);

        new File(FileDownloadUtils.getTempPath(mSinglePath)).delete();
    }

}
