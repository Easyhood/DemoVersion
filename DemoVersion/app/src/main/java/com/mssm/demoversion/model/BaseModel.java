package com.mssm.demoversion.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Easyhood
 * @desciption 基础json解析工具类
 * @since 2024/1/10
 **/
public class BaseModel {
    // 状态码
    @SerializedName("code")
    private int code;

    // 信息
    @SerializedName("msg")
    private String msg;

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

    @Override
    public String toString() {
        return "BaseModel{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
