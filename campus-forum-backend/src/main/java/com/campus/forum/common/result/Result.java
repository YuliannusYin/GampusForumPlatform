package com.campus.forum.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结构包装类
 * 用于包装所有 Controller 接口的返回值，统一前后端交互格式
 *
 * @param <T> 业务数据类型
 * @author campus
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /**
     * 成功响应（无数据）
     *
     * @param <T> 业务数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义提示与数据）
     *
     * @param message 提示信息
     * @param data    业务数据
     * @param <T>     业务数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应（自定义状态码与提示）
     *
     * @param code    状态码
     * @param message 提示信息
     * @param <T>     业务数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（基于 ResultCode）
     *
     * @param resultCode 状态码枚举
     * @param <T>        业务数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败响应（基于 ResultCode 并覆盖提示信息）
     *
     * @param resultCode 状态码枚举
     * @param message    提示信息
     * @param <T>        业务数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

}
