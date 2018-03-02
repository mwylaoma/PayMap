package com.guo.core.common.exception;

/**
 * Created by guo on 3/2/2018.
 * 基础异常
 */
public class BaseException extends RuntimeException {

    public BaseException(String message) {
        super(message,new Throwable(message));
    }

    public BaseException(Throwable cause) {
        super(cause);
    }
    public BaseException(String message,Throwable cause) {
        super(message,cause);
    }
}
