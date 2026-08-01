package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 重置密码请求 DTO
 * 由超级管理员调用，重置普通管理员账号的密码（无需旧密码）
 *
 * @author campus
 */
@Data
@Schema(description = "重置密码请求")
public class PasswordResetRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 新密码 */
    @Schema(description = "新密码（6-20 个字符）", example = "newpass123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6-20 个字符之间")
    private String password;

}
