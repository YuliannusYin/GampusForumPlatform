package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户信息视图对象
 *
 * @author campus
 */
@Data
@Schema(description = "用户信息")
public class UserInfoVO implements Serializable {

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

    /** 邮箱 */
    @Schema(description = "邮箱", example = "admin@campus.edu")
    private String email;

    /** 积分 */
    @Schema(description = "积分", example = "0")
    private Integer points;

    /** 等级 */
    @Schema(description = "等级", example = "1")
    private Integer level;

    /** 角色 code 列表 */
    @Schema(description = "角色编码列表", example = "[\"ROLE_USER\"]")
    private List<String> roles;

}
