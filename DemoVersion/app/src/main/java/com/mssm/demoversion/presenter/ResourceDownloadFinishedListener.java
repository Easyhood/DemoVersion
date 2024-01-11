package com.mssm.demoversion.presenter;

import com.mssm.demoversion.view.Advance;

import java.util.List;

/**
 * @author Easyhood
 * @desciption 预加载资源下载完成回调
 * @since 2024/1/10
 **/
public interface ResourceDownloadFinishedListener {
    void onResourceDownloadFinished(String status);
}
