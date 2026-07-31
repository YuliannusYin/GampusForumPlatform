package com.campus.forum.dto.resp;

import com.campus.forum.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 管理员用户视图对象
 * 用于后台用户管理列表展示，包含用户基本信息、状态与角色列表
 *
 * @author campus
 */
@Data
@Schema(description = "管理员用户信息")
public class AdminUserVO implements Serializable {

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

    /** 邮箱 */
    @Schema(description = "邮箱", example = "admin@campus.edu")
    private String email;

    /** 头像URL */
    @Schema(description = "头像URL", example = "/uploads/avatar/1.png")
    private String avatar;

    /** 积分 */
    @Schema(description = "积分", example = "0")
    private Integer points;

    /** 等级 */
    @Schema(description = "等级", example = "1")
    private Integer level;

    /** 状态 0正常 1封禁 */
    @Schema(description = "状态 0正常 1封禁", example = "0")
    private Integer status;

    /** 角色列表 */
    @Schema(description = "角色列表")
    private List<Role> roles;

    /** 创建时间 */
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

}
