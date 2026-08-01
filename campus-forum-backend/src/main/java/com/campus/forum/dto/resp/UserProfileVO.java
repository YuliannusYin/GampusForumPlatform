package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户公开主页视图对象
 * 展示用户资料与关注/粉丝/发帖统计
 *
 * @author campus
 */
@Data
@Schema(description = "用户公开主页")
public class UserProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /** 用户名 */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    /** 头像URL */
    @Schema(description = "头像URL", example = "/uploads/avatar/1.png")
    private String avatar;

    /** 个人简介 */
    @Schema(description = "个人简介", example = "热爱编程的校园开发者")
    private String bio;

    /** 积分 */
    @Schema(description = "积分", example = "120")
    private Integer points;

    /** 等级 */
    @Schema(description = "等级", example = "3")
    private Integer level;

    /** 注册时间 */
    @Schema(description = "注册时间", example = "2024-01-01 12:00:00")
    private Date createTime;

    /** 关注数 */
    @Schema(description = "关注数", example = "20")
    private long followingCount;

    /** 粉丝数 */
    @Schema(description = "粉丝数", example = "100")
    private long followerCount;

    /** 发帖数 */
    @Schema(description = "发帖数", example = "15")
    private long postCount;

}
