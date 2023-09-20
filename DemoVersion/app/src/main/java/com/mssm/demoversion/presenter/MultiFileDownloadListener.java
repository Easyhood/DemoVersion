package com.mssm.demoversion.presenter;

import com.mssm.demoversion.model.BaseFileDownloadModel;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Easyhood
 * @desciption 定义一个MultiFileDownloadListener接口，用来回调下载结果和进度，以及全部文件下载结束后的结果
 * @since 2023/9/19
 **/
public interface MultiFileDownloadListener {

    void onSuccess(String url, String filePath);

    void onFailure(String url, IOException e);

    void onProgress(String url, int progress);

    void onAllDownloadFinished(ArrayList<BaseFileDownloadModel> successList);
}
