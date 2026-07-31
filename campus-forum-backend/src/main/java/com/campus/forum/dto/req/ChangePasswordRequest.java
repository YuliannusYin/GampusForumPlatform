package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码请求 DTO
 *
 * @author campus
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 旧密码 */
    @Schema(description = "旧密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码 */
    @Schema(description = "新密码（6-20 个字符）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度需在 6-20 个字符之间")
    private String newPassword;

}
