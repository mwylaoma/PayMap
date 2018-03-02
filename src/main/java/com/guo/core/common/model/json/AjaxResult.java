package com.guo.core.common.model.json;

import com.guo.core.common.constant.ActionConstants;
import com.guo.core.common.exception.ResultCode;

import java.io.Serializable;

/**
 * Created by guo on 3/2/2018.
 * AJAX调用返回对象
 */
public class AjaxResult implements Serializable {

    //请求结果是否为成功
    private String ErrorCode = ResultCode.SUCCESS.getCode();

    //请求返回信息
    private String Message = ActionConstants.DEFAULT_SUCCESS_RETURNMSG;

    //请求结果
    private Object Date = null;

    public String getErrorCode() {
        return ErrorCode;
    }

    public void setErrorCode(String errorCode) {
        ErrorCode = errorCode;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public Object getDate() {
        return Date;
    }

    public void setDate(Object date) {
        Date = date;
    }

    /**
     * 获取正确结果模板
     *
     * @param message 请求返回信息
     * @param obj     请求结果
     * @return AjaxResult
     */
    public static AjaxResult getOK(String message, Object obj) {
        AjaxResult result = new AjaxResult();
        result.setMessage(message);
        result.setDate(obj);
        return result;
    }

    /**
     * 获取正确结果模板
     *
     * @param obj 请求结果
     * @return AjaxResult
     */
    public static AjaxResult getOK(Object obj) {
        AjaxResult result = new AjaxResult();
        result.setMessage(ActionConstants.DEFAULT_SUCCESS_RETURNMSG);
        result.setDate(obj);
        return result;
    }

    /**
     * 获取正确结果模板
     *
     * @return AjaxResult
     */
    public static AjaxResult getOK() {
        return getOK(ActionConstants.DEFAULT_SUCCESS_RETURNMSG, null);
    }

    /**
     * 获取错误结果模板
     *
     * @param errorCode
     * @param message   请求返回信息
     * @param obj       请求结果
     * @return AjaxResult
     */
    public static AjaxResult getError(ResultCode errorCode, String message, Object obj) {
        AjaxResult result = new AjaxResult();
        result.setErrorCode(errorCode.getCode());
        result.setMessage(message);
        result.setDate(obj);
        return result;
    }

    /**
     * 获取错误结果模板
     *
     * @return AjaxResult
     */
    public static final AjaxResult getError(ResultCode resultCode) {
        AjaxResult result = new AjaxResult();
        return getError(resultCode, resultCode.getMsg(), null);
    }

    @Override
    public String toString() {
        return "AjaxResult{" +
                "ErrorCode='" + ErrorCode + '\'' +
                ", Message='" + Message + '\'' +
                ", Date=" + Date +
                '}';
    }
}
