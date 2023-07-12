package com.mssm.demoversion.exception;

import com.hjq.http.exception.HttpException;
import com.mssm.demoversion.bean.HttpData;

/**
 * @author Easyhood
 * @desciption 返回结果异常
 * @since 2023/7/12
 **/
public final class ResultException extends HttpException {

    private final HttpData<?> mData;

    public ResultException(String message, HttpData<?> data) {
        super(message);
        mData = data;
    }

    public ResultException(String message, Throwable cause, HttpData<?> data) {
        super(message, cause);
        mData = data;
    }

    public HttpData<?> getHttpData() {
        return mData;
    }
}
