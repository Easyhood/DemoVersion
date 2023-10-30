package com.mssm.demoversion.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Easyhood
 * @desciption Gson数据接收模板解析类
 * @since 2023/7/18
 **/
public class AdvertiseModel {

    // 状态码
    @SerializedName("code")
    public int code;

    // 信息
    @SerializedName("msg")
    public String msg;

    // 数据列表
    @SerializedName("data")
    public List<PlanModel> data;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public List<PlanModel> getData() {
        return data;
    }

    /**
     * 定义广告计划的实体类
     */
    public static class PlanModel {
        // 计划名称
        @SerializedName("name")
        private String planName;

        // 计划唯一标识
        @SerializedName("uuid")
        private String planId;

        // 是否启用
        @SerializedName("is_enable")
        private boolean isEnable;

        // 开始时间
        @SerializedName("start_time")
        private String startTime;

        // 结束时间
        @SerializedName("end_time")
        private String endTime;

        // 创建时间
        @SerializedName("created_at")
        private String createdAt;

        // 更新时间
        @SerializedName("updated_at")
        private String updatedAt;

        // 数据列表
        @SerializedName("ad_materials")
        public List<MaterialsModel> adMaterials;

        public String getPlanName() {
            return planName;
        }

        public String getPlanId() {
            return planId;
        }

        public boolean isEnable() {
            return isEnable;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public List<MaterialsModel> getAdMaterials() {
            return adMaterials;
        }

        @Override
        public String toString() {
            return "PlanModel{" +
                    "planName='" + planName + '\'' +
                    ", planId='" + planId + '\'' +
                    ", isEnable=" + isEnable +
                    ", startTime='" + startTime + '\'' +
                    ", endTime='" + endTime + '\'' +
                    ", createdAt='" + createdAt + '\'' +
                    ", updatedAt='" + updatedAt + '\'' +
                    ", adMaterials=" + adMaterials +
                    '}';
        }
    }

    /**
     * 广告素材的实体类
     */
    public static class MaterialsModel {
        // 素材唯一标识
        @SerializedName("uuid")
        private String matId;

        // 素材名称
        @SerializedName("name")
        private String matName;

        // 素材类型
        @SerializedName("mat_type")
        private String matType;

        // 素材大小
        @SerializedName("file_size")
        private long fileSize;

        // 素材MD5
        @SerializedName("file_md5")
        private String fileMD5;

        // 素材名
        @SerializedName("file_name")
        private String fileName;

        // 素材文件路径
        @SerializedName("ad_file_path")
        private String adFilePath;

        // 素材信息作者标识
        @SerializedName("mgr_user_uuid")
        private String mgrUserUUID;

        // 素材创建时间
        @SerializedName("created_at")
        private String matCreatedAt;

        // 素材更新时间
        @SerializedName("updated_at")
        private String matUpdatedAt;

        // 是否播放
        @SerializedName("is_play")
        private boolean isPlay;

        // 播放时长（秒）
        @SerializedName("play_time")
        private int playTime;

        public String getMatId() {
            return matId;
        }

        public String getMatName() {
            return matName;
        }

        public String getMatType() {
            return matType;
        }

        public long getFileSize() {
            return fileSize;
        }

        public String getFileMD5() {
            return fileMD5;
        }

        public String getFileName() {
            return fileName;
        }

        public String getAdFilePath() {
            return adFilePath;
        }

        public String getMgrUserUUID() {
            return mgrUserUUID;
        }

        public String getMatCreatedAt() {
            return matCreatedAt;
        }

        public String getMatUpdatedAt() {
            return matUpdatedAt;
        }

        public boolean getIsPlay() {
            return isPlay;
        }

        public int getPlayTime() {
            return playTime;
        }

        @Override
        public String toString() {
            return "MaterialsModel{" +
                    "matId='" + matId + '\'' +
                    ", matName='" + matName + '\'' +
                    ", matType='" + matType + '\'' +
                    ", fileSize=" + fileSize +
                    ", fileMD5='" + fileMD5 + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", adFilePath='" + adFilePath + '\'' +
                    ", mgrUserUUID='" + mgrUserUUID + '\'' +
                    ", matCreatedAt='" + matCreatedAt + '\'' +
                    ", matUpdatedAt='" + matUpdatedAt + '\'' +
                    ", isPlay=" + isPlay +
                    ", playTime=" + playTime +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AdvertiseModel{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}

