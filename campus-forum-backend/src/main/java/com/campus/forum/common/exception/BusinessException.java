package com.campus.forum.common.exception;

import com.campus.forum.common.result.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * 用于在业务逻辑中抛出可预期的异常，由全局异常处理器捕获并返回给前端
 *
 * @author campus
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误状态码 */
    private final Integer code;

    /** 错误提示信息 */
    private final String message;

    /**
     * 基于 ResultCode 构造业务异常
     *
     * @param resultCode 状态码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 基于 ResultCode 构造业务异常，并覆盖提示信息
     *
     * @param resultCode 状态码枚举
     * @param message    自定义提示信息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    /**
     * 基于状态码与提示信息构造业务异常
     *
     * @param code    状态码
     * @param message 提示信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 基于提示信息构造业务异常，状态码默认为业务异常
     *
     * @param message 提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
        this.message = message;
    }

}
