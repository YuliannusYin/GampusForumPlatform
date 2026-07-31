package com.campus.forum.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户状态修改请求 DTO
 * status=1 封禁（用户无法登录），status=0 解禁
 *
 * @author campus
 */
@Data
@Schema(description = "用户状态修改请求")
public class UserStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态 0正常 1封禁 */
    @Schema(description = "状态 0正常 1封禁（1=封禁，0=解禁）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

}
