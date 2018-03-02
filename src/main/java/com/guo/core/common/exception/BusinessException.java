package com.guo.core.common.exception;

/**
 * Created by guo on 3/2/2018.
 * 业务异常的自定义封装类
 */
public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
