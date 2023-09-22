package com.mssm.demoversion.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Easyhood
 * @desciption 升级版本模板解析类
 * @since 2023/9/20
 **/
public class OTAModel {
    // 状态码
    @SerializedName("code")
    private int code;

    // 信息
    @SerializedName("msg")
    private String msg;

    // 数据列表
    @SerializedName("data")
    private ApkModel data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ApkModel getData() {
        return data;
    }

    public void setData(ApkModel data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "OTAModel{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    /**
     * 定义apk信息的实体类
     */
    public static class ApkModel {
        // 应用名称
        @SerializedName("name")
        private String apkName;

        // 下载路径
        @SerializedName("file_path")
        private String filePath;

        // md5值
        @SerializedName("md5")
        private String md5Str;

        // 版本名称
        @SerializedName("version_name")
        private String versionName;

        // 版本号
        @SerializedName("version_code")
        private int versionCode;

        // 版本描述
        @SerializedName("version_desc")
        private String versionDesc;

        public String getApkName() {
            return apkName;
        }

        public void setApkName(String apkName) {
            this.apkName = apkName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getMd5Str() {
            return md5Str;
        }

        public void setMd5Str(String md5Str) {
            this.md5Str = md5Str;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionDesc() {
            return versionDesc;
        }

        public void setVersionDesc(String versionDesc) {
            this.versionDesc = versionDesc;
        }

        @Override
        public String toString() {
            return "ApkModel{" +
                    "apkName='" + apkName + '\'' +
                    ", filePath='" + filePath + '\'' +
                    ", md5Str='" + md5Str + '\'' +
                    ", versionName='" + versionName + '\'' +
                    ", versionCode=" + versionCode +
                    ", versionDesc='" + versionDesc + '\'' +
                    '}';
        }
    }
}
