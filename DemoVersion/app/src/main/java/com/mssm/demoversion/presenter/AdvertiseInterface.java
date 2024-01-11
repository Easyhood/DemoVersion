package com.mssm.demoversion.presenter;

import com.mssm.demoversion.model.AdvertiseModel;
import com.mssm.demoversion.model.BaseModel;
import com.mssm.demoversion.model.OTAModel;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * @author Easyhood
 * @desciption Retrofit数据接收类
 * @since 2023/7/18
 **/
public interface AdvertiseInterface {
    //定义了需要实现的方法
    // https://test-admin.woozatop.com/ad/device_current_ad_plan/351E6E49C2A14683
    // https://test-ad.woozatop.com/ad/api/equipment_ad/351E6E49C2A14683
    // https://ad-api.woozatop.com/ad/api/equipment_ad/351E6E49C2A14683
    public static String BASE_URL = "https://ad-api.woozatop.com";

    /**
     * 获取服务器广告轮播计划
     * @return 广告轮播计划
     */
    @GET("/ad/api/equipment_ad/{sn_number}")
    public Call<AdvertiseModel> getAdvertisePlan(@Path("sn_number") String snNumberStr);

    /**
     * 获取服务器广告最新版本
     * @return 广告最新版本
     */
    @GET("/ad/api/ota_equipment/{sn_number}")
    public Call<OTAModel> getNewAdApk(@Path("sn_number") String snNumberStr);

    /**
     * 获取服务器预加载资源
     * https://ad-api.woozatop.com/ad/api/equipment_ad/get_pre_download_list?device_uuid=C182CA5C09299F7E
     * @return 需要加载的资源
     */
    @GET("/ad/api/equipment_ad/get_pre_download_list")
    public Call<AdvertiseModel> getNewResource(@HeaderMap Map<String, String> headerParams, @QueryMap Map<String, String> queryParams);

    /**
     * 上报服务器预加载资源下载状态
     *  https://ad-api.woozatop.com/ad/api/equipment_ad/set_device_is_downloading?device_uuid=C182CA5C09299F7E&status=downloading
     * @return 接收状态
     */
    @GET("/ad/api/equipment_ad/set_device_is_downloading")
    public Call<BaseModel> pushResourceStatus(@HeaderMap Map<String, String> headerParams, @QueryMap Map<String, String> queryParams);

}
