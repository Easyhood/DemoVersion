package com.mssm.demoversion.presenter;

import com.mssm.demoversion.model.AdvertiseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * @author Easyhood
 * @desciption Retrofit数据接收类
 * @since 2023/7/18
 **/
public interface AdvertiseInterface {
    //定义了需要实现的方法
    // https://test-admin.woozatop.com/ad/device_current_ad_plan/1E3D79E9E60F3625
    public static String BASE_URL = "https://test-admin.woozatop.com/";

    /**
     * 获取服务器广告轮播计划
     * @return
     */
    @GET("ad/device_current_ad_plan/{sn_number}")
    public Call<AdvertiseModel> getAdvertisePlan(@Path("sn_number") String snNumberStr);

}
