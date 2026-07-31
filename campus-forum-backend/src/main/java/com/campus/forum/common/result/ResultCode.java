package com.campus.forum.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举
 * 包含状态码 code 与提示信息 message
 *
 * @author campus
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(200, "操作成功"),

    /** 未登录或 token 失效 */
    UNAUTHORIZED(401, "未登录或token失效"),

    /** 无权限访问 */
    FORBIDDEN(403, "无权限访问"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 参数错误 */
    PARAM_ERROR(400, "参数错误"),

    /** 参数校验失败 */
    PARAM_VALIDATE_FAILED(400, "参数校验失败"),

    /** 用户名或密码错误 */
    USERNAME_OR_PASSWORD_ERROR(400, "用户名或密码错误"),

    /** 账号已被封禁 */
    ACCOUNT_DISABLED(403, "账号已被封禁"),

    /** 用户名已存在 */
    USERNAME_EXISTS(400, "用户名已存在"),

    /** 邮箱已存在 */
    EMAIL_EXISTS(400, "邮箱已存在"),

    /** 用户不存在 */
    USER_NOT_FOUND(400, "用户不存在"),

    /** 无效的 token */
    TOKEN_INVALID(401, "无效的token"),

    /** token 已过期 */
    TOKEN_EXPIRED(401, "token已过期"),

    /** 业务异常 */
    BUSINESS_ERROR(500, "业务异常"),

    /** 系统异常 */
    SYSTEM_ERROR(500, "系统异常"),

    /** 操作过于频繁 */
    FREQUENT_OPERATION(429, "操作过于频繁"),

    /** 今日已签到 */
    ALREADY_SIGNED(400, "今日已签到"),

    /** 帖子不存在 */
    POST_NOT_FOUND(404, "帖子不存在"),

    /** 评论不存在 */
    COMMENT_NOT_FOUND(404, "评论不存在");

    /** 状态码 */
    private final Integer code;

    /** 提示信息 */
    private final String message;

}
