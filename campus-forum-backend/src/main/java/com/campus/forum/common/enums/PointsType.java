package com.campus.forum.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分变更类型枚举
 * 标识积分变动的来源场景，供各业务模块调用 PointsService 时使用
 *
 * @author campus
 */
@Getter
@AllArgsConstructor
public enum PointsType {

    /** 签到 */
    SIGN_IN(1, "签到"),

    /** 发帖 */
    POST(2, "发帖"),

    /** 评论 */
    COMMENT(3, "评论"),

    /** 点赞被赞 */
    LIKED(4, "点赞被赞"),

    /** 管理员调整 */
    ADMIN(5, "管理员调整");

    /** 类型编码 */
    private final Integer code;

    /** 类型描述 */
    private final String description;

}
