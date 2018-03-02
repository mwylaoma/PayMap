package com.guo.core.common.exception;

/**
 * Created by guo on 3/2/2018.
 * 数据库异常
 */
public class DBException extends BaseException {
    public DBException(String message) {
        super(message);
    }

    public DBException(Throwable cause) {
        super(cause);
    }

    public DBException(String message, Throwable cause) {
        super(message, cause);
    }
}
