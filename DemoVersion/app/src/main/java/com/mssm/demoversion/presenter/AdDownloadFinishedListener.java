package com.mssm.demoversion.presenter;

import com.mssm.demoversion.view.Advance;

import java.util.List;

/**
 * @author Easyhood
 * @desciption 广告下载完成回调监听
 * @since 2023/9/20
 **/
public interface AdDownloadFinishedListener {
    void onAdDownloadFinished(List<Advance> successAdvanceList);
}
