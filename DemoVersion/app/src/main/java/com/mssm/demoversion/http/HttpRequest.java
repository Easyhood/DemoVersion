package com.mssm.demoversion.http;

import android.util.Log;

import com.mssm.demoversion.model.AdvertiseModel;
import com.mssm.demoversion.presenter.AdvertiseInterface;
import com.mssm.demoversion.util.Utils;

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

    public static void requestAdvertisePlan() {
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
                AdvertiseModel data = response.body();
                Log.d(TAG, "onResponse: data = " + data.toString());
            }

            @Override
            public void onFailure(Call<AdvertiseModel> call, Throwable t) {
                Log.d(TAG, "onFailure: Error ! Cause by " + t);
            }
        });
    }
}
