package com.mssm.demoversion.http;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hjq.http.EasyHttp;
import com.hjq.http.listener.HttpCallback;
import com.hjq.http.listener.OnHttpListener;
import com.mssm.demoversion.api.SearchAuthorApi;
import com.mssm.demoversion.bean.HttpData;

import java.util.List;

import okhttp3.Call;

/**
 * @author Easyhood
 * @desciption http Get 请求类
 * @since 2023/7/12
 **/
public class HttpRequest implements OnHttpListener {
    private AppCompatActivity mAppCompatActivity;

    /**
     * 构造方法
     *
     * @param activity AppCompatActivity
     */
    public HttpRequest(AppCompatActivity activity) {
        mAppCompatActivity = activity;
    }

    /**
     * EasyHttp Get 请求
     */
    public void httpGetRequest() {
        EasyHttp.get(mAppCompatActivity)
                .api(new SearchAuthorApi()
                        .setId(190000))
                .request(new HttpCallback<HttpData<List<SearchAuthorApi.Bean>>>(this) {

                    @Override
                    public void onSucceed(HttpData<List<SearchAuthorApi.Bean>> result) {
                        Toast.makeText(mAppCompatActivity, "Get 请求成功，请看日志",
                                Toast.LENGTH_SHORT).show();
                        result.getData().get(0).getCourseId();
                    }
                });

    }

    /**
     * EasyHttp Post 请求
     */
    public void httpPostRequest() {
        EasyHttp.post(mAppCompatActivity)
                .api(new SearchAuthorApi()
                        .setId(190000))
                .request(new HttpCallback<HttpData<List<SearchAuthorApi.Bean>>>(this) {

                    @Override
                    public void onSucceed(HttpData<List<SearchAuthorApi.Bean>> result) {
                        Toast.makeText(mAppCompatActivity, "Get 请求成功，请看日志",
                                Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public void onStart(Call call) {
        OnHttpListener.super.onStart(call);
    }

    @Override
    public void onSucceed(Object result, boolean cache) {
        OnHttpListener.super.onSucceed(result, cache);
    }

    @Override
    public void onSucceed(Object result) {

    }

    @Override
    public void onFail(Exception e) {

    }

    @Override
    public void onEnd(Call call) {
        OnHttpListener.super.onEnd(call);
    }
}
