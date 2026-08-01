package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建管理员账号请求 DTO
 * 由超级管理员调用，用于创建拥有 ROLE_ADMIN 角色的普通管理员账号
 *
 * @author campus
 */
@Data
@Schema(description = "创建管理员请求")
public class CreateAdminRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @Schema(description = "用户名（3-20 个字符）", example = "admin01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度需在 3-20 个字符之间")
    private String username;

    /** 邮箱 */
    @Schema(description = "邮箱", example = "admin@campus.edu", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 密码 */
    @Schema(description = "密码（6-20 个字符）", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6-20 个字符之间")
    private String password;

    /** 昵称（可选） */
    @Schema(description = "昵称（可选，为空时默认使用用户名）", example = "管理员")
    private String nickname;

}
