package com.guo.core.common.exception;

/**
 * Created by guo on 3/2/2018.
 * 系统类异常
 */
public class SystemException extends BaseException {

    public SystemException( String message) {
        super(message);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }

    public SystemException(String message,Throwable cause) {
        super(message,cause);
    }
}
