package com.mssm.demoversion.download;

import android.content.Context;

import com.mssm.demoversion.base.BaseApplication;
import com.mssm.demoversion.model.BaseFileDownloadModel;
import com.mssm.demoversion.model.MqttModel;
import com.mssm.demoversion.presenter.MultiFileDownloadListener;
import com.mssm.demoversion.util.CallBackUtils;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Easyhood
 * @desciption 界面资源下载管理类
 * @since 2024/1/11
 **/
public class BgResDownloadManager {

    private static final String TAG = "BgResDownloadManager";

    private MultiFileDownloadManager mDownloadManager;

    private List<BaseFileDownloadModel> mFileDownloadTask;

    private Context mContext;

    private MqttModel mMqttModel;

    public BgResDownloadManager() {
        mContext = BaseApplication.getInstances().getApplicationContext();
        mDownloadManager = new MultiFileDownloadManager();
    }

    /**
     * 开始下载mqttModel
     *
     * @param mqttModel MqttModel
     */
    public void startDownloadEndBgRes(MqttModel mqttModel) {
        LogUtils.d(TAG, "startDownloadEndBgRes");
        mFileDownloadTask = new ArrayList<>();
        mFileDownloadTask.clear();
        mMqttModel = mqttModel;
        BaseFileDownloadModel downloadModel = new BaseFileDownloadModel();
        StringBuffer sb = new StringBuffer();
        String downloadUrl = mqttModel.getTopLayerModel().getTopBgImageModel().getResUrl();
        String saveFilePath = Utils.checkDownloadFilePath(Utils.getFileName(downloadUrl));
        String md5Str = mqttModel.getTopLayerModel().getTopBgImageModel().getResType();

        if (Utils.checkFileExistsAndMD5(md5Str, saveFilePath)) {
            if (Constant.DISPLAY_RT_EVENT.equals(mqttModel.getCmdStr())) {
                CallBackUtils.doScanQRCResDownloadFinishedListener(saveFilePath);
            } else {
                CallBackUtils.doEndBgResDownloadFinishedListener(saveFilePath);
            }
        } else {
            downloadModel.setDownloadUrl(downloadUrl);
            downloadModel.setSaveFilePath(saveFilePath);
            downloadModel.setMd5Str(md5Str);
            downloadModel.setIsPlay(true);
            downloadModel.setListener(this.fileDownloadListener);
            if (!mFileDownloadTask.contains(downloadUrl)) {
                mFileDownloadTask.add(downloadModel);
            }
            mDownloadManager.startMultiFileDownload(mFileDownloadTask);
        }
    }

    /**
     * 创建一个MultiFileDownloadListener对象，实现回调方法
     */
    MultiFileDownloadListener fileDownloadListener = new MultiFileDownloadListener() {
        @Override
        public void onSuccess(String url, String filePath) {
            // 下载成功时，打印文件的URL和保存路径
            LogUtils.d(TAG + "fileDownloadListener", "onSuccess: Downloaded : " +
                    url + " to " + filePath);
        }

        @Override
        public void onFailure(String url, IOException e) {
            // 下载失败时，打印文件的URL和异常信息
            LogUtils.d(TAG + "fileDownloadListener", "onFailure: Failed to download : " +
                    url + ": " + e.getMessage());
        }

        @Override
        public void onProgress(String url, int progress) {
            // 下载过程中，打印文件的URL和进度百分比
            LogUtils.d(TAG + "fileDownloadListener", "onProgress: Downloading : " +
                    Utils.getFileName(url) + ": " + progress + "%");
        }

        @Override
        public void onAllDownloadFinished(ArrayList<BaseFileDownloadModel> successList) {
            // 全部文件下载结束后，打印成功列表的大小和内容
            LogUtils.d(TAG + "fileDownloadListener",
                    "onAllDownloadFinished: All download finished, success list size: " +
                            successList.size());
            if (successList.size() > Constant.INDEX_0) {
                String savePath = successList.get(Constant.INDEX_0).getSaveFilePath();
                if (Constant.DISPLAY_RT_EVENT.equals(mMqttModel.getCmdStr())) {
                    CallBackUtils.doScanQRCResDownloadFinishedListener(savePath);
                } else {
                    CallBackUtils.doEndBgResDownloadFinishedListener(savePath);
                }
            }
        }
    };
}
