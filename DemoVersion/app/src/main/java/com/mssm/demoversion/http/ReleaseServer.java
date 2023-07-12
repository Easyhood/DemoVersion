package com.mssm.demoversion.http;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestServer;

/**
 * @author Easyhood
 * @desciption 服务器配置
 * @since 2023/7/12
 **/
public class ReleaseServer implements IRequestServer {
    @NonNull
    @Override
    public String getHost() {
        return "https://www.wanandroid.com/";
    }
}
