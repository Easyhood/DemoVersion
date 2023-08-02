package com.mssm.demoversion.http;

import android.content.Context;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.mssm.demoversion.base.BaseApplication;
import com.mssm.demoversion.download.MultiDownload;
import com.mssm.demoversion.model.AdvertiseModel;
import com.mssm.demoversion.presenter.AdvertiseInterface;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.SharedPreferencesUtils;
import com.mssm.demoversion.util.Utils;
import com.mssm.demoversion.view.Advance;

import java.util.ArrayList;
import java.util.List;

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

    private MultiDownload mMultiDownload;

    private List<BaseDownloadTask> mTask;

    private List<Advance> mData;

    private Context mContext;

    public HttpRequest() {
        mContext = BaseApplication.getInstances().getApplicationContext();
        mMultiDownload = new MultiDownload();
    }
    /**
     * 请求广告播放计划
     */
    public void requestAdvertisePlan() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AdvertiseInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build(); // 创建Retrofit实体类

        // 创建接口实现类
        AdvertiseInterface advertiseInterface = retrofit.create(AdvertiseInterface.class);
        // 通过接口实现类返回call对象
        Call<AdvertiseModel> adCall = advertiseInterface.getAdvertisePlan(Utils.getDeviceSnNumber());
        // 通过Call执行请求
        adCall.enqueue(new Callback<AdvertiseModel>() {
            @Override
            public void onResponse(Call<AdvertiseModel> call, Response<AdvertiseModel> response) {
                // 通过response获取序列化后的数据, 因为之前已经添加了GsonConvert
                AdvertiseModel model = response.body();
                if (model.getData() == null) {
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
                    startMultiDownload(model);
                    SharedPreferencesUtils.putString(mContext, Constant.AD_UUID_KEY, planId);
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
     * 开始多任务下载
     * @param model 广告实体对象
     */
    public void startMultiDownload(AdvertiseModel model) {
        mTask = new ArrayList<>();
        mData = new ArrayList<>();
        mTask.clear();
        mData.clear();
        for (int i = 0; i < model.getData().size(); i++) {
            for (int j = 0; j < model.getData().get(i).getAdMaterials().size(); j++) {
                String fileType = model.getData().get(i).getAdMaterials().get(j).getMatType();
                String filePath = model.getData().get(i).getAdMaterials().get(j).getAdFilePath();
                int playTime = model.getData().get(i).getAdMaterials().get(j).getPlayTime();
                int filePlayTime = playTime * 1000;
                analyzeFileParam(fileType, filePath, filePlayTime);
            }
        }
    mMultiDownload.start_multi(mTask, Constant.ADVERTISE_DOWNLOAD);
    }


    /**
     * 处理文件参数
     * @param fileType 文件类型
     * @param filePath 文件路径
     */
    public void analyzeFileParam(String fileType, String filePath, int filePlayTime) {
        LogUtils.d(TAG, "analyzeFileParam: fileType = " + fileType + " , filePlayTime = "
                        + filePlayTime + " , filePath = " + filePath);
        StringBuffer sb = new StringBuffer();
        String httpUrlPath = sb.append(AdvertiseInterface.BASE_URL).append(filePath).toString();
        BaseDownloadTask task = FileDownloader.getImpl().create(httpUrlPath)
                .setPath(MultiDownload.DOWNLOAD_PATH, true);
        mTask.add(task);
        String localPath = Utils.checkDownloadFilePath(Utils.getFileName(filePath));
        if (Constant.IMAGE_TYPE.equals(fileType)) {
            Advance imageAdvance = new Advance(localPath, Constant.IMAGE_INDEX, filePlayTime);
            mData.add(imageAdvance);
        } else if (Constant.VIDEO_TYPE.equals(fileType)) {
            Advance videoAdvance = new Advance(localPath, Constant.VIDEO_INDEX, filePlayTime);
            mData.add(videoAdvance);
        } else {
            LogUtils.d(TAG, "analyzeFileParam: fileType is Error! fileType is " + fileType);
        }
    }
    public List<Advance> getData () {
        return mData;
    }
}
