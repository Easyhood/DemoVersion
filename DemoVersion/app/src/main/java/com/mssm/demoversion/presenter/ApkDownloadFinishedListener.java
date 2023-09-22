package com.mssm.demoversion.presenter;

import com.mssm.demoversion.view.Advance;

import java.util.List;

/**
 * @author Easyhood
 * @desciption Apk下载完成回调监听
 * @since 2023/9/21
 **/
public interface ApkDownloadFinishedListener {
    void onApkDownloadFinished(String savePath);
}
