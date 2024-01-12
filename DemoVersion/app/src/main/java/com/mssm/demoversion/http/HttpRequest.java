package com.mssm.demoversion.http;

import android.content.Context;
import android.util.Log;

import com.mssm.demoversion.base.BaseApplication;
import com.mssm.demoversion.download.MultiFileDownloadManager;
import com.mssm.demoversion.model.AdvertiseModel;
import com.mssm.demoversion.model.BaseFileDownloadModel;
import com.mssm.demoversion.model.OTAModel;
import com.mssm.demoversion.presenter.AdvertiseInterface;
import com.mssm.demoversion.presenter.MultiFileDownloadListener;
import com.mssm.demoversion.util.CallBackUtils;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.FileUtils;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.SharedPreferencesUtils;
import com.mssm.demoversion.util.Utils;
import com.mssm.demoversion.view.Advance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Easyhood
 * @desciption Retrofit网络请求类
 * @since 2023/7/18
 **/
public class HttpRequest {
    private static final String TAG = "HttpRequest";

    private MultiFileDownloadManager mDownloadManager;

    private List<BaseFileDownloadModel> mFileDownloadTask;

    private List<BaseFileDownloadModel> mApkDownloadTask;

    private List<Advance> mData;

    private Context mContext;

    public HttpRequest() {
        mContext = BaseApplication.getInstances().getApplicationContext();
        mDownloadManager = new MultiFileDownloadManager();
    }
    /**
     * 请求广告播放计划
     */
    public void requestAdvertisePlan() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(Utils.createSSLSocketFactory(), new Utils.TrustAllCerts());
        builder.hostnameVerifier(new Utils.TrustAllHostnameVerifier());
        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .baseUrl(AdvertiseInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build(); // 创建Retrofit实体类

        // 创建接口实现类
        AdvertiseInterface advertiseInterface = retrofit.create(AdvertiseInterface.class);
        // 通过接口实现类返回call对象
        Call<AdvertiseModel> adCall = advertiseInterface.getAdvertisePlan(Utils.getCapitalDeviceSnNumber());
        // 通过Call执行请求
        adCall.enqueue(new Callback<AdvertiseModel>() {
            @Override
            public void onResponse(Call<AdvertiseModel> call, Response<AdvertiseModel> response) {
                // 通过response获取序列化后的数据, 因为之前已经添加了GsonConvert
                if (response == null) {
                    LogUtils.d(TAG, "onResponse: response is null !");
                    return;
                }
                AdvertiseModel model = response.body();
                if (model == null || model.getData() == null || model.getData().size() < Constant.INDEX_1) {
                    LogUtils.d(TAG, "onResponse: model.getData() is null");
                    return;
                }
                LogUtils.d(TAG, "onResponse: model = " + model.toString());
                String planId = model.getData().get(Constant.INDEX_0).getPlanId();
                String spPlanId = SharedPreferencesUtils.getString(mContext, Constant.AD_UUID_KEY);
                if (planId == null || spPlanId == null) {
                    LogUtils.d(TAG, "onResponse: planId or spPlanId is null");
                    return;
                }
                if (!planId.equals(spPlanId)){
                    LogUtils.d(TAG, "onResponse: startMultiDownload ");
                    SharedPreferencesUtils.putString(mContext, Constant.AD_UUID_KEY, planId);
                    startFileDownload(model);
                } else {
                    LogUtils.d(TAG, "onResponse: Not download Cause planId is same as spPlanId : "
                            + planId);
                }
            }

            @Override
            public void onFailure(Call<AdvertiseModel> call, Throwable t) {
                LogUtils.d(TAG, "onFailure: Error ! Cause by " + t);
            }
        });
    }

    /**
     * 开始下载文件
     * @param model AdvertiseModel
     */
    public void startFileDownload(AdvertiseModel model) {
        mFileDownloadTask = new ArrayList<>();
        mData = new ArrayList<>();
        mData.clear();
        mFileDownloadTask.clear();
        for (int i = 0; i < model.getData().size(); i++) {
            for (int j = 0; j < model.getData().get(i).getAdMaterials().size(); j++) {
                String filePath = model.getData().get(i).getAdMaterials().get(j).getAdFilePath();
                StringBuffer sb = new StringBuffer();
                String downloadUrl = sb.append(AdvertiseInterface.BASE_URL).append(filePath).toString();
                String saveFilePath = Utils.checkDownloadFilePath(Utils.getFileName(filePath));
                String fileType = model.getData().get(i).getAdMaterials().get(j).getMatType();
                String md5Str = model.getData().get(i).getAdMaterials().get(j).getFileMD5();
                boolean isPlay = model.getData().get(i).getAdMaterials().get(j).getIsPlay();
                int playTime = model.getData().get(i).getAdMaterials().get(j).getPlayTime() * 1000;
                if (Utils.checkFileExistsAndMD5(md5Str, saveFilePath)) {
                    if (Constant.IMAGE_TYPE.equals(fileType) && isPlay) {
                        Advance imageAdvance = new Advance(saveFilePath, Constant.IMAGE_INDEX, playTime);
                        mData.add(imageAdvance);
                    } else if (Constant.VIDEO_TYPE.equals(fileType) && isPlay) {
                        Advance videoAdvance = new Advance(saveFilePath, Constant.VIDEO_INDEX, playTime);
                        mData.add(videoAdvance);
                    } else {
                        LogUtils.d(TAG, "startFileDownload: fileType is Error! fileType is " + fileType);
                    }
                } else {
                    BaseFileDownloadModel downloadModel = new BaseFileDownloadModel();
                    downloadModel.setDownloadUrl(downloadUrl);
                    downloadModel.setSaveFilePath(saveFilePath);
                    downloadModel.setFileType(fileType);
                    downloadModel.setMd5Str(md5Str);
                    downloadModel.setIsPlay(isPlay);
                    downloadModel.setFilePlayTime(playTime);
                    downloadModel.setListener(this.adListener);
                    if (!mFileDownloadTask.contains(downloadUrl)) {
                        mFileDownloadTask.add(downloadModel);
                    }
                }
            }
        }
        if (Constant.INDEX_1 > mFileDownloadTask.size() && Constant.INDEX_0 < mData.size()) {
            LogUtils.d(TAG, "startFileDownload: mFileDownloadTask size is 0 !");
            CallBackUtils.doAdDownloadFinishedListener(mData);
        }
        mDownloadManager.startMultiFileDownload(mFileDownloadTask);
    }

    /**
     * 创建一个MultiFileDownloadListener对象，实现回调方法
     */
    MultiFileDownloadListener adListener = new MultiFileDownloadListener() {
        @Override
        public void onSuccess(String url, String filePath) {
            // 下载成功时，打印文件的URL和保存路径
            LogUtils.d(TAG,
                    "onSuccess: Downloaded : " + url + " to " + filePath);
        }

        @Override
        public void onFailure(String url, IOException e) {
            // 下载失败时，打印文件的URL和异常信息
            LogUtils.d(TAG,
                    "onFailure: Failed to download : " + url + ": " + e.getMessage());
        }

        @Override
        public void onProgress(String url, int progress) {
            // 下载过程中，打印文件的URL和进度百分比
            LogUtils.d(TAG, "onProgress: Downloading : "
                    + Utils.getFileName(url) + ": " + progress + "%");
        }

        @Override
        public void onAllDownloadFinished(ArrayList<BaseFileDownloadModel> successList) {
            // 全部文件下载结束后，打印成功列表的大小和内容
            LogUtils.d(TAG,
                    "onAllDownloadFinished: All download finished, success list size: " +
                            successList.size());
            for (int i=0; i < successList.size(); i++) {
                LogUtils.d(TAG, "onAllDownloadFinished: downloadModel is " + successList.get(i).toString());
                String savePath = successList.get(i).getSaveFilePath();
                String fileType = successList.get(i).getFileType();
                int playTime = successList.get(i).getFilePlayTime();
                if (Constant.IMAGE_TYPE.equals(fileType)) {
                    Advance imageAdvance = new Advance(savePath, Constant.IMAGE_INDEX, playTime);
                    mData.add(imageAdvance);
                } else if (Constant.VIDEO_TYPE.equals(fileType)) {
                    Advance videoAdvance = new Advance(savePath, Constant.VIDEO_INDEX, playTime);
                    mData.add(videoAdvance);
                } else {
                    LogUtils.d(TAG, "onAllDownloadFinished: fileType is Error! fileType is " + fileType);
                }
            }
            CallBackUtils.doAdDownloadFinishedListener(mData);
        }
    };

    /**
     * 请求最新apk最新版本
     */
    public void requestNewAdApk() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(Utils.createSSLSocketFactory(), new Utils.TrustAllCerts());
        builder.hostnameVerifier(new Utils.TrustAllHostnameVerifier());
        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .baseUrl(AdvertiseInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build(); // 创建Retrofit实体类

        // 创建接口实现类
        AdvertiseInterface advertiseInterface = retrofit.create(AdvertiseInterface.class);
        // 通过接口实现类返回call对象
        Call<OTAModel> apkCall = advertiseInterface.getNewAdApk(Utils.getCapitalDeviceSnNumber());
        apkCall.enqueue(new Callback<OTAModel>() {
            @Override
            public void onResponse(Call<OTAModel> call, Response<OTAModel> response) {
                if (response == null) {
                    Log.d(TAG, "requestNewAdApk onResponse: response is null");
                    return;
                }
                OTAModel otaModel = response.body();
                if (otaModel == null || otaModel.getData() == null) {
                    LogUtils.d(TAG, "requestNewAdApk onResponse: otaModel.getData() is null");
                    return;
                }
                LogUtils.d(TAG, "requestNewAdApk onResponse: otaModel = " + otaModel.toString());
                String serviceApkName = otaModel.getData().getApkName();
                int serviceApkCode = otaModel.getData().getVersionCode();
                String md5Str = otaModel.getData().getMd5Str();
                LogUtils.d(TAG, "onResponse: serviceApkName = " +
                        serviceApkName + "; serviceApkCode = " + serviceApkCode);
                if (serviceApkName == null || serviceApkCode < Constant.INDEX_7) {
                    LogUtils.d(TAG,
                            "onResponse: stop ! cause serviceApkName is null or serviceApkVersion less than 5.0");
                    return;
                }
                if (Utils.getAppVersionCode() < serviceApkCode) {
                    if (md5Str.equals(SharedPreferencesUtils.getString(BaseApplication.getContext(), Constant.OTA_UUID_KEY))) {
                        LogUtils.d(TAG, "onResponse: OTA_UUID_KEY is same ! OTA_UUID_KEY = " + md5Str);
                        return;
                    }
                    SharedPreferencesUtils.putString(BaseApplication.getContext(), Constant.OTA_UUID_KEY, md5Str);
                    LogUtils.d(TAG, "onResponse: save md5Str = " + md5Str);
                    startDownloadApk(otaModel);
                }
            }

            @Override
            public void onFailure(Call<OTAModel> call, Throwable t) {
                LogUtils.d(TAG, "requestNewAdApk onFailure: Error ! Cause by " + t);
            }
        });
    }

    /**
     * 开始下载apk
     * @param otaModel OTAModel
     */
    private void startDownloadApk(OTAModel otaModel) {
        LogUtils.d(TAG, "startDownloadApk");
        mApkDownloadTask = new ArrayList<>();
        mApkDownloadTask.clear();
        deleteDownloadApkPath();
        BaseFileDownloadModel downloadModel = new BaseFileDownloadModel();
        String filePath = otaModel.getData().getFilePath();
        StringBuffer sb = new StringBuffer();
        String downloadUrl = sb.append(AdvertiseInterface.BASE_URL).append(filePath).toString();
        String saveFilePath = Utils.checkApkDownloadPath(Utils.getFileName(filePath));
        downloadModel.setDownloadUrl(downloadUrl);
        downloadModel.setSaveFilePath(saveFilePath);
        downloadModel.setMd5Str(otaModel.getData().getMd5Str());
        downloadModel.setApkName(otaModel.getData().getApkName());
        downloadModel.setVersionName(otaModel.getData().getVersionName());
        downloadModel.setVersionCode(otaModel.getData().getVersionCode());
        downloadModel.setIsPlay(true);
        downloadModel.setListener(this.apkListener);
        mApkDownloadTask.add(downloadModel);
        mDownloadManager.startMultiFileDownload(mApkDownloadTask);
    }

    /**
     * 创建一个MultiFileDownloadListener对象，实现回调方法
     */
    MultiFileDownloadListener apkListener = new MultiFileDownloadListener() {
        @Override
        public void onSuccess(String url, String filePath) {
            // 下载成功时，打印文件的URL和保存路径
            LogUtils.d(TAG + "_apkListener", "onSuccess: Downloaded : " +
                    url + " to " + filePath);
        }

        @Override
        public void onFailure(String url, IOException e) {
            // 下载失败时，打印文件的URL和异常信息
            LogUtils.d(TAG + "_apkListener", "onFailure: Failed to download : " +
                    url + ": " + e.getMessage());
        }

        @Override
        public void onProgress(String url, int progress) {
            // 下载过程中，打印文件的URL和进度百分比
            LogUtils.d(TAG + "_apkListener", "onProgress: Downloading : " +
                    Utils.getFileName(url) + ": " + progress + "%");
        }

        @Override
        public void onAllDownloadFinished(ArrayList<BaseFileDownloadModel> successList) {
            // 全部文件下载结束后，打印成功列表的大小和内容
            LogUtils.d(TAG + "_apkListener",
                    "onAllDownloadFinished: All download finished, success list size: " +
                            successList.size());
            if (successList.size() > Constant.INDEX_0) {
                String savePath = successList.get(Constant.INDEX_0).getSaveFilePath();
                CallBackUtils.doApkDownloadFinishedListener(savePath);
            }
        }
    };

    /**
     * 下载前先删除上次下载的安装包
     */
    private void deleteDownloadApkPath() {
        LogUtils.d(TAG, "deleteDownloadApkPath");
        File file = new File(Constant.APK_DOWNLOAD_PATH);
        if (file.exists()) {
            FileUtils.deleteFile2(file);
        } else {
            LogUtils.d(TAG, "deleteDownloadApkPath: file is not exists");
        }
    }
}
