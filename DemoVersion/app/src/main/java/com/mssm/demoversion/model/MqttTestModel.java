package com.mssm.demoversion.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Easyhood
 * @desciption mqtt测试模板类
 * @since 2023/7/24
 **/
public class MqttTestModel {
    // 状态码
    @SerializedName("cmd")
    public String cmdStr;

    // 播放时间
    @SerializedName("display_time")
    public int displayTime;

    public String getCmdStr() {
        return cmdStr;
    }

    public int getDisplayTime() {
        return displayTime;
    }

    @Override
    public String toString() {
        return "MqttTestModel{" +
                "cmdStr='" + cmdStr + '\'' +
                ", displayTime=" + displayTime +
                '}';
    }
}
