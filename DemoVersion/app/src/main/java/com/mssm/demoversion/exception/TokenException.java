package com.mssm.demoversion.exception;

import com.hjq.http.exception.HttpException;

/**
 * @author Easyhood
 * @desciption Token 失效 异常
 * @since 2023/7/12
 **/
public final class TokenException extends HttpException {

    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
