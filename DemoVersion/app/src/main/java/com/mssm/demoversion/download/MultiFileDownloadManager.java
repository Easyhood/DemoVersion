package com.mssm.demoversion.download;

import android.util.Log;

import com.mssm.demoversion.model.BaseFileDownloadModel;
import com.mssm.demoversion.presenter.MultiFileDownloadListener;
import com.mssm.demoversion.util.Constant;
import com.mssm.demoversion.util.LogUtils;
import com.mssm.demoversion.util.Utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Easyhood
 * @desciption 多文件下载管理
 * @since 2023/9/19
 **/
public class MultiFileDownloadManager {

    private static final String TAG = "MultiFileDownloadManager";

    // 定义一个OkHttpClient对象
    private OkHttpClient okHttpClient;

    // 定义一个HashMap对象，用来存储每个下载任务的Call对象和进度信息
    private HashMap<String, DownloadInfo> downloadMap;

    // 定义一个HashMap对象，用来存储每个文件的期望MD5值
    private HashMap<String, String> md5Map;

    // 定义一个HashMap对象，用来存储每个文件的重试次数
    private HashMap<String, Integer> retryMap;

    // 定义一个ArrayList对象，用来存储全部下载成功并校验MD5成功的文件保存路径列表
    private ArrayList<BaseFileDownloadModel> successList;

    /**
     * 定义一个DownloadInfo类，用来封装每个下载任务的信息
     */
    private static class DownloadInfo {
        Call call; // 下载任务对应的Call对象
        long totalSize; // 文件总大小
        long currentSize; // 当前已下载大小
        int progress; // 当前进度百分比
    }

    /**
     * 构造方法，初始化OkHttpClient对象和HashMap对象和ArrayList对象
     */
    public MultiFileDownloadManager() {
        okHttpClient = new OkHttpClient().newBuilder()
                .sslSocketFactory(Utils.createSSLSocketFactory(), new Utils.TrustAllCerts())
                .hostnameVerifier(new Utils.TrustAllHostnameVerifier())
                .build();
        downloadMap = new HashMap<>();
        md5Map = new HashMap<>();
        retryMap = new HashMap<>();
        successList = new ArrayList<>();
    }

    /**
     * 开始多个下载任务，接收MultiDownloadModel作为参数
     *
     * @param modelList MultiDownloadModel
     */
    public void startMultiFileDownload(List<BaseFileDownloadModel> modelList) {
        LogUtils.d(TAG, "startMultiFileDownload: modelList: " + modelList.toString());
        if (successList != null) {
            successList.clear();
        }
        for (int i = 0; i < modelList.size(); i++) {
            downloadMap.put(modelList.get(i).getDownloadUrl(), new DownloadInfo());
        }
        for (int i = 0; i < modelList.size(); i++) {
            startSingleFileDownload(modelList.get(i));
        }
    }

    /**
     * 开始一个下载任务，接收一个文件的URL和保存路径作为参数
     *
     * @param downloadModel BaseFileDownloadModel
     */
    private void startSingleFileDownload(BaseFileDownloadModel downloadModel) {
        LogUtils.d(TAG, "startSingleFileDownload: downloadModel: " + downloadModel.toString());
        setExpectedMD5(downloadModel.getDownloadUrl(), downloadModel.getMd5Str());
        // 创建一个Request对象
        Request request = new Request.Builder()
                .url(downloadModel.getDownloadUrl())
                .build();
        // 创建一个Call对象，并将其添加到HashMap中
        Call call = okHttpClient.newCall(request);
        downloadMap.get(downloadModel.getDownloadUrl()).call = call;
        // 使用enqueue方法发送异步请求，并在回调中处理下载逻辑
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.d(TAG, "onFailure: cause by " + e.getMessage());
                // 下载失败时，从HashMap中移除该Call对象，并回调onFailure方法
                downloadMap.remove(downloadModel.getDownloadUrl());
                downloadModel.getListener().onFailure(downloadModel.getDownloadUrl(), e);
                // 检查是否所有文件都下载结束
                checkAllDownloadFinished(downloadModel.getListener());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse");
                // 下载成功时，获取响应体的输入流和文件总大小，并创建输出流和文件对象
                InputStream is = response.body().byteStream();
                long totalSize = response.body().contentLength();
                FileOutputStream fos = new FileOutputStream(new File(downloadModel.getSaveFilePath()));
                File file = new File(downloadModel.getSaveFilePath());
                // 定义缓冲区大小和每次读取的字节数
                byte[] buffer = new byte[1024];
                int len;
                // 初始化当前已下载大小和当前进度百分比，并更新HashMap中对应的值
                long currentSize = 0;
                int progress = 0;
                downloadMap.get(downloadModel.getDownloadUrl()).totalSize = totalSize;
                downloadMap.get(downloadModel.getDownloadUrl()).currentSize = currentSize;
                downloadMap.get(downloadModel.getDownloadUrl()).progress = progress;
                // 循环读取输入流，并写入输出流
                while ((len = is.read(buffer)) != -1) {
                    // 检查是否取消了下载任务，如果是，则关闭输入流和输出流，并删除文件
                    if (call.isCanceled()) {
                        is.close();
                        fos.close();
                        file.delete();
                        return;
                    }
                    // 否则，更新当前已下载大小和当前进度百分比，并回调onProgress方法
                    currentSize += len;
                    progress = (int) (currentSize * 100 / totalSize);
                    downloadMap.get(downloadModel.getDownloadUrl()).currentSize = currentSize;
                    downloadMap.get(downloadModel.getDownloadUrl()).progress = progress;
                    if ((progress % Constant.INDEX_10) == Constant.INDEX_0) {
                        downloadModel.getListener().onProgress(downloadModel.getDownloadUrl(), progress);
                    }
                    // 将缓冲区的内容写入输出流
                    fos.write(buffer, 0, len);
                }
                // 关闭输入流和输出流
                is.close();
                fos.close();
                // 从HashMap中移除该Call对象
                downloadMap.remove(downloadModel.getDownloadUrl());
                // 检查文件的MD5值是否与期望值一致，如果是，则回调onSuccess方法，并将文件保存路径添加到successList中
                if (checkMD5(downloadModel.getDownloadUrl(), downloadModel.getSaveFilePath())) {
                    downloadModel.getListener().onSuccess(downloadModel.getDownloadUrl(), downloadModel.getSaveFilePath());
                    if (!successList.contains(downloadModel)) {
                        successList.add(downloadModel);
                    }
                    // 检查是否所有文件都下载结束
                    checkAllDownloadFinished(downloadModel.getListener());
                } else {
                    // 否则，重试下载该文件，并更新重试次数
                    retryDownload(downloadModel);
                }
            }
        });
    }

    /**
     * 取消一个下载任务，接收一个文件的URL作为参数
     *
     * @param url 文件的URL
     */
    public void cancelDownload(String url) {
        // 从HashMap中获取该下载任务对应的Call对象，并取消请求
        Call call = downloadMap.get(url).call;
        call.cancel();
        // 从HashMap中移除该Call对象
        downloadMap.remove(url);
    }

    /**
     * 获取一个下载任务的进度百分比，接收一个文件的URL作为参数
     *
     * @param url 文件的URL
     * @return progress
     */
    public int getProgress(String url) {
        // 从HashMap中获取该下载任务对应的进度信息，并返回
        int progress = downloadMap.get(url).progress;
        return progress;
    }

    /**
     * 设置每个文件的期望MD5值，接收一个文件的URL和MD5值作为参数
     *
     * @param url 文件的URL
     * @param md5 期望MD5值
     */
    public void setExpectedMD5(String url, String md5) {
        // 将URL和MD5值添加到HashMap中
        md5Map.put(url, md5);
    }

    /**
     * 检查文件的实际MD5值是否与期望值一致，接收一个文件的URL和保存路径作为参数，返回一个布尔值
     *
     * @param url      文件的URL
     * @param filePath 保存路径
     * @return 比较MD5是否和期望MD5值相等
     */
    public boolean checkMD5(String url, String filePath) {
        try {
            // 使用DigestUtils类计算文件的实际MD5值，并转换为16进制字符串
            String actualMD5 = DigestUtils.md5Hex(new FileInputStream(filePath));
            // 从HashMap中获取该文件的期望MD5值，并比较是否相等，返回结果
            String expectedMD5 = md5Map.get(url);
            return actualMD5.equals(expectedMD5);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 重试下载一个文件，并更新重试次数，接收一个文件的URL和保存路径作为参数
     *
     * @param downloadModel BaseFileDownloadModel
     */
    public void retryDownload(BaseFileDownloadModel downloadModel) {
        // 从HashMap中获取该文件的重试次数，如果没有，则初始化为0
        int retryCount = retryMap.getOrDefault(downloadModel.getDownloadUrl(), 0);
        LogUtils.d(TAG, "retryDownload: retryCount : " + retryCount + "; url : " + downloadModel.getDownloadUrl());
        // 如果重试次数小于5，则增加1，并重新开始下载该文件
        if (retryCount < 5) {
            retryCount++;
            retryMap.put(downloadModel.getDownloadUrl(), retryCount);
            startSingleFileDownload(downloadModel);
        } else {
            // 否则，删除该文件，并回调onFailure方法，传入一个异常信息
            File file = new File(downloadModel.getSaveFilePath());
            file.delete();
            downloadModel.getListener().onFailure(downloadModel.getDownloadUrl(), new IOException("Failed to download and verify the file after 5 retries."));
            // 检查是否所有文件都下载结束
            checkAllDownloadFinished(downloadModel.getListener());
        }
    }

    /**
     * 检查是否所有文件都下载结束，并回调AllDownloadListener接口，接收一个MultiFileDownloadListener对象作为参数
     *
     * @param listener 监听器
     */
    public void checkAllDownloadFinished(MultiFileDownloadListener listener) {
        // 如果downloadMap为空，说明没有正在进行的下载任务，则回调onAllDownloadFinished方法，并传入successList作为参数
        if (downloadMap.isEmpty()) {
            listener.onAllDownloadFinished(successList);
        }
    }
}