package com.mssm.demoversion.http;

import android.content.Context;

import com.mssm.demoversion.base.BaseApplication;
import com.mssm.demoversion.download.MultiFileDownloadManager;
import com.mssm.demoversion.model.AdvertiseModel;
import com.mssm.demoversion.model.BaseFileDownloadModel;
import com.mssm.demoversion.model.BaseModel;
import com.mssm.demoversion.presenter.AdvertiseInterface;
import com.mssm.demoversion.presenter.MultiFileDownloadListener;
import com.mssm.demoversion.util.CallBackUtils;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.SharedPreferencesUtils;
import com.mssm.demoversion.util.Utils;
import com.mssm.demoversion.view.Advance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Easyhood
 * @desciption 资源预加载请求
 * @since 2024/1/10
 **/
public class HttpResRequest {

    private static final String TAG = "HttpResRequest";

    private MultiFileDownloadManager mDownloadManager;

    private List<BaseFileDownloadModel> mFileDownloadTask;

    private List<Advance> mData;

    private Context mContext;

    public HttpResRequest() {
        mContext = BaseApplication.getInstances().getApplicationContext();
        mDownloadManager = new MultiFileDownloadManager();
    }

    /**
     * 请求预加载资源
     */
    public void requestNewResource() {
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
        HashMap<String, String> headerParams= new HashMap<>();
        headerParams.put("signature",
                Utils.getSignatureMd5Str("/ad/api/equipment_ad/get_pre_download_list/"));
        HashMap<String, String> queryParams= new HashMap<>();
        queryParams.put("device_uuid", Utils.getCapitalDeviceSnNumber());
        queryParams.put("ad_version_code",String.valueOf(Utils.getAppVersionCode()));
        queryParams.put("ad_version_name", Utils.getAppVersionName());

        // 通过接口实现类返回call对象
        Call<AdvertiseModel> resCall = advertiseInterface.getNewResource(headerParams, queryParams);
        // 通过Call执行请求
        resCall.enqueue(new Callback<AdvertiseModel>() {
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
        LogUtils.d(TAG, "startFileDownload");
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
            CallBackUtils.doResourceDownloadFinishedListener(Constant.STATUS_FINISHED);
        } else {
            CallBackUtils.doResourceDownloadFinishedListener(Constant.STATUS_DOWNLOADING);
            mDownloadManager.startMultiFileDownload(mFileDownloadTask);
        }
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
            CallBackUtils.doResourceDownloadFinishedListener(Constant.STATUS_FINISHED);
        }
    };

    /**
     * 推送下载状态
     */
    public void pushResourceDownloadStatus(String status) {
        LogUtils.d(TAG, "pushResourceDownloadStatus: status = " + status);
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
        HashMap<String, String> headerParams = new HashMap<>();
        headerParams.put("signature",
                Utils.getSignatureMd5Str("/ad/api/equipment_ad/set_device_is_downloading/"));
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("device_uuid", Utils.getCapitalDeviceSnNumber());
        queryParams.put("status", status);
        // 通过接口实现类返回call对象
        Call<BaseModel> pushCall = advertiseInterface.pushResourceStatus(headerParams, queryParams);
        // 通过Call执行请求
        pushCall.enqueue(new Callback<BaseModel>() {
            @Override
            public void onResponse(Call<BaseModel> call, Response<BaseModel> response) {
                if (response == null) {
                    LogUtils.d(TAG, "pushResourceDownloadStatus onResponse: response is null !");
                    return;
                }
                BaseModel model = response.body();
                if (model == null) {
                    LogUtils.d(TAG, "pushResourceDownloadStatus onResponse: model is null");
                    return;
                }
                LogUtils.d(TAG, "pushResourceDownloadStatus onResponse: model = " + model.toString());
            }

            @Override
            public void onFailure(Call<BaseModel> call, Throwable t) {
                LogUtils.d(TAG, "pushResourceDownloadStatus onFailure: Error ! Cause by " + t);
            }
        });
    }
}
