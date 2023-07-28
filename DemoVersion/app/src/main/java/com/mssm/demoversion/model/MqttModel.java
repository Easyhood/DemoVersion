package com.mssm.demoversion.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Easyhood
 * @desciption MQTT数据接收模板解析类
 * @since 2023/7/23
 **/
public class MqttModel {

    // 状态码
    @SerializedName("cmd")
    public String cmdStr;

    // 播放时间
    @SerializedName("display_time")
    public int displayTime;

    // 事件标识
    @SerializedName("event_uuid")
    public String eventUUID;

    // 结束文字
    @SerializedName("end_text")
    public String endText;

    // 背景布局数据列表
    @SerializedName("bg_layer")
    public BgLayerModel bgLayerModel;

    // 顶层布局数据列表
    @SerializedName("top_layer")
    public TopLayerModel topLayerModel;

    public String getCmdStr() {
        return cmdStr;
    }

    public int getDisplayTime() {
        return displayTime;
    }

    public String getEventUUID() {
        return eventUUID;
    }

    public String getEndText() {
        return endText;
    }

    public BgLayerModel getBgLayerModel() {
        return bgLayerModel;
    }

    public TopLayerModel getTopLayerModel() {
        return topLayerModel;
    }

    @Override
    public String toString() {
        return "MqttModel{" +
                "cmdStr='" + cmdStr + '\'' +
                ", displayTime=" + displayTime +
                ", eventUUID='" + eventUUID + '\'' +
                ", endText='" + endText + '\'' +
                ", bgLayerModel=" + bgLayerModel +
                ", topLayerModel=" + topLayerModel +
                '}';
    }

    /**
     * 定义背景布局实体类
     */
    public static class BgLayerModel {

        // 背景资源类型
        @SerializedName("bg_res_type")
        public String bgResType;

        // 背景资源名字
        @SerializedName("bg_start_res_name")
        public String bgStartResName;

        public String getBgResType() {
            return bgResType;
        }

        public String getBgStartResName() {
            return bgStartResName;
        }

        @Override
        public String toString() {
            return "BgLayerModel{" +
                    "bgResType='" + bgResType + '\'' +
                    ", bgStartResName='" + bgStartResName + '\'' +
                    '}';
        }
    }

    /**
     * 定义顶层布局实体类
     */
    public static class TopLayerModel {
        // 顶层布局背景图片列表
        @SerializedName("top_bg_img")
        public TopBgImageModel topBgImageModel;

        // 顶层二层布局数据列表
        @SerializedName("top_float_img")
        public TopFloatImgModel topFloatImgModel;

        public TopBgImageModel getTopBgImageModel() {
            return topBgImageModel;
        }

        public TopFloatImgModel getTopFloatImgModel() {
            return topFloatImgModel;
        }

        @Override
        public String toString() {
            return "TopLayerModel{" +
                    "topBgImageModel=" + topBgImageModel +
                    ", topFloatImgModel=" + topFloatImgModel +
                    '}';
        }
    }

    /**
     * 定义顶层布局背景图片实体类
     */
    public static class TopBgImageModel {

        // 资源类型
        @SerializedName("res_type")
        public String resType;

        // 资源地址
        @SerializedName("res_url")
        public String resUrl;

        public String getResType() {
            return resType;
        }

        public String getResUrl() {
            return resUrl;
        }

        @Override
        public String toString() {
            return "TopBgImageModel{" +
                    "resType='" + resType + '\'' +
                    ", resUrl='" + resUrl + '\'' +
                    '}';
        }
    }

    /**
     * 定义顶层二层布局图片实体类
     */
    public static class TopFloatImgModel {

        // 资源类型
        @SerializedName("res_type")
        public String resType;

        // 偏移x
        @SerializedName("display_offset_x")
        public int displayOffsetX;

        // 偏移y
        @SerializedName("display_offset_y")
        public int displayOffsetY;

        // 资源宽
        @SerializedName("display_width")
        public int displayWidth;

        // 资源高
        @SerializedName("display_height")
        public int displayHeight;

        // 资源地址
        @SerializedName("res_url")
        public String resUrl;

        public String getResType() {
            return resType;
        }

        public int getDisplayOffsetX() {
            return displayOffsetX;
        }

        public int getDisplayOffsetY() {
            return displayOffsetY;
        }

        public int getDisplayWidth() {
            return displayWidth;
        }

        public int getDisplayHeight() {
            return displayHeight;
        }

        public String getResUrl() {
            return resUrl;
        }

        @Override
        public String toString() {
            return "TopFloatImgModel{" +
                    "resType='" + resType + '\'' +
                    ", displayOffsetX=" + displayOffsetX +
                    ", displayOffsetY=" + displayOffsetY +
                    ", displayWidth=" + displayWidth +
                    ", displayHeight=" + displayHeight +
                    ", resUrl='" + resUrl + '\'' +
                    '}';
        }
    }
}
